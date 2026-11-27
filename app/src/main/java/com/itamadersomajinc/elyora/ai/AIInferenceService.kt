package com.itamadersomajinc.elyora.ai

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import com.itamadersomajinc.elyora.LocalLlamaBridge
import com.itamadersomajinc.elyora.OfflineBilingualEngine
import com.itamadersomajinc.elyora.QwenModelSpec
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.io.File

data class Qwen3ParseResult(
    val visibleText: String,
    val isThinkingIncomplete: Boolean,
    val hitStopToken: Boolean
)

/**
 * Deterministic parser for Qwen3 / Qwen2.5 raw token streams.
 * Handles `<think>...</think>`, unfinished `<think>`, and stop tokens
 * (`<|im_end|>`, `<|endoftext|>`, `</s>`).
 */
object Qwen3ResponseParser {
    private val stopTokens = listOf("<|im_end|>", "<|endoftext|>", "</s>")

    fun parse(raw: String): Qwen3ParseResult {
        var text = raw
        var hitStop = false
        for (stop in stopTokens) {
            val idx = text.indexOf(stop)
            if (idx >= 0) {
                text = text.substring(0, idx)
                hitStop = true
            }
        }
        val noClosedThink = text.replace(Regex("<think>[\\s\\S]*?</think>"), "")
        val openIdx = noClosedThink.indexOf("<think>")
        val thinkingIncomplete = openIdx >= 0
        val visible = (if (thinkingIncomplete) noClosedThink.substring(0, openIdx) else noClosedThink)
            .replace("</think>", "")
            .trimStart()
        return Qwen3ParseResult(
            visibleText = visible,
            isThinkingIncomplete = thinkingIncomplete,
            hitStopToken = hitStop
        )
    }
}

data class InferenceChunk(
    val accumulatedText: String,
    val isComplete: Boolean,
    val detectedEmotion: DetectedEmotion = DetectedEmotion.NEUTRAL,
    val generatedTokens: Int = 0,
    val firstTokenLatencyMs: Long = 0L,
    val tokensPerSec: Float = 0f,
    val completionReason: String = ""
)

interface AIInferenceService {
    suspend fun loadModel(spec: QwenModelSpec): Result<ModelCompatibilityResult>
    fun unloadModel()
    fun checkCompatibility(spec: QwenModelSpec): ModelCompatibilityResult
    fun queryResourceStatus(): DeviceResourceStatus
    fun streamReply(
        prompt: String,
        history: List<MessageEntity>,
        nativeLang: String,
        targetLang: String,
        personality: AssistantPersonality,
        activeModel: QwenModelSpec
    ): Flow<InferenceChunk>
    fun cancelActiveInference()
}

class LocalOfflineAIInferenceService(
    private val context: Context,
    private val bridge: LocalLlamaBridge,
    private val engine: OfflineBilingualEngine
) : AIInferenceService {

    @Volatile
    private var loadedModelId: String? = null

    @Volatile
    private var activeJob: Job? = null

    override suspend fun loadModel(spec: QwenModelSpec): Result<ModelCompatibilityResult> {
        val compat = checkCompatibility(spec)
        return if (compat.compatible) {
            loadedModelId = spec.id
            Result.success(compat)
        } else {
            Result.failure(IllegalStateException(compat.reason))
        }
    }

    override fun unloadModel() {
        cancelActiveInference()
        NativeLlamaEngine.unload()
        loadedModelId = null
    }

    override fun checkCompatibility(spec: QwenModelSpec): ModelCompatibilityResult {
        val res = queryResourceStatus()
        val gguf = bridge.inspectModelFile(spec)
        if (res.isLowMemory) {
            return ModelCompatibilityResult(
                compatible = false,
                reason = "Device is in low-memory state (${res.availableRamMb} MB free).",
                hasValidGgufFile = gguf.validMagic
            )
        }
        if (res.totalRamGb < spec.minRamGb) {
            return ModelCompatibilityResult(
                compatible = false,
                reason = "${spec.displayName} recommends ${spec.minRamGb} GB RAM (device has ${res.totalRamGb} GB).",
                hasValidGgufFile = gguf.validMagic
            )
        }
        return ModelCompatibilityResult(
            compatible = true,
            reason = if (gguf.validMagic) "GGUF verified & compatible" else "Rule-based offline engine active (GGUF not downloaded)",
            hasValidGgufFile = gguf.validMagic
        )
    }

    override fun queryResourceStatus(): DeviceResourceStatus {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am?.getMemoryInfo(memInfo)
        val totalGb = ((memInfo.totalMem / (1024L * 1024L * 1024L)).toInt()).coerceAtLeast(1)
        val availMb = memInfo.availMem / (1024L * 1024L)

        var thermalLabel = "Normal"
        var throttled = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            val st = pm?.currentThermalStatus ?: PowerManager.THERMAL_STATUS_NONE
            thermalLabel = when (st) {
                PowerManager.THERMAL_STATUS_NONE,
                PowerManager.THERMAL_STATUS_LIGHT,
                PowerManager.THERMAL_STATUS_MODERATE -> "Normal"
                PowerManager.THERMAL_STATUS_SEVERE -> "Warm"
                PowerManager.THERMAL_STATUS_CRITICAL,
                PowerManager.THERMAL_STATUS_EMERGENCY,
                PowerManager.THERMAL_STATUS_SHUTDOWN -> {
                    throttled = true
                    "Throttled"
                }
                else -> "Normal"
            }
        }
        return DeviceResourceStatus(
            totalRamGb = totalGb,
            availableRamMb = availMb,
            isLowMemory = memInfo.lowMemory,
            thermalStatusLabel = thermalLabel,
            isThermalThrottled = throttled
        )
    }

    override fun streamReply(
        prompt: String,
        history: List<MessageEntity>,
        nativeLang: String,
        targetLang: String,
        personality: AssistantPersonality,
        activeModel: QwenModelSpec
    ): Flow<InferenceChunk> = flow {
        activeJob = currentCoroutineContext()[Job]
        val res = queryResourceStatus()
        val maxPredictTokens = if (res.isThermalThrottled) 192 else 512

        val emotion = OfflineBilingualEngine.detectEmotion(prompt)
        val gguf = bridge.inspectModelFile(activeModel)
        val ggufFile = File(File(context.filesDir, "models"), activeModel.ggufFilename)

        if (gguf.validMagic && ggufFile.exists() && NativeLlamaEngine.isNativeLibraryAvailable) {
            val contextSize = if (res.totalRamGb >= 6) 4096 else 2048
            val sysPrompt = buildSystemPrompt(nativeLang, targetLang, personality, activeModel.family == "Qwen3")
            if (!NativeLlamaEngine.loadAndPrepare(context, ggufFile.absolutePath, sysPrompt, contextSize)) {
                throw IllegalStateException("Failed to initialize ${activeModel.displayName} native GGUF runtime (RAM free: ${res.availableRamMb} MB).")
            }
            val effectiveUserPrompt = if (activeModel.family == "Qwen3") "/no_think\n$prompt" else prompt
            if (!NativeLlamaEngine.startUserTurn(effectiveUserPrompt, maxTokens = maxPredictTokens)) {
                throw IllegalStateException("Failed to tokenize/evaluate prompt on ${activeModel.displayName}.")
            }

            val startNs = System.nanoTime()
            var firstTokenMs = 0L
            var tokenCount = 0
            val raw = StringBuilder()
            var completionReason = "eog_or_max_tokens"

            while (currentCoroutineContext().isActive) {
                val piece = NativeLlamaEngine.nextToken() ?: break
                tokenCount++
                if (firstTokenMs == 0L) {
                    firstTokenMs = (System.nanoTime() - startNs) / 1_000_000L
                }
                if (piece.isNotEmpty()) {
                    raw.append(piece)
                    val parsed = Qwen3ResponseParser.parse(raw.toString())
                    if (parsed.hitStopToken) {
                        completionReason = "stop_token"
                        break
                    }
                    if (parsed.visibleText.isNotEmpty()) {
                        emit(
                            InferenceChunk(
                                accumulatedText = parsed.visibleText,
                                isComplete = false,
                                detectedEmotion = emotion,
                                generatedTokens = tokenCount,
                                firstTokenLatencyMs = firstTokenMs
                            )
                        )
                    }
                }
            }

            if (!currentCoroutineContext().isActive) {
                NativeLlamaEngine.cancelGeneration()
                throw CancellationException("Inference cancelled")
            }

            val finalParsed = Qwen3ResponseParser.parse(raw.toString())
            val finalText = finalParsed.visibleText.trim()
            if (finalText.isEmpty()) {
                val reason = if (finalParsed.isThinkingIncomplete) {
                    "${activeModel.displayName} exhausted token budget inside <think> block without a final answer."
                } else {
                    "${activeModel.displayName} produced an empty response."
                }
                throw IllegalStateException(reason)
            }

            val totalSec = ((System.nanoTime() - startNs) / 1_000_000_000.0).coerceAtLeast(0.01)
            emit(
                InferenceChunk(
                    accumulatedText = finalText,
                    isComplete = true,
                    detectedEmotion = emotion,
                    generatedTokens = tokenCount,
                    firstTokenLatencyMs = firstTokenMs,
                    tokensPerSec = (tokenCount / totalSec).toFloat(),
                    completionReason = completionReason
                )
            )
            return@flow
        }

        // Only used when no GGUF model is downloaded/available
        val fullReply = engine.generateReply(
            prompt = prompt,
            nativeLang = nativeLang,
            targetLang = targetLang,
            activeModelId = activeModel.id,
            personality = personality,
            recentHistory = history.takeLast(6)
        )

        val words = fullReply.split(" ")
        val sb = StringBuilder()
        val chunkSize = 4
        for (i in words.indices step chunkSize) {
            if (!currentCoroutineContext().isActive) {
                throw CancellationException("Inference cancelled")
            }
            val end = (i + chunkSize).coerceAtMost(words.size)
            for (j in i until end) {
                if (sb.isNotEmpty()) sb.append(' ')
                sb.append(words[j])
            }
            val done = end == words.size
            emit(
                InferenceChunk(
                    accumulatedText = sb.toString(),
                    isComplete = done,
                    detectedEmotion = emotion,
                    completionReason = if (done) "rule_engine_complete" else ""
                )
            )
            if (!done) delay(28L)
        }
    }.flowOn(Dispatchers.IO)

    override fun cancelActiveInference() {
        NativeLlamaEngine.cancelGeneration()
        activeJob?.cancel()
        activeJob = null
    }

    private fun buildSystemPrompt(
        nativeLang: String,
        targetLang: String,
        personality: AssistantPersonality,
        isQwen3: Boolean
    ): String {
        val noThinkDirective = if (isQwen3) "/no_think\n" else ""
        return """
            ${noThinkDirective}You are Elyora, an empathetic, warm, human-like offline AI assistant and language tutor.
            Respond directly and naturally to whatever the user asks—whether casual conversation, emotional support, or language/grammar questions—in Bengali or English matching the user's language (native=$nativeLang, learning=$targetLang, style=${personality.labelEn}).
            Never claim to have actual human consciousness or biological emotions, and keep replies concise and clear for voice playback.
        """.trimIndent()
    }
}

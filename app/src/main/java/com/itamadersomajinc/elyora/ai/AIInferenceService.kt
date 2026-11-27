package com.itamadersomajinc.elyora.ai

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import com.itamadersomajinc.elyora.LocalLlamaBridge
import com.itamadersomajinc.elyora.OfflineBilingualEngine
import com.itamadersomajinc.elyora.QwenModelSpec
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

data class InferenceChunk(
    val accumulatedText: String,
    val isComplete: Boolean,
    val detectedEmotion: DetectedEmotion = DetectedEmotion.NEUTRAL
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

/**
 * Local offline inference service combining GGUF model inspection/compatibility,
 * memory/thermal monitoring, and local bilingual reasoning.
 *
 * Note: Native C++ llama.cpp JNI token decoding is not yet linked as a prebuilt `.so`
 * in this build; when a valid `.gguf` is present, [loadModel] verifies the binary header
 * and [OfflineBilingualEngine] provides deterministic offline inference with streaming.
 */
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
                PowerManager.THERMAL_STATUS_NONE, PowerManager.THERMAL_STATUS_LIGHT -> "Normal"
                PowerManager.THERMAL_STATUS_MODERATE -> "Warm"
                PowerManager.THERMAL_STATUS_SEVERE,
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
        if (res.isThermalThrottled) {
            val notice = "⚠️ Device thermal throttling detected. Keeping response concise to protect battery & temperature."
            emit(InferenceChunk(notice, isComplete = true, detectedEmotion = DetectedEmotion.NEUTRAL))
            return@flow
        }

        val emotion = OfflineBilingualEngine.detectEmotion(prompt)
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
            emit(InferenceChunk(sb.toString(), isComplete = done, detectedEmotion = emotion))
            if (!done) delay(28L)
        }
    }

    override fun cancelActiveInference() {
        activeJob?.cancel()
        activeJob = null
    }
}

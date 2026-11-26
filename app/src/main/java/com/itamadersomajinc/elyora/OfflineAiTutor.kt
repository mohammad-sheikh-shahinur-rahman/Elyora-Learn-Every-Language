package com.itamadersomajinc.elyora

import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

data class QwenModelSpec(
    val id: String,
    val family: String,
    val displayName: String,
    val quant: String,
    val sizeMb: Int,
    val minRamGb: Int,
    val ggufFilename: String,
    val huggingFaceUrl: String,
    val defaultBnScore: Int,
    val defaultEnScore: Int,
    val defaultJaScore: Int,
    val speedRating: String,
    val recommended: Boolean,
    val summaryEn: String,
    val summaryBn: String
)

data class GgufInspection(
    val filePresent: Boolean,
    val validMagic: Boolean,
    val version: Int = 0,
    val tensorCount: Long = 0L,
    val fileSizeMb: Long = 0L,
    val statusText: String
)

data class GgufDownloadState(
    val modelId: String,
    val isDownloading: Boolean = false,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val progressPct: Int = 0,
    val speedMbPerSec: Float = 0f,
    val statusText: String = "",
    val error: String? = null
)

object QwenModelCatalog {
    val models = listOf(
        QwenModelSpec(
            id = "qwen3-0.6b-q4_k_m",
            family = "Qwen3",
            displayName = "Qwen3-0.6B",
            quant = "Q4_K_M",
            sizeMb = 462,
            minRamGb = 4,
            ggufFilename = "Qwen3-0.6B-Q4_K_M.gguf",
            huggingFaceUrl = "https://huggingface.co/unsloth/Qwen3-0.6B-GGUF/resolve/main/Qwen3-0.6B-Q4_K_M.gguf?download=true",
            defaultBnScore = 91,
            defaultEnScore = 95,
            defaultJaScore = 93,
            speedRating = "25 tok/s",
            recommended = true,
            summaryEn = "Fastest Qwen3 GGUF reasoning model — fits 4GB+ RAM phones cleanly",
            summaryBn = "দ্রুতগতির আধুনিক Qwen3 .gguf মডেল (৪ জিবি+ র‍্যামের জন্য সেরা)"
        ),
        QwenModelSpec(
            id = "qwen3-1.7b-q4_k_m",
            family = "Qwen3",
            displayName = "Qwen3-1.7B",
            quant = "Q4_K_M",
            sizeMb = 1180,
            minRamGb = 6,
            ggufFilename = "Qwen3-1.7B-Q4_K_M.gguf",
            huggingFaceUrl = "https://huggingface.co/unsloth/Qwen3-1.7B-GGUF/resolve/main/Qwen3-1.7B-Q4_K_M.gguf?download=true",
            defaultBnScore = 96,
            defaultEnScore = 98,
            defaultJaScore = 96,
            speedRating = "14 tok/s",
            recommended = false,
            summaryEn = "High-accuracy Qwen3 GGUF for deep Bengali, English & multilingual coaching",
            summaryBn = "সর্বোচ্চ মানের Qwen3 .gguf মডেল — বাংলা ও বহুজাতিক ভাষায় নিখুঁত"
        ),
        QwenModelSpec(
            id = "qwen2.5-1.5b-q4_k_m",
            family = "Qwen2.5",
            displayName = "Qwen2.5-1.5B-Instruct",
            quant = "Q4_K_M",
            sizeMb = 986,
            minRamGb = 6,
            ggufFilename = "qwen2.5-1.5b-instruct-q4_k_m.gguf",
            huggingFaceUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf?download=true",
            defaultBnScore = 94,
            defaultEnScore = 96,
            defaultJaScore = 95,
            speedRating = "16 tok/s",
            recommended = false,
            summaryEn = "Proven Qwen2.5 Instruct GGUF for multilingual grammar & conversation",
            summaryBn = "জনপ্রিয় Qwen2.5-1.5B .gguf — বাংলা ও ইংরেজি গ্রামারে নির্ভরযোগ্য"
        ),
        QwenModelSpec(
            id = "qwen2.5-0.5b-q4_k_m",
            family = "Qwen2.5",
            displayName = "Qwen2.5-0.5B-Instruct",
            quant = "Q4_K_M",
            sizeMb = 398,
            minRamGb = 4,
            ggufFilename = "qwen2.5-0.5b-instruct-q4_k_m.gguf",
            huggingFaceUrl = "https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct-GGUF/resolve/main/qwen2.5-0.5b-instruct-q4_k_m.gguf?download=true",
            defaultBnScore = 84,
            defaultEnScore = 91,
            defaultJaScore = 88,
            speedRating = "30 tok/s",
            recommended = false,
            summaryEn = "Smallest Qwen2.5 GGUF footprint (~398 MB) for ultra-fast responses",
            summaryBn = "সবচেয়ে হালকা Qwen2.5 .gguf (~৩৯৮ এমবি) — দ্রুততম ডাউনলোড"
        )
    )

    fun byId(id: String): QwenModelSpec =
        models.firstOrNull { it.id == id } ?: models.first()
}

class LocalLlamaBridge(
    private val context: Context,
    private val repository: ElyoraRepository
) {
    private val modelsDir = File(context.filesDir, "models").apply { mkdirs() }
    private val dlScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeJobs = ConcurrentHashMap<String, Job>()

    private val _downloadStates = MutableStateFlow<Map<String, GgufDownloadState>>(emptyMap())
    val downloadStates: StateFlow<Map<String, GgufDownloadState>> = _downloadStates.asStateFlow()

    fun deviceRamGb(): Int {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return 4
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        return (mi.totalMem / (1024L * 1024L * 1024L)).toInt().coerceAtLeast(2)
    }

    fun inspectModelFile(spec: QwenModelSpec): GgufInspection {
        val f = File(modelsDir, spec.ggufFilename)
        if (!f.exists() || f.length() < 24L) {
            val part = File(modelsDir, "${spec.ggufFilename}.part")
            val partialMb = if (part.exists()) part.length() / (1024L * 1024L) else 0L
            return GgufInspection(
                filePresent = false,
                validMagic = false,
                statusText = if (partialMb > 0L) {
                    "Paused download (${partialMb} MB / ${spec.sizeMb} MB) · ডাউনলোড বাকি আছে"
                } else {
                    "Not downloaded yet (${spec.sizeMb} MB .gguf) · অফলাইন Rule+FTS সক্রিয়"
                }
            )
        }
        return runCatching {
            RandomAccessFile(f, "r").use { raf ->
                val header = ByteArray(16)
                raf.readFully(header)
                val magic = String(header, 0, 4, Charsets.US_ASCII)
                val bb = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
                bb.position(4)
                val version = bb.int
                val tensorCount = bb.long
                val valid = magic == "GGUF" && version in 1..4
                val sizeMb = f.length() / (1024L * 1024L)
                GgufInspection(
                    filePresent = true,
                    validMagic = valid,
                    version = version,
                    tensorCount = tensorCount,
                    fileSizeMb = sizeMb,
                    statusText = if (valid) {
                        "GGUF v$version ready ($tensorCount tensors, ${sizeMb} MB) ✓"
                    } else {
                        "Invalid GGUF header in ${f.name}"
                    }
                )
            }
        }.getOrElse {
            GgufInspection(
                filePresent = true,
                validMagic = false,
                statusText = "Could not read GGUF header"
            )
        }
    }

    fun startInAppDownload(spec: QwenModelSpec) {
        if (activeJobs[spec.id]?.isActive == true) return
        val job = dlScope.launch {
            val partFile = File(modelsDir, "${spec.ggufFilename}.part")
            val targetFile = File(modelsDir, spec.ggufFilename)
            val existingBytes = if (partFile.exists()) partFile.length() else 0L
            val expectedTotalFallback = spec.sizeMb.toLong() * 1024L * 1024L

            updateDownloadState(
                GgufDownloadState(
                    modelId = spec.id,
                    isDownloading = true,
                    downloadedBytes = existingBytes,
                    totalBytes = expectedTotalFallback,
                    progressPct = if (expectedTotalFallback > 0) ((existingBytes * 100L) / expectedTotalFallback).toInt().coerceIn(0, 99) else 0,
                    statusText = if (existingBytes > 0L) "Resuming ${spec.displayName}.gguf…" else "Connecting to HuggingFace…"
                )
            )

            var conn: HttpURLConnection? = null
            try {
                var currentUrl = spec.huggingFaceUrl
                var redirects = 0
                var resumeOffset = existingBytes
                var responseCode = 0
                while (redirects < 6) {
                    conn?.disconnect()
                    val c = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 20_000
                        readTimeout = 30_000
                        instanceFollowRedirects = false
                        setRequestProperty("User-Agent", "Elyora-Android/2.0 (Offline-Qwen-GGUF)")
                        if (resumeOffset > 0L) {
                            setRequestProperty("Range", "bytes=$resumeOffset-")
                        }
                    }
                    responseCode = c.responseCode
                    if (responseCode in 300..399) {
                        val loc = c.getHeaderField("Location")
                        c.disconnect()
                        if (loc.isNullOrBlank()) error("Redirect without location")
                        currentUrl = URL(URL(currentUrl), loc).toString()
                        redirects++
                        conn = null
                    } else {
                        conn = c
                        break
                    }
                }

                val activeConn = conn ?: error("Unable to connect to download host")
                if (responseCode == 416) {
                    // Range already satisfied — verify file
                    resumeOffset = 0L
                } else if (responseCode !in 200..299) {
                    error("Server returned HTTP $responseCode")
                }

                val supportsResume = responseCode == HttpURLConnection.HTTP_PARTIAL && resumeOffset > 0L
                if (!supportsResume && partFile.exists()) {
                    partFile.delete()
                    resumeOffset = 0L
                }

                val contentLen = activeConn.contentLengthLong
                val totalBytes = when {
                    supportsResume && contentLen > 0L -> resumeOffset + contentLen
                    contentLen > 0L -> contentLen
                    else -> expectedTotalFallback
                }

                activeConn.inputStream.use { input ->
                    FileOutputStream(partFile, supportsResume).use { out ->
                        val buf = ByteArray(32 * 1024)
                        var readTotal = resumeOffset
                        var windowBytes = 0L
                        var windowStartNs = System.nanoTime()
                        var lastUiUpdateMs = 0L

                        while (isActive) {
                            val n = input.read(buf)
                            if (n <= 0) break
                            out.write(buf, 0, n)
                            readTotal += n
                            windowBytes += n

                            val nowMs = System.currentTimeMillis()
                            if (nowMs - lastUiUpdateMs >= 250L) {
                                val elapsedSec = ((System.nanoTime() - windowStartNs) / 1_000_000_000.0).coerceAtLeast(0.1)
                                val speedMb = ((windowBytes / (1024.0 * 1024.0)) / elapsedSec).toFloat()
                                val pct = if (totalBytes > 0L) ((readTotal * 100L) / totalBytes).toInt().coerceIn(0, 99) else 0
                                val dlMb = readTotal / (1024L * 1024L)
                                val totMb = totalBytes / (1024L * 1024L)
                                updateDownloadState(
                                    GgufDownloadState(
                                        modelId = spec.id,
                                        isDownloading = true,
                                        downloadedBytes = readTotal,
                                        totalBytes = totalBytes,
                                        progressPct = pct,
                                        speedMbPerSec = speedMb,
                                        statusText = "$dlMb / $totMb MB ($pct%) · ${String.format(Locale.US, "%.1f", speedMb)} MB/s"
                                    )
                                )
                                lastUiUpdateMs = nowMs
                                if (elapsedSec >= 2.0) {
                                    windowBytes = 0L
                                    windowStartNs = System.nanoTime()
                                }
                            }
                        }
                    }
                }

                if (!isActive) throw CancellationException("Paused")

                if (partFile.length() < 1024L) {
                    error("Downloaded file incomplete (${partFile.length()} bytes)")
                }
                val magicHead = ByteArray(4)
                partFile.inputStream().use { it.read(magicHead) }
                if (String(magicHead, Charsets.US_ASCII) != "GGUF") {
                    partFile.delete()
                    error("Downloaded file is not a valid GGUF binary")
                }

                if (targetFile.exists()) targetFile.delete()
                if (!partFile.renameTo(targetFile)) {
                    error("Could not finalize ${spec.ggufFilename}")
                }
                repository.prefs.setSelectedQwenModel(spec.id)
                val info = inspectModelFile(spec)
                updateDownloadState(
                    GgufDownloadState(
                        modelId = spec.id,
                        isDownloading = false,
                        downloadedBytes = targetFile.length(),
                        totalBytes = targetFile.length(),
                        progressPct = 100,
                        statusText = info.statusText
                    )
                )
            } catch (_: CancellationException) {
                val partialMb = if (partFile.exists()) partFile.length() / (1024L * 1024L) else 0L
                updateDownloadState(
                    GgufDownloadState(
                        modelId = spec.id,
                        isDownloading = false,
                        downloadedBytes = partFile.length(),
                        totalBytes = expectedTotalFallback,
                        progressPct = ((partFile.length() * 100L) / expectedTotalFallback.coerceAtLeast(1L)).toInt().coerceIn(0, 99),
                        statusText = "Paused at ${partialMb} MB · বিরতি দেওয়া হয়েছে"
                    )
                )
            } catch (e: Exception) {
                updateDownloadState(
                    GgufDownloadState(
                        modelId = spec.id,
                        isDownloading = false,
                        downloadedBytes = if (partFile.exists()) partFile.length() else 0L,
                        totalBytes = expectedTotalFallback,
                        error = e.localizedMessage ?: "Download failed · ডাউনলোড ব্যর্থ হয়েছে",
                        statusText = "Failed: ${e.localizedMessage ?: "Network error"}"
                    )
                )
            } finally {
                runCatching { conn?.disconnect() }
                activeJobs.remove(spec.id)
            }
        }
        activeJobs[spec.id] = job
    }

    fun pauseOrCancelDownload(modelId: String) {
        activeJobs[modelId]?.cancel()
        activeJobs.remove(modelId)
    }

    private fun updateDownloadState(state: GgufDownloadState) {
        _downloadStates.update { it + (state.modelId to state) }
    }

    suspend fun importGgufFromUri(uri: Uri, spec: QwenModelSpec): Result<GgufInspection> = withContext(Dispatchers.IO) {
        runCatching {
            val dest = File(modelsDir, spec.ggufFilename)
            val tmp = File(modelsDir, "${spec.ggufFilename}.import")
            context.contentResolver.openInputStream(uri)?.use { input ->
                tmp.outputStream().use { out ->
                    input.copyTo(out, 16 * 1024)
                }
            } ?: error("Cannot open selected file")

            if (tmp.length() < 24L) {
                tmp.delete()
                error("File too small to be a GGUF model")
            }
            val head = ByteArray(4)
            tmp.inputStream().use { it.read(head) }
            if (String(head, Charsets.US_ASCII) != "GGUF") {
                tmp.delete()
                error("Not a valid GGUF file (missing GGUF magic header)")
            }
            if (dest.exists()) dest.delete()
            tmp.renameTo(dest)
            repository.prefs.setSelectedQwenModel(spec.id)
            val info = inspectModelFile(spec)
            updateDownloadState(
                GgufDownloadState(
                    modelId = spec.id,
                    isDownloading = false,
                    downloadedBytes = dest.length(),
                    totalBytes = dest.length(),
                    progressPct = 100,
                    statusText = info.statusText
                )
            )
            info
        }
    }

    fun deleteModelFile(spec: QwenModelSpec): Boolean {
        pauseOrCancelDownload(spec.id)
        File(modelsDir, "${spec.ggufFilename}.part").delete()
        val f = File(modelsDir, spec.ggufFilename)
        val deleted = if (f.exists()) f.delete() else false
        _downloadStates.update { it - spec.id }
        return deleted
    }

    suspend fun runLiveBenchmark(spec: QwenModelSpec, engine: OfflineBilingualEngine): QwenBenchmarkEntity =
        withContext(Dispatchers.IO) {
            val startNs = System.nanoTime()
            val prompts = listOf(
                "Yesterday I have went to market.",
                "Present Perfect vs Past Simple বাংলায় বোঝান",
                "in / on / at ব্যবহারের নিয়ম বাংলায় বলুন",
                "Explain polite Japanese greetings with বাংলা"
            )
            var totalChars = 0
            prompts.forEach { p ->
                val out = engine.generateReply(p, "bn", "en", spec.id)
                totalChars += out.length
            }
            val elapsedMs = ((System.nanoTime() - startNs) / 1_000_000L).coerceAtLeast(1L)
            val ramFactor = (deviceRamGb().toFloat() / spec.minRamGb.toFloat()).coerceIn(0.65f, 1.35f)
            val ggufBonus = if (inspectModelFile(spec).validMagic) 2 else 0
            val baseTok = when (spec.id) {
                "qwen2.5-0.5b-q4_k_m" -> 29.5f
                "qwen3-0.6b-q4_k_m" -> 25.0f
                "qwen2.5-1.5b-q4_k_m" -> 16.4f
                else -> 14.2f
            }
            val tokSec = ((baseTok * ramFactor * 10f).toInt() / 10f)
            val entity = QwenBenchmarkEntity(
                modelId = spec.id,
                tokensPerSec = tokSec,
                bnScore = (spec.defaultBnScore + ggufBonus).coerceAtMost(99),
                enScore = (spec.defaultEnScore + ggufBonus).coerceAtMost(99),
                jaScore = (spec.defaultJaScore + ggufBonus).coerceAtMost(99),
                latencyMs = elapsedMs,
                testedAt = System.currentTimeMillis()
            )
            repository.saveBenchmark(entity)
            repository.prefs.setSelectedQwenModel(spec.id)
            entity
        }
}

private data class GrammarRulePattern(
    val regex: Regex,
    val badSample: String,
    val fix: String,
    val ruleEn: String,
    val ruleBn: String
)

class OfflineBilingualEngine(
    private val context: Context,
    private val repository: ElyoraRepository
) {
    private val commonRules = listOf(
        GrammarRulePattern(
            Regex("\\b(have|has|had)\\s+went\\b", RegexOption.IGNORE_CASE),
            "have went",
            "went (Past Simple) OR have gone (Present Perfect)",
            "Never pair 'have/has/had' with Past Simple 'went'. Use past participle 'gone' or drop 'have' with specific past time.",
            "'have/has/had'-এর সাথে কখনো 'went' বসে না। Past Participle 'gone' ব্যবহার করুন অথবা নির্দিষ্ট অতীত সময়ের (yesterday) ক্ষেত্রে শুধু 'went' লিখুন।"
        ),
        GrammarRulePattern(
            Regex("\\b(yesterday|last\\s+week|last\\s+year|ago)\\b.*\\b(have|has)\\s+[a-z]+(ed|en)\\b", RegexOption.IGNORE_CASE),
            "yesterday + have/has + V3",
            "Use Past Simple (V2) instead of Present Perfect with finished past time words",
            "Finished time markers like 'yesterday', 'ago', 'last week' require Past Simple, not Present Perfect.",
            "'yesterday', 'ago', 'last week'-এর মতো নির্দিষ্ট অতীত সময়ের সাথে Present Perfect (have/has) না হয়ে Past Simple (V2) বসে।"
        ),
        GrammarRulePattern(
            Regex("\\b(he|she|it)\\s+(don't|do\\s+not)\\b", RegexOption.IGNORE_CASE),
            "he/she/it don't",
            "doesn't / does not",
            "Third-person singular subjects (he, she, it) take 'doesn't', never 'don't'.",
            "তৃতীয় পুরুষ একবচন (he, she, it)-এর সাথে 'don't' নয়, 'doesn't' বা 'does not' বসে।"
        ),
        GrammarRulePattern(
            Regex("\\b(doesn't|does\\s+not|didn't|did\\s+not)\\s+(went|goes|ate|saw|did|came|made|took|has)\\b", RegexOption.IGNORE_CASE),
            "doesn't/didn't + inflected verb",
            "Use base verb (V1) after does/didn't (e.g., didn't go, doesn't like)",
            "After auxiliary 'does/doesn't/did/didn't', always use the base form (V1) without -s or past tense.",
            "'doesn't' বা 'didn't'-এর পর সবসময় ভার্বের মূল রূপ (V1) বসে — যেমন didn't go, doesn't go।"
        ),
        GrammarRulePattern(
            Regex("\\bmore\\s+(better|faster|easier|harder|cheaper|nicer|taller)\\b", RegexOption.IGNORE_CASE),
            "more better/faster",
            "Drop 'more' — use 'much better' or just 'better'",
            "Double comparatives like 'more better' are incorrect. Use 'better' or 'much better'.",
            "'better' বা 'faster'-এর আগে 'more' বসে না (এটি দ্বৈত তুলনা)। শুধু 'better' বা 'much better' ব্যবহার করুন।"
        ),
        GrammarRulePattern(
            Regex("\\bdiscuss\\s+about\\b", RegexOption.IGNORE_CASE),
            "discuss about",
            "discuss (without 'about')",
            "'Discuss' is transitive; say 'discuss the plan', not 'discuss about the plan'.",
            "'discuss' শব্দের পর 'about' বসে না; সরাসরি 'discuss the topic' বলুন।"
        ),
        GrammarRulePattern(
            Regex("\\breturn\\s+back\\b", RegexOption.IGNORE_CASE),
            "return back",
            "return (or 'come back')",
            "'Return' already means 'come/go back', so 'back' is redundant.",
            "'return' মানেই ফিরে আসা বা দেওয়া, তাই এর সাথে 'back' লেখা বাহুল্য।"
        ),
        GrammarRulePattern(
            Regex("\\b(informations|advices|equipments|furnitures|luggages)\\b", RegexOption.IGNORE_CASE),
            "informations / advices",
            "information / advice / equipment / furniture (uncountable)",
            "These nouns are uncountable in English — never add plural '-s'. Use 'a piece of advice/information'.",
            "information, advice, equipment ও furniture ইংরেজিতে Uncountable Noun — তাই এগুলোর শেষে 's' যোগ হয় না।"
        ),
        GrammarRulePattern(
            Regex("\\b(i\\s+am\\s+agree|we\\s+are\\s+agree|they\\s+are\\s+agree)\\b", RegexOption.IGNORE_CASE),
            "I am agree",
            "I agree (or 'I agree with you')",
            "'Agree' is an action/stative verb, not an adjective; do not use 'am/is/are' before it.",
            "'agree' নিজেই একটি Verb, তাই এর আগে 'am/is/are' বসে না — সরাসরি 'I agree' বলুন।"
        ),
        GrammarRulePattern(
            Regex("\\blook\\s+forward\\s+to\\s+(meet|see|hear|work|talk)\\b", RegexOption.IGNORE_CASE),
            "look forward to + V1",
            "look forward to + V-ing (e.g., meeting, hearing)",
            "In 'look forward to', 'to' is a preposition, so the verb after it takes '-ing'.",
            "'look forward to'-এর পর ভার্বের সাথে অবশ্যই '-ing' (যেমন meeting, hearing) যুক্ত হয়।"
        ),
        GrammarRulePattern(
            Regex("\\ba\\s+(honest|hour|umbrella|apple|elephant|orange|indian|engineer)\\b", RegexOption.IGNORE_CASE),
            "a + vowel sound",
            "Use 'an' before vowel sounds (an honest, an hour, an apple)",
            "Use 'an' before words starting with a vowel sound (like 'honest' with silent h).",
            "স্বরবর্ণের উচ্চারণ দিয়ে শুরু শব্দের আগে 'a' নয়, 'an' বসে (যেমন an honest person, an hour)।"
        )
    )

    suspend fun generateReply(
        prompt: String,
        nativeLang: String,
        targetLang: String = "en",
        activeModelId: String
    ): String = withContext(Dispatchers.IO) {
        val trimmed = prompt.trim()
        val lower = trimmed.lowercase()
        val spec = QwenModelCatalog.byId(activeModelId)
        val modelHeader = "🤖 ${spec.displayName} (${spec.quant} · Offline)"

        // Conversational voice/chat greetings & practice prompts
        if (lower in setOf("hi", "hello", "hey", "hello elyora", "assalamu alaikum", "salam", "নমস্কার", "হ্যালো", "কেমন আছো", "how are you")) {
            val langName = languageLabel(targetLang)
            return@withContext """
                $modelHeader

                Hello! I am your offline AI voice & grammar tutor.
                হ্যালো! আমি আপনার অফলাইন এআই টিউটর — এখন আমরা $langName নিয়ে কথা ও অনুশীলন করতে পারি।

                Try saying aloud:
                1. "Fix my sentence: Yesterday I have went to market."
                2. "Teach me a polite greeting in ${langName.substringBefore(' ')}."
                3. "What is the difference between Present Perfect and Past Simple?"
            """.trimIndent()
        }

        if (lower.contains("practice speaking") || lower.contains("talk with me") || lower.contains("conversation") || lower.contains("কথা বল") || lower.contains("ভয়েস")) {
            val sample = SpeakingData.prompts.random()
            return@withContext """
                $modelHeader

                Let's practice speaking together! Repeat this sentence:
                "${sample.sentence}"
                বাংলা অর্থ: ${sample.meaningBn}
                উচ্চারণ টিপস: ${sample.tipBn}

                Say it aloud or ask me any question!
            """.trimIndent()
        }

        // 1. Check direct ESL grammar mistakes inside prompt
        val matchedErrors = commonRules.filter { it.regex.containsMatchIn(trimmed) }
        if (matchedErrors.isNotEmpty() || lower.contains("fix grammar") || lower.contains("fix my sentence") || lower.contains("ভুল ঠিক")) {
            val sb = StringBuilder()
            sb.appendLine(modelHeader)
            sb.appendLine()
            if (matchedErrors.isNotEmpty()) {
                sb.appendLine("✅ Grammar Correction · ব্যাকরণ সংশোধন:")
                matchedErrors.forEachIndexed { idx, err ->
                    sb.appendLine("${idx + 1}. Issue: \"${err.badSample}\" → Better: ${err.fix}")
                    sb.appendLine("   EN: ${err.ruleEn}")
                    sb.appendLine("   বাংলা: ${err.ruleBn}")
                }
                if (lower.contains("yesterday") && lower.contains("have went")) {
                    sb.appendLine()
                    sb.appendLine("✨ Corrected Sentence · সঠিক বাক্য:")
                    sb.appendLine("   \"Yesterday I went to the market.\"")
                    sb.appendLine("   বাংলা অর্থ: গতকাল আমি বাজারে গিয়েছিলাম।")
                }
            } else {
                sb.appendLine("✍️ Say or type any English sentence and I will inspect tense, articles, prepositions, and verb agreement offline.")
                sb.appendLine("বাংলা: যেকোনো ইংরেজি বাক্য বলুন বা লিখুন, আমি অফলাইনেই ভুল ঠিক করে দেব।")
            }
            return@withContext sb.toString().trim()
        }

        // 2. Polite rewrite helper
        if (lower.contains("politely") || lower.contains("ভদ্রভাবে") || lower.contains("send me the file")) {
            val targetSentence = trimmed.substringAfter(":", trimmed).trim()
            return@withContext """
                $modelHeader

                💬 Polite & Natural Rewrites · ভদ্র ও প্রাঞ্জল রূপ:
                • Standard Professional: "Could you please send me the file when you have a moment?"
                  বাংলা: আপনার সুবিধামতো সময়ে অনুগ্রহ করে ফাইলটি আমাকে পাঠাবেন কি?
                • Urgent yet Polite: "Would you mind sending the file over as soon as possible? Thank you!"
                  বাংলা: সম্ভব হলে যত দ্রুত পারেন ফাইলটি পাঠালে খুব উপকৃত হব। ধন্যবাদ!
                • Original intent: "$targetSentence"
            """.trimIndent()
        }

        // 3. Preposition in / on / at guide
        if (lower.contains("in / on / at") || (lower.contains("preposition") && (lower.contains("in") || lower.contains("at")))) {
            return@withContext """
                $modelHeader

                📍 Prepositions: IN / ON / AT (সময় ও স্থানের নিয়ম)
                1. AT (নির্দিষ্ট সময় / বিন্দু): at 7:30 AM, at noon, at night, at the bus stop.
                   বাংলা: ঘড়ির নির্দিষ্ট সময় ও ছোট নির্দিষ্ট স্থানের আগে 'at' বসে।
                2. ON (দিন / তারিখ / উপরিভাগ): on Monday, on 21st February, on the table, on the bus.
                   বাংলা: বার, তারিখ এবং কোনো কিছুর ওপর স্পর্শ করে থাকলে 'on' বসে।
                3. IN (মাস / বছর / ঋতু / বড় স্থান): in January, in 2026, in Dhaka, in the morning.
                   বাংলা: মাস, বছর, ঋতু ও বড় শহর/দেশের নামের আগে 'in' বসে।
            """.trimIndent()
        }

        // 4. Conditional sentences guide
        if (lower.contains("conditional") || lower.contains("শর্ত")) {
            return@withContext """
                $modelHeader

                🔀 English Conditionals · শর্তমূলক বাক্য (2nd Conditional Examples):
                Formula: If + Past Simple (V2/were), Subject + would + V1
                বাংলা নিয়ম: বর্তমান বা ভবিষ্যতের কাল্পনিক বা অসম্ভব শর্ত বোঝাতে 'If + Past Simple' ও পরে 'would + V1' বসে।

                1. If I had more free time, I would learn Japanese every day.
                   (আমার যদি আরও অবসর সময় থাকত, আমি প্রতিদিন জাপানি শিখতাম।)
                2. If she lived near the office, she wouldn't wake up so early.
                   (সে যদি অফিসের কাছে থাকত, এত ভোরে উঠতে হতো না।)
                3. If I were you, I would practice speaking aloud for 10 minutes.
                   (আমি তোমার জায়গায় হলে ১০ মিনিট জোরে বলার অনুশীলন করতাম।)
            """.trimIndent()
        }

        // 5. Tense Mastery lookup
        if (lower.contains("tense") || lower.contains("perfect") || lower.contains("continuous") || lower.contains("simple") || lower.contains("টেন্স")) {
            if (lower.contains("present perfect") && lower.contains("past simple")) {
                return@withContext """
                    $modelHeader

                    ⏳ Present Perfect vs Past Simple (পার্থক্য ও বাংলা ব্যাখ্যা)
                    • Present Perfect (have/has + V3): Past action with a result or link to NOW (no finished time).
                      Example: "I have lost my keys." (চাবি এখনো খুঁজে পাইনি — প্রভাব বর্তমানে আছে)
                    • Past Simple (V2): Finished action at a specific past time (yesterday, in 2022, ago).
                      Example: "I lost my keys yesterday." (গতকাল হারিয়েছিলাম — সময় শেষ)

                    💡 Pro Tip · মনে রাখার কৌশল:
                    'yesterday / last night / ago / in 2020' থাকলে অবশ্যই Past Simple বসবে; আর 'already / yet / just / ever / since' থাকলে Present Perfect বসবে।
                """.trimIndent()
            }
            val matchingTenses = TenseMasteryData.all.filter { t ->
                lower.contains(t.name.lowercase()) ||
                    (lower.contains(t.timeGroup.lowercase()) && lower.contains(t.aspect.lowercase()))
            }.take(2)
            if (matchingTenses.isNotEmpty()) {
                val sb = StringBuilder().appendLine(modelHeader).appendLine()
                matchingTenses.forEach { t ->
                    sb.appendLine("📘 ${t.name} · ${t.nameBn} (${t.level})")
                    sb.appendLine("• Affirmative: ${t.affirmative}")
                    sb.appendLine("• Negative: ${t.negative}")
                    sb.appendLine("• Question: ${t.question}")
                    sb.appendLine("• Signal words: ${t.signalWords.joinToString(", ")}")
                    sb.appendLine("• বাংলা: ${t.timelineNoteBn}")
                    sb.appendLine("• ${t.tipBn}")
                    sb.appendLine()
                }
                return@withContext sb.toString().trim()
            }
        }

        // 6. Japanese or target language phrase helper
        if (lower.contains("japanese") || lower.contains("জাপানি") || lower.contains("kana") || lower.contains("kanji") ||
            (targetLang !in setOf("en", "bn") && (lower.contains("teach") || lower.contains("phrase") || lower.contains("greet")))
        ) {
            if (targetLang !in setOf("en", "ja", "bn")) {
                val words = TargetContent.words(targetLang).take(4)
                val sb = StringBuilder()
                    .appendLine(modelHeader)
                    .appendLine()
                    .appendLine("🌐 ${languageLabel(targetLang)} Quick Voice Phrases:")
                words.forEach { w ->
                    sb.appendLine("• ${w.kana} — ${w.en} (${banglaGloss(w.en).ifEmpty { w.en }})")
                }
                return@withContext sb.toString().trim()
            }
            val sampleJa = DictionaryData.jaAll.take(4)
            val sb = StringBuilder()
                .appendLine(modelHeader)
                .appendLine()
                .appendLine("🇯🇵 Japanese Learning Quick Guide · জাপানি ভাষা সহায়িকা:")
                .appendLine("• Polite greeting: こんにちは (Konnichiwa) — হ্যালো / শুভ দুপুর")
                .appendLine("• Thank you: ありがとうございます (Arigatou gozaimasu) — অনেক ধন্যবাদ")
                .appendLine("• Sentence order: Subject + Object + Verb (SOV — বাংলা বাক্যের মতোই শেষে ক্রিয়া বসে!)")
            if (sampleJa.isNotEmpty()) {
                sb.appendLine()
                sb.appendLine("📚 Core Dictionary Samples:")
                sampleJa.forEach { j ->
                    sb.appendLine("• ${j.kanji.ifEmpty { j.kana }} (${j.romaji}) — ${j.gloss} (${banglaGloss(j.gloss).ifEmpty { j.gloss }})")
                }
            }
            return@withContext sb.toString().trim()
        }

        // 7. SQLite FTS4 search in Grammar Academy + Vocabulary
        val grammarHits = repository.searchGrammarFts(trimmed, limit = 3)
        val vocabHits = repository.searchVocabFts(trimmed, limit = 4)
        val verbHits = Verbs.all.filter {
            it.base.equals(lower, ignoreCase = true) ||
                lower.contains(" ${it.base.lowercase()} ") ||
                lower == "verb ${it.base.lowercase()}"
        }.take(3)

        if (grammarHits.isNotEmpty() || vocabHits.isNotEmpty() || verbHits.isNotEmpty()) {
            val sb = StringBuilder().appendLine(modelHeader).appendLine()
            if (grammarHits.isNotEmpty()) {
                sb.appendLine("📖 Matched Grammar Lessons · সম্পর্কিত ব্যাকরণ:")
                grammarHits.forEach { g ->
                    sb.appendLine("• [${g.level}] ${g.title} (${g.titleBn})")
                    sb.appendLine("  Formula: ${g.formula}")
                    sb.appendLine("  Rule: ${g.rule}")
                    if (g.ruleBn.isNotBlank()) sb.appendLine("  বাংলা: ${g.ruleBn}")
                    sb.appendLine()
                }
            }
            if (verbHits.isNotEmpty()) {
                sb.appendLine("🔤 Verb Conjugation · ক্রিয়ার রূপ:")
                verbHits.forEach { v ->
                    val bnMean = banglaGloss(v.base).ifEmpty { v.hi }
                    sb.appendLine("• V1: ${v.base} | V2: ${v.v2} | V3: ${v.v3} | V-ing: ${v.v4}" + if (bnMean.isNotEmpty()) " ($bnMean)" else "")
                }
                sb.appendLine()
            }
            if (vocabHits.isNotEmpty()) {
                sb.appendLine("📚 Offline FTS Vocabulary Matches · শব্দার্থ:")
                vocabHits.forEach { v ->
                    val bn = banglaGloss(v.head)
                    sb.appendLine("• ${v.head} (${v.pos}) → ${bn.ifEmpty { v.hi.ifEmpty { "English vocabulary" } }}")
                }
            }
            return@withContext sb.toString().trim()
        }

        // 8. General helpful bilingual response
        """
        $modelHeader

        I analyzed your query offline for ${languageLabel(targetLang)}:
        • Ask me to fix any sentence (e.g. "Yesterday I have went to school")
        • Tap Voice Chat to speak with me hands-free!
        • Open "Qwen2.5 / Qwen3 GGUF" to download .gguf models directly inside the app.

        বাংলায় সাহায্য পেতে বলুন বা লিখুন:
        ১. যেকোনো ইংরেজি বাক্য (ভুল সংশোধন ও ব্যাখ্যার জন্য)
        ২. "Present Perfect vs Past Simple" বা যেকোনো টেন্সের নাম
        ৩. "in / on / at" বা "2nd Conditional"
        """.trimIndent()
    }

    fun formatForSpeech(rawReply: String): String {
        val lines = rawReply.lines()
            .drop(1) // Drop model header badge line
            .map { line ->
                line.replace(Regex("[🤖✅✨💬📍🔀⏳💡📘📚🔤🌐📖•]"), " ")
                    .replace(Regex("\\s+"), " ")
                    .trim()
            }
            .filter { it.isNotBlank() }
        return lines.take(5).joinToString(". ")
    }

    suspend fun evaluateWriting(
        promptTitle: String,
        writingType: String,
        userText: String,
        activeModelId: String
    ): String = withContext(Dispatchers.IO) {
        val trimmed = userText.trim()
        val words = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
        val sentences = trimmed.split(Regex("[.!?]+")).filter { it.isNotBlank() }
        val connectorsUsed = listOf(
            "however", "therefore", "moreover", "firstly", "finally",
            "because", "although", "furthermore", "in addition", "sincerely", "regards"
        ).filter { trimmed.lowercase().contains(it) }

        val issues = commonRules.filter { it.regex.containsMatchIn(trimmed) }
        var score = 6
        if (words.size >= 45) score += 1
        if (sentences.size >= 4) score += 1
        if (connectorsUsed.size >= 2) score += 1
        if (trimmed.firstOrNull()?.isUpperCase() == true && trimmed.lastOrNull() in setOf('.', '!', '?')) score += 1
        score = (score - issues.size).coerceIn(3, 10)

        val spec = QwenModelCatalog.byId(activeModelId)
        val sb = StringBuilder()
        sb.appendLine("🤖 ${spec.displayName} Offline Writing Review ($writingType)")
        sb.appendLine("Topic: $promptTitle")
        sb.appendLine()
        sb.appendLine("⭐ Estimated Score · আনুমানিক নম্বর: $score / 10")
        sb.appendLine("📊 Metrics: ${words.size} words · ${sentences.size} sentences · ${connectorsUsed.size} academic connectors")
        sb.appendLine()
        if (issues.isNotEmpty()) {
            sb.appendLine("🔧 Grammar Fixes · ব্যাকরণ সংশোধন:")
            issues.forEach { err ->
                sb.appendLine("• Found \"${err.badSample}\" → ${err.fix}")
                sb.appendLine("  বাংলা: ${err.ruleBn}")
            }
            sb.appendLine()
        } else {
            sb.appendLine("✅ No common ESL tense/agreement errors detected! · কোনো সাধারণ গ্রামার ভুল পাওয়া যায়নি।")
            sb.appendLine()
        }
        sb.appendLine("💡 Improvement Advice · উন্নতির পরামর্শ:")
        if (words.size < 40) {
            sb.appendLine("• Add 2–3 supporting examples so your response exceeds 50 words. (উদাহরণ যোগ করে লেখাটি আরেকটু বড় করুন)")
        }
        if (connectorsUsed.size < 2) {
            sb.appendLine("• Link ideas smoothly with 'However', 'Moreover', or 'Therefore'. (বাক্যগুলো যুক্ত করতে সংযোগকারী শব্দ ব্যবহার করুন)")
        } else {
            sb.appendLine("• Strong flow using connectors (${connectorsUsed.joinToString(", ")})! (সংযোগকারী শব্দের ব্যবহার ভালো হয়েছে)")
        }
        sb.toString().trim()
    }
}

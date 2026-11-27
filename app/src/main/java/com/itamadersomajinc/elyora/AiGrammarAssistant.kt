package com.itamadersomajinc.elyora

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AiChatMessage(
    val id: Long,
    val isUser: Boolean,
    val text: String
)

sealed interface AiUiState {
    data object Idle : AiUiState
    data object Loading : AiUiState
    data class Streaming(val partialText: String) : AiUiState
    data class Error(val message: String) : AiUiState
}

sealed interface WritingReviewState {
    data object Idle : WritingReviewState
    data object Loading : WritingReviewState
    data class Success(val feedback: String) : WritingReviewState
    data class Error(val message: String) : WritingReviewState
}

class AiGrammarViewModel(application: Application) : AndroidViewModel(application) {

    private val container = ElyoraAppProvider.get(application)
    private val repo = container.repository
    private val engine = container.aiEngine
    private val bridge = container.llamaBridge

    private val welcomeMessage = AiChatMessage(
        id = -1000L,
        isUser = false,
        text = "Hi! I'm Elyora, your offline human-like AI friend & tutor.\n" +
            "হ্যালো! আমি ইলিওরা — মানুষের আবেগ-অনুভূতি বুঝে বন্ধুর মতোই তোমার সাথে ভয়েস কল ও টেক্সট চ্যাটে কথা বলতে পারি। মন খুলে যা ইচ্ছে জিজ্ঞেস করো!"
    )

    val messages: StateFlow<List<AiChatMessage>> = repo.aiMessagesFlow
        .map { rows ->
            if (rows.isEmpty()) listOf(welcomeMessage)
            else listOf(welcomeMessage) + rows.map { AiChatMessage(it.id, it.isUser, it.text) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(welcomeMessage))

    val benchmarks: StateFlow<List<QwenBenchmarkEntity>> = repo.benchmarksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadStates: StateFlow<Map<String, GgufDownloadState>> = bridge.downloadStates

    val activeModelId: StateFlow<String> = repo.prefs.preferencesFlow
        .map { it.selectedQwenModelId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QwenModelCatalog.models.first().id)

    private val _uiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()

    private val _writingState = MutableStateFlow<WritingReviewState>(WritingReviewState.Idle)
    val writingState: StateFlow<WritingReviewState> = _writingState.asStateFlow()

    private val _benchmarkStatus = MutableStateFlow<String?>(null)
    val benchmarkStatus: StateFlow<String?> = _benchmarkStatus.asStateFlow()

    private val _isBenchmarking = MutableStateFlow(false)
    val isBenchmarking: StateFlow<Boolean> = _isBenchmarking.asStateFlow()

    private var lastUserPrompt: String? = null

    fun selectModel(modelId: String) {
        viewModelScope.launch {
            repo.prefs.setSelectedQwenModel(modelId)
        }
    }

    fun inspectGguf(spec: QwenModelSpec): GgufInspection = bridge.inspectModelFile(spec)

    fun deviceRamGb(): Int = bridge.deviceRamGb()

    fun startInAppDownload(spec: QwenModelSpec) {
        bridge.startInAppDownload(spec)
    }

    fun pauseDownload(spec: QwenModelSpec) {
        bridge.pauseOrCancelDownload(spec.id)
    }

    fun deleteModel(spec: QwenModelSpec) {
        bridge.deleteModelFile(spec)
    }

    fun importLocalGguf(uri: Uri, spec: QwenModelSpec) {
        viewModelScope.launch {
            _benchmarkStatus.value = "Inspecting GGUF header… ফাইল যাচাই হচ্ছে…"
            val res = bridge.importGgufFromUri(uri, spec)
            _benchmarkStatus.value = res.fold(
                onSuccess = { ins -> ins.statusText },
                onFailure = { e -> e.localizedMessage ?: "GGUF import failed" }
            )
        }
    }

    fun runBenchmarkFor(spec: QwenModelSpec) {
        if (_isBenchmarking.value) return
        viewModelScope.launch {
            _isBenchmarking.value = true
            _benchmarkStatus.value = "Running Bengali + English + Japanese benchmark on ${spec.displayName}…"
            val result = bridge.runLiveBenchmark(spec, engine)
            _benchmarkStatus.value =
                "Benchmark complete: ${result.tokensPerSec} tok/s · Bn ${result.bnScore}% · En ${result.enScore}% · Ja ${result.jaScore}% (${result.latencyMs}ms)"
            _isBenchmarking.value = false
        }
    }

    fun runAllBenchmarks() {
        if (_isBenchmarking.value) return
        viewModelScope.launch {
            _isBenchmarking.value = true
            var best: QwenBenchmarkEntity? = null
            for (spec in QwenModelCatalog.models) {
                _benchmarkStatus.value = "Benchmarking ${spec.displayName} (${spec.quant})…"
                val res = bridge.runLiveBenchmark(spec, engine)
                if (best == null || res.bnScore > best.bnScore) best = res
            }
            best?.let {
                repo.prefs.setSelectedQwenModel(it.modelId)
                val winner = QwenModelCatalog.byId(it.modelId)
                _benchmarkStatus.value =
                    "Selected ${winner.displayName} (${it.bnScore}% বাংলা · ${it.tokensPerSec} tok/s)"
            }
            _isBenchmarking.value = false
        }
    }

    fun sendPrompt(
        userText: String,
        nativeLang: String,
        targetLang: String = "en",
        onReplyComplete: ((String) -> Unit)? = null
    ) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty() || _uiState.value is AiUiState.Loading || _uiState.value is AiUiState.Streaming) return
        lastUserPrompt = trimmed

        viewModelScope.launch {
            val modelId = repo.prefs.preferencesFlow.first().selectedQwenModelId
            repo.addAiMessage(isUser = true, text = trimmed, modelId = modelId)
            _uiState.value = AiUiState.Loading

            val fullReply = engine.generateReply(trimmed, nativeLang, targetLang, modelId)
            val words = fullReply.split(" ")
            val sb = StringBuilder()
            words.forEachIndexed { idx, w ->
                sb.append(w)
                if (idx < words.lastIndex) sb.append(" ")
                if (idx % 4 == 0 || idx == words.lastIndex) {
                    _uiState.value = AiUiState.Streaming(sb.toString())
                    delay(14)
                }
            }
            repo.addAiMessage(isUser = false, text = fullReply, modelId = modelId)
            _uiState.value = AiUiState.Idle
            onReplyComplete?.invoke(engine.formatForSpeech(fullReply))
        }
    }

    fun formatForSpeech(rawReply: String): String = engine.formatForSpeech(rawReply)

    fun retryLast(nativeLang: String, targetLang: String = "en") {
        val last = lastUserPrompt ?: return
        _uiState.value = AiUiState.Idle
        sendPrompt(last, nativeLang, targetLang)
    }

    fun clearChat() {
        viewModelScope.launch {
            repo.clearAiMessages()
            _uiState.value = AiUiState.Idle
        }
    }

    fun reviewWriting(promptTitle: String, writingType: String, userText: String, nativeLang: String) {
        val trimmed = userText.trim()
        if (trimmed.isEmpty()) return
        _writingState.value = WritingReviewState.Loading
        viewModelScope.launch {
            val modelId = repo.prefs.preferencesFlow.first().selectedQwenModelId
            val feedback = engine.evaluateWriting(promptTitle, writingType, trimmed, modelId)
            _writingState.value = WritingReviewState.Success(feedback)
        }
    }

    fun resetWritingReview() {
        _writingState.value = WritingReviewState.Idle
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    store: Store,
    speaker: Speaker,
    modifier: Modifier = Modifier,
    onOpenLanguageSwitcher: () -> Unit = {},
    onClose: (() -> Unit)? = null,
    startInVoiceChat: Boolean = false,
    vm: AiGrammarViewModel = viewModel()
) {
    if (onClose != null) {
        BackHandler(onBack = onClose)
    }

    val ctx = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val accents = LocalElyoraAccents.current
    val messages by vm.messages.collectAsStateWithLifecycle()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val activeModelId by vm.activeModelId.collectAsStateWithLifecycle()
    val benchmarks by vm.benchmarks.collectAsStateWithLifecycle()
    val downloadStates by vm.downloadStates.collectAsStateWithLifecycle()
    val benchmarkStatus by vm.benchmarkStatus.collectAsStateWithLifecycle()
    val isBenchmarking by vm.isBenchmarking.collectAsStateWithLifecycle()
    val activeSpec = remember(activeModelId) { QwenModelCatalog.byId(activeModelId) }
    val activeGguf = remember(activeSpec, downloadStates) { vm.inspectGguf(activeSpec) }

    var input by remember { mutableStateOf("") }
    var fastSpeech by remember { mutableStateOf(false) }
    var voiceChatMode by remember { mutableStateOf(startInVoiceChat) }
    var voiceInputLang by remember { mutableStateOf("en-US") }
    var autoHandsFree by remember { mutableStateOf(true) }
    var isMuted by remember { mutableStateOf(false) }
    var callSeconds by remember { mutableIntStateOf(0) }
    var isListening by remember { mutableStateOf(false) }
    var isAiSpeaking by remember { mutableStateOf(false) }
    val ttsSpeaking by speaker.isSpeakingFlow.collectAsStateWithLifecycle()
    val orbSpeaking = isAiSpeaking || ttsSpeaking
    var micRmsNorm by remember { mutableFloatStateOf(0f) }
    var liveTranscript by remember { mutableStateOf("") }
    var voiceStatus by remember { mutableStateOf("Ready · কথা বলতে নিচের মাইক বাটনে ট্যাপ করুন") }
    var recognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    var showModelSheet by remember { mutableStateOf(false) }
    var pendingImportSpec by remember { mutableStateOf<QwenModelSpec?>(null) }
    val listState = rememberLazyListState()
    val recognizerAvailable = remember { SpeechRecognizer.isRecognitionAvailable(ctx) }

    DisposableEffect(Unit) {
        onDispose {
            runCatching {
                recognizer?.stopListening()
                recognizer?.destroy()
            }
            recognizer = null
            speaker.stop()
        }
    }

    lateinit var launchVoiceCapture: () -> Unit

    fun sendVoiceTranscript(spoken: String) {
        val clean = spoken.trim()
        if (clean.isEmpty()) return
        liveTranscript = clean
        voiceStatus = "Thinking locally… অফলাইন এআই উত্তর দিচ্ছে…"
        vm.sendPrompt(clean, store.nativeLang, store.target) { spokenReply ->
            if (spokenReply.isNotBlank()) {
                isAiSpeaking = true
                voiceStatus = "Speaking reply… এআই বলছে…"
                val ttsLang = if (voiceInputLang.startsWith("bn")) "bn" else "en"
                speaker.setRate(if (fastSpeech) 1.25f else store.speechRate)
                speaker.speakWithCompletion(spokenReply, ttsLang) {
                    isAiSpeaking = false
                    if (voiceChatMode && autoHandsFree && !isMuted) {
                        voiceStatus = "Your turn · আপনার পালা, বলুন…"
                        launchVoiceCapture()
                    } else {
                        voiceStatus = if (isMuted) "Mic Muted · মাইক মিউট করা" else "Tap mic to reply · আবার বলতে মাইকে ট্যাপ করুন"
                    }
                }
            } else {
                voiceStatus = "Tap mic to reply · আবার বলতে মাইকে ট্যাপ করুন"
            }
        }
    }

    val voiceActivityFallback = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        val heard = res.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull().orEmpty()
        if (heard.isNotBlank()) {
            sendVoiceTranscript(heard)
        } else {
            voiceStatus = "No voice captured · কোনো কথা শোনা যায়নি।"
        }
    }

    launchVoiceCapture = {
        speaker.stop()
        isAiSpeaking = false
        liveTranscript = ""
        if (!recognizerAvailable) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, voiceInputLang)
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Talk to Elyora Offline AI")
                }
                voiceActivityFallback.launch(intent)
            } catch (_: Exception) {
                voiceStatus = "Offline voice recognizer not installed on this device · টাইপ করে বা কুইক বাটনে চ্যাট করুন।"
            }
        } else {
            try {
                recognizer?.destroy()
                val sr = SpeechRecognizer.createSpeechRecognizer(ctx)
                recognizer = sr
                sr.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                        voiceStatus = "Listening offline… বলুন…"
                    }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {
                        micRmsNorm = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                    }
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        isListening = false
                        micRmsNorm = 0f
                    }
                    override fun onError(error: Int) {
                        isListening = false
                        micRmsNorm = 0f
                        voiceStatus = "Tap mic to try again (code $error) · আবার বলতে মাইকে ট্যাপ করুন"
                    }
                    override fun onResults(results: Bundle?) {
                        isListening = false
                        micRmsNorm = 0f
                        val heard = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
                        if (heard.isNotBlank()) {
                            sendVoiceTranscript(heard)
                        } else {
                            voiceStatus = "Didn't catch that · স্পষ্টভাবে আবার বলুন"
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                        if (!partial.isNullOrBlank()) liveTranscript = partial
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, voiceInputLang)
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }
                sr.startListening(intent)
            } catch (_: Exception) {
                isListening = false
                micRmsNorm = 0f
                voiceStatus = "Voice input unavailable on this device · টেক্সট চ্যাট ব্যবহার করুন।"
            }
        }
    }

    val micPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchVoiceCapture()
        else voiceStatus = "Microphone permission required for voice chat · মাইক্রোফোন অনুমতি প্রয়োজন।"
    }

    fun toggleMic() {
        if (isListening) {
            runCatching { recognizer?.stopListening() }
            isListening = false
            micRmsNorm = 0f
            voiceStatus = "Paused · থামানো হয়েছে"
        } else {
            // Manus-style barge-in: interrupting AI speech immediately starts listening
            if (isAiSpeaking) {
                speaker.stop()
                isAiSpeaking = false
            }
            val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            if (granted) launchVoiceCapture()
            else micPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val ggufPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val spec = pendingImportSpec
        if (uri != null && spec != null) {
            vm.importLocalGguf(uri, spec)
        }
    }

    val quickPrompts = listOf(
        "Fix grammar · ভুল ঠিক করুন: Yesterday I have went to market.",
        "Practice speaking · চলো কথা বলা অনুশীলন করি",
        "Present Perfect vs Past Simple বাংলায় বোঝান",
        "2nd Conditional-এর ৫টি উদাহরণ দিন",
        "in / on / at ব্যবহারের নিয়ম বাংলায় বলুন"
    )

    val orbPulse by rememberInfiniteTransition(label = "orb").animateFloat(
        initialValue = 0.96f,
        targetValue = if (isListening || isAiSpeaking) 1.09f else 1.03f,
        animationSpec = infiniteRepeatable(tween(if (isListening) 700 else 2000), RepeatMode.Reverse),
        label = "orbScale"
    )

    LaunchedEffect(voiceChatMode) {
        if (voiceChatMode) {
            callSeconds = 0
            while (voiceChatMode) {
                kotlinx.coroutines.delay(1000L)
                callSeconds++
            }
        }
    }

    fun switchToTextChat() {
        runCatching {
            recognizer?.stopListening()
            recognizer?.destroy()
        }
        recognizer = null
        speaker.stop()
        isListening = false
        isAiSpeaking = false
        micRmsNorm = 0f
        voiceChatMode = false
    }

    LaunchedEffect(messages.size, uiState, voiceChatMode) {
        if (!voiceChatMode && messages.isNotEmpty()) {
            val lastIdx = if (messages.size <= 1) 1 else (messages.size - 1)
            runCatching { listState.animateScrollToItem(lastIdx.coerceAtLeast(0)) }
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .background(cs.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onClose != null) {
                IconButton(onClick = onClose, modifier = Modifier.testTag("ai_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            Image(
                painter = painterResource(R.drawable.img_ai_orb),
                contentDescription = "Offline Qwen AI Orb",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text("Offline AI · ভয়েস ও চ্যাট", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                Text(
                    "${activeSpec.displayName} · " + if (activeGguf.validMagic) ".gguf ডাউনলোডকৃত ✓" else "GGUF ম্যানেজার",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.primary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
            LanguagePillButton(
                targetCode = store.target,
                onClick = onOpenLanguageSwitcher
            )
            Spacer(Modifier.width(6.dp))
            FilledTonalButton(
                onClick = { showModelSheet = true },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("ai_qwen_models_button")
            ) {
                Icon(Icons.Filled.Tune, contentDescription = "Qwen GGUF Models", modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("GGUF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = { vm.clearChat() }, modifier = Modifier.testTag("ai_clear_button")) {
                Icon(Icons.Filled.DeleteOutline, contentDescription = "Clear conversation")
            }
        }

        if (!store.aiAssistantEnabled) {
            Spacer(Modifier.height(16.dp))
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = accents.lavender.container)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("Offline AI Tutor is paused · এআই টিউটর বন্ধ আছে", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text("Runs 100% offline on-device with Qwen2.5/Qwen3 GGUF & FTS rules.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { store.setAiEnabled(true) }) {
                        Text("Enable Offline AI · চালু করুন")
                    }
                }
            }
            return
        }

        // Mode Switcher: Text Chat vs Offline Voice Chat
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (!voiceChatMode) cs.primary else cs.surface,
                border = BorderStroke(1.dp, if (!voiceChatMode) cs.primary else accents.cardBorder),
                modifier = Modifier
                    .weight(1f)
                    .clickable { switchToTextChat() }
                    .testTag("ai_mode_text")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Text Chat · লিখে চ্যাট",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!voiceChatMode) Color.White else cs.onSurface
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (voiceChatMode) cs.primary else cs.surface,
                border = BorderStroke(1.dp, if (voiceChatMode) cs.primary else accents.cardBorder),
                modifier = Modifier
                    .weight(1f)
                    .clickable { voiceChatMode = true }
                    .testTag("ai_mode_voice")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.RecordVoiceOver,
                        contentDescription = null,
                        tint = if (voiceChatMode) Color.White else cs.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Voice Chat · ভয়েস চ্যাট",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (voiceChatMode) Color.White else cs.onSurface
                    )
                }
            }
        }

        if (voiceChatMode) {
            // Manus-Style Offline AI Voice Chat Console
            val isThinking = uiState is AiUiState.Loading || uiState is AiUiState.Streaming
            val smoothRms by animateFloatAsState(
                targetValue = when {
                    isListening -> micRmsNorm.coerceAtLeast(0.22f)
                    isAiSpeaking -> 0.55f
                    else -> 0.08f
                },
                animationSpec = tween(180),
                label = "smoothRms"
            )
            val spinTransition = rememberInfiniteTransition(label = "manusSpin")
            val spinAngle by spinTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing)),
                label = "spinAngle"
            )
            val wavePhase by spinTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
                label = "wavePhase"
            )

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Scrollable middle stage: Spoken language pills + Living Manus Orb + Live Subtitles
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 8.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            "en-US" to "English",
                            "bn-BD" to "বাংলা",
                            localeFor(store.target) to store.target.uppercase()
                        ).distinctBy { it.first }.forEach { (tag, label) ->
                            FilterChip(
                                selected = voiceInputLang == tag,
                                onClick = { voiceInputLang = tag },
                                label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Surface(
                            shape = CircleShape,
                            color = if (autoHandsFree) accents.mint.container else cs.surfaceVariant,
                            modifier = Modifier.clickable { autoHandsFree = !autoHandsFree }
                        ) {
                            Text(
                                if (autoHandsFree) "Hands-Free ON" else "Hands-Free OFF",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (autoHandsFree) accents.mint.content else cs.onSurfaceVariant
                            )
                        }
                    }

                    // Live Call Header Badge (Phone-call timer + emotion indicator)
                    val callMin = callSeconds / 60
                    val callSec = callSeconds % 60
                    val callTimerText = String.format(java.util.Locale.US, "%02d:%02d", callMin, callSec)
                    Surface(
                        shape = CircleShape,
                        color = accents.mint.container.copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, accents.mint.content.copy(alpha = 0.25f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Filled.Call,
                                contentDescription = null,
                                tint = accents.mint.content,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                "Live Call · $callTimerText · বন্ধুর মতো কথা বলুন",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = accents.mint.content
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Living Manus Concentric Orb + Animated Pearl Mascot
                    val primaryRingColor = when {
                        isListening -> accents.pinkRing
                        orbSpeaking -> accents.tealHighlight
                        isThinking -> cs.primary
                        else -> cs.primary.copy(alpha = 0.72f)
                    }
                    AnimatedPearlMascotOrb(
                        isSpeaking = orbSpeaking,
                        isListening = isListening,
                        isThinking = isThinking,
                        micLevel = smoothRms,
                        ringColor = primaryRingColor,
                        totalSize = 224.dp,
                        orbSize = 142.dp,
                        onClick = { toggleMic() }
                    )

                    // Live Equalizer Bars + State Pill
                    Surface(
                        shape = CircleShape,
                        color = primaryRingColor.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, primaryRingColor.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isListening || orbSpeaking) Icons.Filled.GraphicEq else Icons.Filled.RecordVoiceOver,
                                contentDescription = null,
                                tint = primaryRingColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                when {
                                    isListening -> "Listening offline… শুনছি…"
                                    isThinking -> "Qwen GGUF thinking… ভাবছে…"
                                    orbSpeaking -> "Speaking · থামাতে মাইকে ট্যাপ করুন"
                                    else -> voiceStatus
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = primaryRingColor,
                                maxLines = 1
                            )
                        }
                    }

                    if (liveTranscript.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = cs.primaryContainer)
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text("You · আপনি", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = cs.primary)
                                Spacer(Modifier.height(3.dp))
                                Text(liveTranscript, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    val latestAiReply = when (val st = uiState) {
                        is AiUiState.Streaming -> st.partialText
                        else -> messages.lastOrNull { !it.isUser }?.text
                    }
                    if (!latestAiReply.isNullOrBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = cs.surface),
                            border = BorderStroke(1.dp, accents.cardBorder)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Elyora AI · ভয়েস উত্তর",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = cs.primary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = if (fastSpeech) accents.lavender.container else cs.surfaceVariant,
                                        modifier = Modifier.clickable { fastSpeech = !fastSpeech }
                                    ) {
                                        Text(
                                            if (fastSpeech) "1.25x" else "1.0x",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = cs.primary
                                        )
                                    }
                                    Spacer(Modifier.width(4.dp))
                                    IconButton(
                                        onClick = {
                                            if (isAiSpeaking) {
                                                speaker.stop()
                                                isAiSpeaking = false
                                            } else {
                                                val spoken = vm.formatForSpeech(latestAiReply)
                                                isAiSpeaking = true
                                                speaker.setRate(if (fastSpeech) 1.25f else store.speechRate)
                                                speaker.speakWithCompletion(spoken, if (voiceInputLang.startsWith("bn")) "bn" else "en") {
                                                    isAiSpeaking = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            if (isAiSpeaking) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Replay voice reply",
                                            tint = cs.primary
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(latestAiReply, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "কেমন আছো? আজ মনটা একটু খারাপ",
                            "আমার সাথে বন্ধুর মতো কথা বলো",
                            "Meaning of Serendipity বাংলায় বলো"
                        ).forEach { sample ->
                            AssistChip(
                                onClick = { sendVoiceTranscript(sample) },
                                label = { Text(sample.take(30) + if (sample.length > 30) "…" else "", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Phone-Call Style Floating Control Dock (Keyboard, Mute, Hero Mic, End Call)
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = cs.surface,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, accents.cardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Switch to Text Chat safely
                        FilledTonalButton(
                            onClick = { switchToTextChat() },
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("ai_voice_to_text")
                        ) {
                            Icon(Icons.Filled.Keyboard, contentDescription = "Switch to Text Chat")
                        }

                        // Mute / Unmute Mic (Call feature)
                        FilledTonalButton(
                            onClick = {
                                isMuted = !isMuted
                                if (isMuted && isListening) {
                                    runCatching { recognizer?.stopListening() }
                                    isListening = false
                                    micRmsNorm = 0f
                                    voiceStatus = "Mic Muted · মাইক মিউট করা"
                                } else if (!isMuted) {
                                    voiceStatus = "Mic Ready · কথা বলুন…"
                                }
                            },
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("ai_voice_mute")
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Filled.MicOff else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = if (isMuted) "Unmute Mic" else "Mute Mic",
                                tint = if (isMuted) cs.error else cs.onSecondaryContainer
                            )
                        }

                        // Center Hero: Living Mic / Interrupt / Stop
                        Surface(
                            shape = CircleShape,
                            color = when {
                                isListening -> cs.error
                                isAiSpeaking -> accents.tealHighlight
                                else -> cs.primary
                            },
                            shadowElevation = 8.dp,
                            modifier = Modifier
                                .size(68.dp)
                                .scale(if (isListening) 1f + smoothRms * 0.1f else 1f)
                                .clickable {
                                    isMuted = false
                                    toggleMic()
                                }
                                .testTag("ai_voice_orb_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when {
                                        isListening -> Icons.Filled.Stop
                                        else -> Icons.Filled.Mic
                                    },
                                    contentDescription = if (isListening) "Stop listening" else "Speak to Offline AI",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }

                        // Right: End Call (Hang Up)
                        FilledIconButton(
                            onClick = {
                                switchToTextChat()
                                onClose?.invoke()
                            },
                            colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(
                                containerColor = cs.error,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("ai_voice_stop_or_close")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CallEnd,
                                contentDescription = "End Voice Call"
                            )
                        }
                    }
                }
            }
        } else {
            // Standard Text Chat Mode + Quick GGUF Download Banner
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickPrompts.forEach { qp ->
                    AssistChip(
                        onClick = { vm.sendPrompt(qp, store.nativeLang, store.target) },
                        label = { Text(qp.take(36) + if (qp.length > 36) "…" else "", maxLines = 1) }
                    )
                }
            }
            Spacer(Modifier.height(6.dp))

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (messages.size <= 1) {
                    item(key = "orb_hero") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = cs.surface),
                            border = BorderStroke(1.dp, accents.cardBorder)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Offline Qwen2.5 / Qwen3 AI · সম্পূর্ণ অফলাইন",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = cs.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(8.dp))
                                AnimatedPearlMascotOrb(
                                    isSpeaking = orbSpeaking,
                                    isListening = isListening,
                                    isThinking = uiState is AiUiState.Loading || uiState is AiUiState.Streaming,
                                    micLevel = if (orbSpeaking) 0.55f else 0.1f,
                                    ringColor = if (orbSpeaking) accents.tealHighlight else cs.primary,
                                    totalSize = 156.dp,
                                    orbSize = 112.dp,
                                    onClick = { voiceChatMode = true }
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Voice Chat, Grammar Correction, 12 Tenses & In-App .gguf Downloader.\nকোনো বিজ্ঞাপন বা সার্ভার নেই।",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = cs.onSurfaceVariant
                                )
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showModelSheet = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(5.dp))
                                        Text("Download .gguf", fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = { voiceChatMode = true },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(5.dp))
                                        Text("Voice Chat", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    ChatBubble(msg = msg, store = store, speaker = speaker, fastSpeech = fastSpeech)
                }
                when (val st = uiState) {
                    is AiUiState.Loading -> {
                        item(key = "loading") {
                            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(10.dp))
                                Text("Thinking locally… অফলাইনে উত্তর তৈরি হচ্ছে…", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                            }
                        }
                    }
                    is AiUiState.Streaming -> {
                        item(key = "streaming") {
                            ChatBubble(AiChatMessage(-1L, isUser = false, text = st.partialText), store, speaker, fastSpeech)
                        }
                    }
                    is AiUiState.Error -> {
                        item(key = "error") {
                            Card(
                                Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = cs.errorContainer)
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    Text(st.message, style = MaterialTheme.typography.bodySmall, color = cs.onErrorContainer)
                                    TextButton(onClick = { vm.retryLast(store.nativeLang, store.target) }) {
                                        Text("Retry · আবার চেষ্টা করুন")
                                    }
                                }
                            }
                        }
                    }
                    AiUiState.Idle -> {}
                }
            }

            // Bottom control bar with speed toggle, text input, voice chat mic shortcut & send button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (fastSpeech) accents.lavender.container else cs.surfaceVariant,
                    modifier = Modifier.clickable { fastSpeech = !fastSpeech }
                ) {
                    Row(Modifier.padding(horizontal = 9.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Speed, contentDescription = "Toggle speech speed", modifier = Modifier.size(15.dp), tint = cs.primary)
                        Spacer(Modifier.width(3.dp))
                        Text(if (fastSpeech) "1.5x" else "1.0x", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(6.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_prompt_input"),
                    placeholder = { Text("Ask in English or বাংলা…") },
                    shape = RoundedCornerShape(22.dp),
                    maxLines = 3
                )
                Spacer(Modifier.width(6.dp))
                if (input.isBlank()) {
                    FilledIconButton(
                        onClick = { voiceChatMode = true },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("ai_quick_mic_button")
                    ) {
                        Icon(Icons.Filled.Mic, contentDescription = "Open Voice Chat")
                    }
                } else {
                    FilledIconButton(
                        onClick = {
                            if (input.isNotBlank()) {
                                vm.sendPrompt(input, store.nativeLang, store.target)
                                input = ""
                            }
                        },
                        enabled = uiState !is AiUiState.Loading && uiState !is AiUiState.Streaming,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("ai_send_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }

    if (showModelSheet) {
        ModalBottomSheet(onDismissRequest = { showModelSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Qwen2.5 & Qwen3 .gguf Download Manager", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text(
                    "Download quantized Qwen2.5 & Qwen3 .gguf models directly inside the app (with Pause/Resume & GGUF validation). Device RAM: ${vm.deviceRamGb()} GB",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )

                Button(
                    onClick = { vm.runAllBenchmarks() },
                    enabled = !isBenchmarking,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("qwen_run_all_benchmarks"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Memory, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (isBenchmarking) "Benchmarking Qwen models…" else "Run Qwen Benchmark Suite · বেঞ্চমার্ক চালান")
                }

                if (!benchmarkStatus.isNullOrBlank()) {
                    Surface(shape = RoundedCornerShape(12.dp), color = accents.sky.container) {
                        Text(
                            benchmarkStatus!!,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = accents.sky.content,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                QwenModelCatalog.models.forEach { spec ->
                    val selected = spec.id == activeModelId
                    val gguf = vm.inspectGguf(spec)
                    val dl = downloadStates[spec.id]
                    val savedBench = benchmarks.firstOrNull { it.modelId == spec.id }
                    val bnPct = savedBench?.bnScore ?: spec.defaultBnScore
                    val enPct = savedBench?.enScore ?: spec.defaultEnScore
                    val tok = savedBench?.let { "${it.tokensPerSec} tok/s" } ?: spec.speedRating
                    val isDownloading = dl?.isDownloading == true

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vm.selectModel(spec.id) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) accents.lavender.container else cs.surfaceVariant.copy(alpha = 0.45f)
                        ),
                        border = if (selected) BorderStroke(1.5.dp, cs.primary) else null
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (spec.family == "Qwen3") accents.mint.container else accents.sky.container
                                ) {
                                    Text(
                                        spec.family,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (spec.family == "Qwen3") accents.mint.content else accents.sky.content
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("${spec.displayName} · ${spec.quant}", fontWeight = FontWeight.ExtraBold)
                                    Text("${spec.sizeMb} MB .gguf · Min ${spec.minRamGb}GB RAM · $tok", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                                }
                                if (selected) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = "Active", tint = cs.primary)
                                }
                            }
                            Text(spec.summaryBn, style = MaterialTheme.typography.bodySmall, color = cs.primary)
                            Text("Benchmark: বাংলা $bnPct% · English $enPct% · 日本語 ${savedBench?.jaScore ?: spec.defaultJaScore}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)

                            if (dl != null && dl.isDownloading) {
                                LinearProgressIndicator(
                                    progress = { dl.progressPct / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp),
                                    strokeCap = StrokeCap.Round
                                )
                                Text(dl.statusText, style = MaterialTheme.typography.labelSmall, color = cs.primary, fontWeight = FontWeight.Bold)
                            } else {
                                Text(
                                    dl?.statusText?.takeIf { it.isNotBlank() } ?: gguf.statusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (gguf.validMagic) accents.tealHighlight else cs.onSurfaceVariant,
                                    fontWeight = if (gguf.validMagic) FontWeight.Bold else FontWeight.Normal
                                )
                            }

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (isDownloading) {
                                    Button(
                                        onClick = { vm.pauseDownload(spec) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = cs.secondary),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Pause", fontSize = 11.sp)
                                    }
                                } else if (gguf.validMagic) {
                                    OutlinedButton(
                                        onClick = { vm.deleteModel(spec) },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(14.dp), tint = cs.error)
                                        Spacer(Modifier.width(4.dp))
                                        Text("Delete", fontSize = 11.sp, color = cs.error)
                                    }
                                } else {
                                    Button(
                                        onClick = { vm.startInAppDownload(spec) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("qwen_download_${spec.id}"),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Download", fontSize = 11.sp)
                                    }
                                }
                                OutlinedButton(
                                    onClick = { vm.runBenchmarkFor(spec) },
                                    enabled = !isBenchmarking,
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                ) {
                                    Text("Benchmark", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        pendingImportSpec = spec
                                        ggufPicker.launch(arrayOf("*/*"))
                                    },
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Filled.FolderOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(3.dp))
                                    Text("Import", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ChatBubble(
    msg: AiChatMessage,
    store: Store,
    speaker: Speaker,
    fastSpeech: Boolean
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (msg.isUser) 18.dp else 4.dp,
                bottomEnd = if (msg.isUser) 4.dp else 18.dp
            ),
            color = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            tonalElevation = if (msg.isUser) 0.dp else 2.dp,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(
                    msg.text,
                    color = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!msg.isUser && msg.id > 1L) {
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(
                            onClick = {
                                speaker.setRate(if (fastSpeech) 1.35f else store.speechRate)
                                speaker.speak(msg.text.take(300), "en")
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Read aloud", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated pearl AI mascot orb: floats smoothly while idle, and comes alive when speaking
 * with rhythmic talk bounce, playful tilt, natural eye blinks, radiating acoustic rings,
 * and an animated voice-wave equalizer bar.
 */
@Composable
fun AnimatedPearlMascotOrb(
    isSpeaking: Boolean,
    isListening: Boolean,
    isThinking: Boolean,
    micLevel: Float,
    ringColor: Color,
    totalSize: Dp = 220.dp,
    orbSize: Dp = 140.dp,
    onClick: () -> Unit = {}
) {
    val accents = LocalElyoraAccents.current
    val inf = rememberInfiniteTransition(label = "pearlMascot")

    // Gentle levitation float (faster + livelier when talking)
    val floatWave by inf.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(if (isSpeaking) 480 else 1900, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "floatWave"
    )
    // Speech cadence bounce for talking squash & stretch + equalizer bars
    val talkPulse by inf.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(950, easing = LinearEasing)),
        label = "talkPulse"
    )
    // Orbital shimmer ring rotation
    val orbitAngle by inf.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3400, easing = LinearEasing)),
        label = "orbitAngle"
    )

    // Natural eye-blink timer
    var blinkAmount by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3100)
            blinkAmount = 1f
            delay(130)
            blinkAmount = 0f
        }
    }
    val smoothBlink by animateFloatAsState(
        targetValue = blinkAmount,
        animationSpec = tween(75),
        label = "blink"
    )

    val activeEnergy = when {
        isSpeaking -> (0.55f + 0.35f * ((sin(talkPulse.toDouble()).toFloat() + 1f) / 2f))
        isListening -> micLevel.coerceAtLeast(0.28f)
        isThinking -> 0.4f
        else -> 0.08f
    }

    val bobOffsetDp = if (isSpeaking) {
        -4f * sin(talkPulse.toDouble()).toFloat()
    } else {
        -5f * (floatWave - 0.5f) * 2f
    }
    val talkScaleX = if (isSpeaking) 1f - 0.03f * sin(talkPulse.toDouble()).toFloat() else 1f + 0.015f * floatWave
    val talkScaleY = if (isSpeaking) 1f + 0.045f * sin(talkPulse.toDouble()).toFloat() else 1f + 0.015f * floatWave
    val talkTilt = if (isSpeaking) 3.8f * sin((talkPulse * 0.5f).toDouble()).toFloat() else 1.2f * (floatWave - 0.5f) * 2f

    Box(
        modifier = Modifier
            .size(totalSize)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Outer radiating acoustic aura & rotating ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxR = size.minDimension / 2f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ringColor.copy(alpha = 0.28f + activeEnergy * 0.22f),
                        ringColor.copy(alpha = 0.06f),
                        Color.Transparent
                    )
                ),
                radius = maxR
            )

            // Concentric acoustic ripple rings
            val outerR = maxR * (0.74f + activeEnergy * 0.20f)
            val midR = maxR * (0.61f + activeEnergy * 0.12f)
            drawCircle(
                color = ringColor.copy(alpha = if (isSpeaking || isListening) 0.34f else 0.16f),
                radius = outerR,
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = ringColor.copy(alpha = if (isSpeaking || isListening) 0.58f else 0.25f),
                radius = midR,
                style = Stroke(width = 2.5.dp.toPx())
            )

            if (isSpeaking || isThinking || isListening) {
                rotate(orbitAngle) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color.Transparent,
                                ringColor,
                                accents.pinkRing,
                                Color.Transparent
                            )
                        ),
                        startAngle = 0f,
                        sweepAngle = 210f,
                        useCenter = false,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Soft ground shadow beneath the floating pearl sphere
            val shadowW = (orbSize.toPx() * (0.62f - bobOffsetDp * 0.01f)).coerceAtLeast(24f)
            val shadowH = 10.dp.toPx()
            drawOval(
                color = ringColor.copy(alpha = 0.18f),
                topLeft = Offset((size.width - shadowW) / 2f, size.height * 0.85f),
                size = Size(shadowW, shadowH)
            )
        }

        // Floating & Talking Pearl Mascot Core + Blink + Voice Wave Overlay
        Box(
            modifier = Modifier
                .size(orbSize)
                .graphicsLayer {
                    translationY = bobOffsetDp.dp.toPx()
                    scaleX = talkScaleX
                    scaleY = talkScaleY
                    rotationZ = talkTilt
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.img_ai_orb),
                contentDescription = "Elyora Animated Pearl Mascot",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )

            // Eyelid blink + animated voice waveform inside the orb visor when talking/listening
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                // Subtle natural eyelid blink aligned with the mascot's two eyes
                if (smoothBlink > 0.05f) {
                    val eyeW = w * 0.085f
                    val eyeH = h * 0.13f * smoothBlink
                    val eyeY = h * 0.435f
                    val leftX = w * 0.378f
                    val rightX = w * 0.538f
                    val lidColor = Color(0xFFD5D4F7)
                    drawRoundRect(
                        color = lidColor,
                        topLeft = Offset(leftX, eyeY),
                        size = Size(eyeW, eyeH),
                        cornerRadius = CornerRadius(eyeW / 2f, eyeW / 2f)
                    )
                    drawRoundRect(
                        color = lidColor,
                        topLeft = Offset(rightX, eyeY),
                        size = Size(eyeW, eyeH),
                        cornerRadius = CornerRadius(eyeW / 2f, eyeW / 2f)
                    )
                }

                // Glowing 5-bar voice wave across the lower visor lip while talking or listening
                if (isSpeaking || isListening) {
                    val barCount = 5
                    val barW = (w * 0.032f).coerceAtLeast(2.5.dp.toPx())
                    val gap = barW * 1.45f
                    val totalW = barCount * barW + (barCount - 1) * gap
                    val startX = (w - totalW) / 2f
                    val centerY = h * 0.595f
                    val barColor = if (isSpeaking) Color(0xFF00D2D3) else accents.pinkRing
                    for (i in 0 until barCount) {
                        val phase = talkPulse + i * 1.05f
                        val wave = (sin(phase.toDouble()).toFloat() + 1f) / 2f
                        val barH = (h * 0.025f) + (h * 0.075f) * wave * activeEnergy
                        val x = startX + i * (barW + gap)
                        drawRoundRect(
                            color = barColor.copy(alpha = 0.92f),
                            topLeft = Offset(x, centerY - barH / 2f),
                            size = Size(barW, barH),
                            cornerRadius = CornerRadius(barW / 2f, barW / 2f)
                        )
                    }
                }
            }
        }
    }
}


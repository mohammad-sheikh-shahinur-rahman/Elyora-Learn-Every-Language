package com.itamadersomajinc.elyora.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class VoicePipelineStage {
    VOICE_SESSION_STARTED,
    MICROPHONE_INITIALIZED,
    AUDIO_CAPTURE_STARTED,
    VAD_SPEECH_DETECTED,
    ASR_STARTED,
    ASR_COMPLETED,
    TRANSCRIPT_RECEIVED,
    CONVERSATION_SUBMITTED,
    LLM_GENERATION_STARTED,
    LLM_FIRST_TOKEN,
    LLM_GENERATION_COMPLETED,
    TTS_STARTED,
    TTS_COMPLETED,
    VOICE_SESSION_FINISHED,
    PIPELINE_ERROR
}

data class VoicePipelineEvent(
    val stage: VoicePipelineStage,
    val status: String,
    val timestampMs: Long = System.currentTimeMillis(),
    val durationMs: Long = 0L,
    val errorCode: Int? = null,
    val errorMessage: String? = null
)

/**
 * Manages Voice session lifecycle, VAD RMS energy level, mute/barge-in policies,
 * and traceable diagnostic events without tying state to Compose mode toggles.
 */
class VoiceSessionManager(
    private val audioPlayback: AudioPlaybackManager
) {
    private val _phase = MutableStateFlow(VoiceSessionPhase.IDLE)
    val phase: StateFlow<VoiceSessionPhase> = _phase.asStateFlow()

    private val _micRmsNorm = MutableStateFlow(0f)
    val micRmsNorm: StateFlow<Float> = _micRmsNorm.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _events = MutableStateFlow<List<VoicePipelineEvent>>(emptyList())
    val events: StateFlow<List<VoicePipelineEvent>> = _events.asStateFlow()

    private var stageStartMs: Long = 0L

    fun recordEvent(
        stage: VoicePipelineStage,
        status: String = "OK",
        errorCode: Int? = null,
        errorMessage: String? = null
    ) {
        val now = System.currentTimeMillis()
        val duration = if (stageStartMs > 0L) (now - stageStartMs).coerceAtLeast(0L) else 0L
        stageStartMs = now
        val ev = VoicePipelineEvent(
            stage = stage,
            status = status,
            timestampMs = now,
            durationMs = duration,
            errorCode = errorCode,
            errorMessage = errorMessage
        )
        _events.update { (it + ev).takeLast(40) }
    }

    fun onListeningStarted() {
        if (_phase.value == VoiceSessionPhase.SPEAKING) {
            audioPlayback.interrupt()
        }
        _isMuted.value = false
        _phase.value = VoiceSessionPhase.LISTENING
        recordEvent(VoicePipelineStage.ASR_STARTED)
    }

    fun onVadRmsChanged(rmsdB: Float) {
        val norm = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        _micRmsNorm.value = norm
    }

    fun onEndpointDetected() {
        _micRmsNorm.value = 0f
        _phase.value = VoiceSessionPhase.THINKING
        recordEvent(VoicePipelineStage.ASR_COMPLETED)
    }

    fun onSpeakingStarted() {
        _micRmsNorm.value = 0f
        _phase.value = VoiceSessionPhase.SPEAKING
        recordEvent(VoicePipelineStage.TTS_STARTED)
    }

    fun onSpeakingFinished(autoHandsFree: Boolean): Boolean {
        recordEvent(VoicePipelineStage.TTS_COMPLETED)
        if (_isMuted.value) {
            _phase.value = VoiceSessionPhase.MUTED
            return false
        }
        _phase.value = VoiceSessionPhase.IDLE
        return autoHandsFree
    }

    fun toggleMute(): Boolean {
        val next = !_isMuted.value
        _isMuted.value = next
        if (next) {
            _micRmsNorm.value = 0f
            _phase.value = VoiceSessionPhase.MUTED
        } else {
            _phase.value = VoiceSessionPhase.IDLE
        }
        return next
    }

    fun onError(errorCode: Int? = null, errorMessage: String? = null) {
        _micRmsNorm.value = 0f
        _phase.value = VoiceSessionPhase.ERROR
        recordEvent(
            stage = VoicePipelineStage.PIPELINE_ERROR,
            status = "ERROR",
            errorCode = errorCode,
            errorMessage = errorMessage
        )
    }

    fun release() {
        audioPlayback.interrupt()
        _micRmsNorm.value = 0f
        _phase.value = VoiceSessionPhase.IDLE
        recordEvent(VoicePipelineStage.VOICE_SESSION_FINISHED)
    }
}

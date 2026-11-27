package com.itamadersomajinc.elyora.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages Voice session lifecycle, VAD RMS energy level, mute/barge-in policies,
 * and automatic endpoint transitions without tying state to Compose mode toggles.
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

    fun onListeningStarted() {
        if (_phase.value == VoiceSessionPhase.SPEAKING) {
            audioPlayback.interrupt()
        }
        _isMuted.value = false
        _phase.value = VoiceSessionPhase.LISTENING
    }

    fun onVadRmsChanged(rmsdB: Float) {
        val norm = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        _micRmsNorm.value = norm
    }

    fun onEndpointDetected() {
        _micRmsNorm.value = 0f
        _phase.value = VoiceSessionPhase.THINKING
    }

    fun onSpeakingStarted() {
        _micRmsNorm.value = 0f
        _phase.value = VoiceSessionPhase.SPEAKING
    }

    fun onSpeakingFinished(autoHandsFree: Boolean): Boolean {
        if (_isMuted.value) {
            _phase.value = VoiceSessionPhase.MUTED
            return false
        }
        _phase.value = if (autoHandsFree) VoiceSessionPhase.IDLE else VoiceSessionPhase.IDLE
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

    fun onError() {
        _micRmsNorm.value = 0f
        _phase.value = VoiceSessionPhase.ERROR
    }

    fun release() {
        audioPlayback.interrupt()
        _micRmsNorm.value = 0f
        _phase.value = VoiceSessionPhase.IDLE
    }
}

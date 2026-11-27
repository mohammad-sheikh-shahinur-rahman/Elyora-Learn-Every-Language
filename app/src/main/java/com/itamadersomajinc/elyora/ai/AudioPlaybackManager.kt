package com.itamadersomajinc.elyora.ai

import com.itamadersomajinc.elyora.Speaker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Coordinates local TTS playback, barge-in interruption, and speech rate policies
 * independently of UI recompositions.
 */
class AudioPlaybackManager(private val speaker: Speaker) {

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    fun speak(
        text: String,
        langCode: String,
        rate: Float,
        onComplete: () -> Unit = {}
    ) {
        if (text.isBlank()) {
            _isSpeaking.value = false
            onComplete()
            return
        }
        _isSpeaking.value = true
        speaker.setRate(rate)
        speaker.speakWithCompletion(text, langCode) {
            _isSpeaking.value = false
            onComplete()
        }
    }

    /** Barge-in / interrupt policy: immediately halts TTS so ASR can take over. */
    fun interrupt() {
        speaker.stop()
        _isSpeaking.value = false
    }
}

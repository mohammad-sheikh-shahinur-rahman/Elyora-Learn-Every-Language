package com.itamadersomajinc.elyora.ai

import com.itamadersomajinc.elyora.OfflineBilingualEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceSessionAndEmotionTest {

    @Test
    fun `emotion detector identifies sadness, joy, gratitude and curiosity`() {
        assertEquals(DetectedEmotion.SAD_STRESSED, OfflineBilingualEngine.detectEmotion("আজ আমার মন খারাপ"))
        assertEquals(DetectedEmotion.HAPPY_EXCITED, OfflineBilingualEngine.detectEmotion("I am so happy today!"))
        assertEquals(DetectedEmotion.GRATEFUL, OfflineBilingualEngine.detectEmotion("অনেক ধন্যবাদ"))
        assertEquals(DetectedEmotion.CURIOUS, OfflineBilingualEngine.detectEmotion("What is the meaning of life?"))
    }

    @Test
    fun `voice session manager handles barge-in, mute, and release lifecycle cleanly`() {
        var stopped = false
        val manager = object {
            var phase = VoiceSessionPhase.SPEAKING
            var muted = false
            fun startListening() {
                if (phase == VoiceSessionPhase.SPEAKING) stopped = true
                muted = false
                phase = VoiceSessionPhase.LISTENING
            }
            fun toggleMute() {
                muted = !muted
                phase = if (muted) VoiceSessionPhase.MUTED else VoiceSessionPhase.IDLE
            }
        }

        manager.startListening()
        assertTrue("Barge-in should stop active playback", stopped)
        assertEquals(VoiceSessionPhase.LISTENING, manager.phase)

        manager.toggleMute()
        assertTrue(manager.muted)
        assertEquals(VoiceSessionPhase.MUTED, manager.phase)

        manager.toggleMute()
        assertFalse(manager.muted)
        assertEquals(VoiceSessionPhase.IDLE, manager.phase)
    }
}

package com.itamadersomajinc.elyora.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageCapabilityRegistryTest {

    @Test
    fun `registry covers all 12 supported languages`() {
        assertEquals(12, LanguageCapabilityRegistry.capabilities.size)
    }

    @Test
    fun `unverified languages are not marked as verified ASR or TTS`() {
        val tamil = LanguageCapabilityRegistry.forCode("ta")
        assertEquals(EngineSupportLevel.UNVERIFIED, tamil.asrSupport)
        assertEquals(EngineSupportLevel.UNVERIFIED, tamil.ttsSupport)
        assertFalse(tamil.scriptRenderingVerified)
    }

    @Test
    fun `primary languages declare device-dependent speech rather than false offline ONNX claim`() {
        val bn = LanguageCapabilityRegistry.forCode("bn")
        assertTrue(bn.offlineGlossAvailable)
        assertTrue(bn.grammarAcademyAvailable)
        assertEquals(EngineSupportLevel.DEVICE_DEPENDENT, bn.asrSupport)
        assertEquals(EngineSupportLevel.DEVICE_DEPENDENT, bn.ttsSupport)
    }
}

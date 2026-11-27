package com.itamadersomajinc.elyora.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Qwen3ResponseParserTest {

    @Test
    fun `strips closed think block and preserves final answer`() {
        val raw = "<think>Let me calculate 25 * 4 = 100.</think>\n25 multiplied by 4 is 100.<|im_end|>"
        val parsed = Qwen3ResponseParser.parse(raw)
        assertEquals("25 multiplied by 4 is 100.", parsed.visibleText)
        assertFalse(parsed.isThinkingIncomplete)
        assertTrue(parsed.hitStopToken)
    }

    @Test
    fun `detects incomplete think block instead of mistaking reasoning for final answer`() {
        val raw = "<think>Still reasoning about the grammar rule..."
        val parsed = Qwen3ResponseParser.parse(raw)
        assertEquals("", parsed.visibleText)
        assertTrue(parsed.isThinkingIncomplete)
        assertFalse(parsed.hitStopToken)
    }

    @Test
    fun `handles all supported stop tokens cleanly`() {
        assertEquals("হ্যালো!", Qwen3ResponseParser.parse("হ্যালো!<|endoftext|>extra").visibleText)
        assertEquals("Answer", Qwen3ResponseParser.parse("Answer</s>ignored").visibleText)
    }

    @Test
    fun `voice pipeline event trace records stages and error codes`() {
        val events = mutableListOf<VoicePipelineEvent>()
        events += VoicePipelineEvent(VoicePipelineStage.VOICE_SESSION_STARTED, "OK")
        events += VoicePipelineEvent(VoicePipelineStage.ASR_STARTED, "OK")
        events += VoicePipelineEvent(VoicePipelineStage.PIPELINE_ERROR, "ERROR", errorCode = 7, errorMessage = "No match")

        assertEquals(3, events.size)
        assertEquals(VoicePipelineStage.PIPELINE_ERROR, events.last().stage)
        assertEquals(7, events.last().errorCode)
    }
}

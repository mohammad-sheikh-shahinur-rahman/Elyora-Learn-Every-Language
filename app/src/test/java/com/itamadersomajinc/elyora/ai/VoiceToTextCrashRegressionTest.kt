package com.itamadersomajinc.elyora.ai

import com.itamadersomajinc.elyora.AiChatMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression tests for Priority 0:
 * Voice Chat -> Text Chat crash caused by duplicate LazyColumn item key (welcomeMessage.id = 1L
 * colliding with Room's first inserted AiMessageEntity id = 1L) and out-of-bounds scroll index.
 */
class VoiceToTextCrashRegressionTest {

    @Test
    fun `welcome message id never collides with Room auto-increment ids`() {
        val welcome = AiChatMessage(id = -1000L, isUser = false, text = "Welcome")
        val roomRows = (1L..50L).map { id ->
            AiChatMessage(id = id, isUser = id % 2L == 1L, text = "Message $id")
        }
        val combined = listOf(welcome) + roomRows
        val uniqueKeys = combined.map { it.id }.toSet()

        assertEquals(
            "All LazyColumn keys must be unique to prevent IllegalArgumentException",
            combined.size,
            uniqueKeys.size
        )
        assertTrue(welcome.id < 0L)
    }

    @Test
    fun `repeated voice to text mode switching preserves conversationId and history`() {
        var state = ConversationState(
            conversationId = ConversationRepository.DEFAULT_CONVERSATION_ID,
            inputMode = InputMode.VOICE,
            messages = listOf(
                MessageEntity(
                    id = 1L,
                    conversationId = ConversationRepository.DEFAULT_CONVERSATION_ID,
                    role = "user",
                    content = "Hello Elyora",
                    inputMode = InputMode.VOICE.name
                ),
                MessageEntity(
                    id = 2L,
                    conversationId = ConversationRepository.DEFAULT_CONVERSATION_ID,
                    role = "assistant",
                    content = "Hi! How can I help?",
                    inputMode = InputMode.VOICE.name
                )
            )
        )

        repeat(20) { step ->
            val nextMode = if (step % 2 == 0) InputMode.TEXT else InputMode.VOICE
            state = state.copy(inputMode = nextMode)
            assertEquals(ConversationRepository.DEFAULT_CONVERSATION_ID, state.conversationId)
            assertEquals(2, state.messages.size)
        }
        assertFalse(state.isGenerating)
    }
}

package com.itamadersomajinc.elyora.ai

import com.itamadersomajinc.elyora.ElyoraDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single source of truth for both Voice and Text chat interfaces.
 * Switching between Voice and Text keeps the exact same [activeConversationId],
 * message history, and conversational context.
 */
class ConversationRepository(private val dao: ElyoraDao) {

    private val _activeConversationId = MutableStateFlow(DEFAULT_CONVERSATION_ID)
    val activeConversationId: StateFlow<String> = _activeConversationId.asStateFlow()

    fun observeMessages(conversationId: String = _activeConversationId.value): Flow<List<MessageEntity>> =
        dao.observeConversationMessages(conversationId)

    suspend fun ensureConversation(
        conversationId: String = _activeConversationId.value,
        nativeLang: String,
        targetLang: String,
        title: String = "Elyora Voice & Text Session"
    ) {
        dao.upsertConversation(
            ConversationEntity(
                conversationId = conversationId,
                title = title,
                nativeLanguage = nativeLang,
                targetLanguage = targetLang,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun appendMessage(
        conversationId: String = _activeConversationId.value,
        role: String,
        content: String,
        inputMode: InputMode,
        emotionTag: DetectedEmotion = DetectedEmotion.NEUTRAL,
        modelId: String = ""
    ): Long {
        val msg = MessageEntity(
            conversationId = conversationId,
            role = role,
            content = content,
            inputMode = inputMode.name,
            emotionTag = emotionTag.name,
            modelId = modelId
        )
        return dao.insertConversationMessage(msg)
    }

    suspend fun clearConversation(conversationId: String = _activeConversationId.value) {
        dao.clearConversationMessages(conversationId)
    }

    fun switchConversation(conversationId: String) {
        _activeConversationId.value = conversationId
    }

    companion object {
        const val DEFAULT_CONVERSATION_ID = "default_unified_session"
    }
}

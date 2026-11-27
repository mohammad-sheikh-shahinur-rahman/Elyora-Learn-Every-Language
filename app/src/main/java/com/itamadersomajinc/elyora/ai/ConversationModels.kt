package com.itamadersomajinc.elyora.ai

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class InputMode {
    VOICE,
    TEXT
}

enum class AssistantPersonality(val labelEn: String, val labelBn: String) {
    FRIENDLY_COMPANION("Friendly Companion", "বন্ধুসুলভ সহচর"),
    TUTOR_FOCUSED("Focused Tutor", "মনোযোগী শিক্ষক"),
    CONCISE_PRO("Concise Assistant", "সংক্ষিপ্ত সহকারী")
}

enum class DetectedEmotion(val labelBn: String) {
    SAD_STRESSED("সহানুভূতিশীল"),
    HAPPY_EXCITED("উৎসাহী"),
    CURIOUS("কৌতূহলী"),
    GRATEFUL("আন্তরিক"),
    NEUTRAL("সাবলীল")
}

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val conversationId: String,
    val title: String,
    val nativeLanguage: String,
    val targetLanguage: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "conversation_messages",
    indices = [Index(value = ["conversationId"])]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val conversationId: String,
    val role: String, // "user" | "assistant" | "system"
    val content: String,
    val inputMode: String = InputMode.TEXT.name,
    val emotionTag: String = DetectedEmotion.NEUTRAL.name,
    val modelId: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val isUser: Boolean get() = role == "user"
}

package com.itamadersomajinc.elyora.ai

import com.itamadersomajinc.elyora.QwenModelCatalog
import com.itamadersomajinc.elyora.QwenModelSpec

enum class VoiceSessionPhase {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    MUTED,
    ERROR
}

data class DeviceResourceStatus(
    val totalRamGb: Int,
    val availableRamMb: Long,
    val isLowMemory: Boolean,
    val thermalStatusLabel: String,
    val isThermalThrottled: Boolean
)

data class ModelCompatibilityResult(
    val compatible: Boolean,
    val reason: String,
    val hasValidGgufFile: Boolean
)

data class ConversationState(
    val conversationId: String = ConversationRepository.DEFAULT_CONVERSATION_ID,
    val inputMode: InputMode = InputMode.TEXT,
    val messages: List<MessageEntity> = emptyList(),
    val isGenerating: Boolean = false,
    val streamingText: String = "",
    val voicePhase: VoiceSessionPhase = VoiceSessionPhase.IDLE,
    val micRmsNorm: Float = 0f,
    val liveTranscript: String = "",
    val callSeconds: Int = 0,
    val isMuted: Boolean = false,
    val autoHandsFree: Boolean = true,
    val personality: AssistantPersonality = AssistantPersonality.FRIENDLY_COMPANION,
    val activeModel: QwenModelSpec = QwenModelCatalog.models.first(),
    val resourceStatus: DeviceResourceStatus? = null,
    val errorMessage: String? = null
)

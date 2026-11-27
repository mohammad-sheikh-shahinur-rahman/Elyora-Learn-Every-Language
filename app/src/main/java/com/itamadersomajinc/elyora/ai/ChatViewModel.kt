package com.itamadersomajinc.elyora.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itamadersomajinc.elyora.OfflineBilingualEngine
import com.itamadersomajinc.elyora.QwenModelCatalog
import com.itamadersomajinc.elyora.QwenModelSpec
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Shared ViewModel backing both Voice and Text interfaces using a single
 * [ConversationRepository] and [AIInferenceService]. Switching between Voice and
 * Text never recreates the conversation or destroys shared AI state.
 */
class ChatViewModel(
    private val repository: ConversationRepository,
    private val inferenceService: AIInferenceService,
    private val engine: OfflineBilingualEngine
) : ViewModel() {

    private val _state = MutableStateFlow(ConversationState())
    val state: StateFlow<ConversationState> = _state.asStateFlow()

    private var inferenceJob: Job? = null

    init {
        viewModelScope.launch {
            repository.activeConversationId.collectLatest { convId ->
                _state.update { it.copy(conversationId = convId) }
                repository.observeMessages(convId).collectLatest { msgs ->
                    _state.update { it.copy(messages = msgs) }
                }
            }
        }
    }

    fun setInputMode(mode: InputMode) {
        _state.update { it.copy(inputMode = mode) }
    }

    fun setPersonality(personality: AssistantPersonality) {
        _state.update { it.copy(personality = personality) }
    }

    fun setActiveModel(spec: QwenModelSpec) {
        _state.update { it.copy(activeModel = spec) }
    }

    fun refreshResourceStatus() {
        val status = inferenceService.queryResourceStatus()
        _state.update { it.copy(resourceStatus = status) }
    }

    fun sendMessage(
        prompt: String,
        nativeLang: String,
        targetLang: String,
        mode: InputMode,
        onComplete: (spokenReply: String) -> Unit = {}
    ) {
        val clean = prompt.trim()
        if (clean.isEmpty()) return

        inferenceJob?.cancel()
        inferenceJob = viewModelScope.launch {
            val convId = _state.value.conversationId
            val model = _state.value.activeModel
            val emotion = OfflineBilingualEngine.detectEmotion(clean)

            repository.ensureConversation(convId, nativeLang, targetLang)
            repository.appendMessage(
                conversationId = convId,
                role = "user",
                content = clean,
                inputMode = mode,
                emotionTag = emotion,
                modelId = model.id
            )

            _state.update { it.copy(isGenerating = true, streamingText = "", errorMessage = null) }
            var finalReply = ""
            try {
                inferenceService.streamReply(
                    prompt = clean,
                    history = _state.value.messages,
                    nativeLang = nativeLang,
                    targetLang = targetLang,
                    personality = _state.value.personality,
                    activeModel = model
                ).collect { chunk ->
                    finalReply = chunk.accumulatedText
                    _state.update { it.copy(streamingText = chunk.accumulatedText) }
                }

                if (finalReply.isNotBlank()) {
                    repository.appendMessage(
                        conversationId = convId,
                        role = "assistant",
                        content = finalReply,
                        inputMode = mode,
                        emotionTag = emotion,
                        modelId = model.id
                    )
                }
                _state.update { it.copy(isGenerating = false, streamingText = "") }
                onComplete(engine.formatForSpeech(finalReply))
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isGenerating = false,
                        streamingText = "",
                        errorMessage = e.localizedMessage ?: "Inference interrupted"
                    )
                }
                onComplete("")
            }
        }
    }

    fun cancelGeneration() {
        inferenceJob?.cancel()
        inferenceService.cancelActiveInference()
        _state.update { it.copy(isGenerating = false, streamingText = "") }
    }

    fun clearConversation() {
        viewModelScope.launch {
            repository.clearConversation(_state.value.conversationId)
            _state.update { it.copy(streamingText = "", errorMessage = null) }
        }
    }
}

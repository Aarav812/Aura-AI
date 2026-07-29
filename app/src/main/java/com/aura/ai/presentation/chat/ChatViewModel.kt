package com.aura.ai.presentation.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.ai.domain.model.AiModel
import com.aura.ai.domain.model.AppPreferences
import com.aura.ai.domain.model.ChatStreamEvent
import com.aura.ai.domain.model.Feedback
import com.aura.ai.domain.model.Message
import com.aura.ai.domain.model.MessageStatus
import com.aura.ai.domain.model.Role
import com.aura.ai.domain.repository.AiRepository
import com.aura.ai.domain.repository.ChatRepository
import com.aura.ai.domain.repository.PreferencesRepository
import com.aura.ai.domain.usecase.GenerateTitleUseCase
import com.aura.ai.navigation.Routes
import com.aura.ai.utils.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatUiState(
    val chatId: String? = null,
    val title: String = "New Chat",
    val messages: List<Message> = emptyList(),
    val input: String = "",
    val model: AiModel = AiModel.default,
    val isGenerating: Boolean = false,
    val isOnline: Boolean = true,
    val error: String? = null,
    val pendingImages: List<String> = emptyList()
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val aiRepository: AiRepository,
    private val preferencesRepository: PreferencesRepository,
    private val generateTitle: GenerateTitleUseCase,
    connectivity: ConnectivityObserver
) : ViewModel() {

    private val argChatId: String = savedStateHandle[Routes.CHAT_ARG] ?: "new"

    private val _state = MutableStateFlow(ChatUiState())
    val state = _state.asStateFlow()

    private var prefs: AppPreferences = AppPreferences()
    private var streamJob: Job? = null

    init {
        preferencesRepository.preferences.onEach {
            prefs = it
            if (_state.value.chatId == null) {
                _state.update { s -> s.copy(model = AiModel.fromId(it.defaultModel)) }
            }
        }.launchIn(viewModelScope)

        connectivity.isOnline.onEach { online ->
            _state.update { it.copy(isOnline = online) }
        }.launchIn(viewModelScope)

        if (argChatId != "new" && !argChatId.startsWith("new?")) observeChat(argChatId)
    }

    private fun observeChat(chatId: String) {
        _state.update { it.copy(chatId = chatId) }
        chatRepository.observeChat(chatId).onEach { chat ->
            if (chat != null) {
                _state.update {
                    it.copy(
                        chatId = chat.id,
                        title = chat.title,
                        messages = chat.messages,
                        model = AiModel.fromId(chat.model)
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onInputChange(v: String) = _state.update { it.copy(input = v) }
    fun addImage(uri: String) = _state.update { it.copy(pendingImages = it.pendingImages + uri) }
    fun selectModel(model: AiModel) = viewModelScope.launch {
        preferencesRepository.update { it.copy(defaultModel = model.id) }
    }

    fun send() {
        val text = _state.value.input.trim()
        if (text.isBlank() || _state.value.isGenerating) return
        val images = _state.value.pendingImages
        val isFirst = _state.value.messages.none { it.role == Role.USER }
        _state.update { it.copy(input = "", pendingImages = emptyList()) }

        viewModelScope.launch {
            val chatId = ensureChat()
            val userMsg = Message(
                id = UUID.randomUUID().toString(), chatId = chatId, role = Role.USER,
                text = text, images = images, status = MessageStatus.COMPLETE
            )
            chatRepository.upsertMessage(userMsg)
            runStream(chatId, isFirst, text)
        }
    }

    fun regenerate() {
        val chatId = _state.value.chatId ?: return
        if (_state.value.isGenerating) return
        viewModelScope.launch {
            _state.value.messages.lastOrNull { it.role == Role.ASSISTANT }?.let {
                chatRepository.deleteMessage(it.id)
            }
            runStream(chatId, isFirstMessage = false, firstText = null)
        }
    }

    private suspend fun ensureChat(): String {
        _state.value.chatId?.let { return it }
        val chat = chatRepository.createChat(model = _state.value.model.id)
        observeChat(chat.id)
        return chat.id
    }

    /** Creates the assistant placeholder, streams tokens, and persists incrementally. */
    private fun runStream(chatId: String, isFirstMessage: Boolean, firstText: String?) {
        _state.update { it.copy(isGenerating = true, error = null) }
        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            val history = chatRepository.getChat(chatId)?.messages.orEmpty()
                .filter { it.role != Role.ASSISTANT || it.text.isNotBlank() }

            val assistantId = UUID.randomUUID().toString()
            chatRepository.upsertMessage(
                Message(assistantId, chatId, Role.ASSISTANT, "", status = MessageStatus.STREAMING)
            )

            val builder = StringBuilder()
            val reasoning = StringBuilder()
            val stream: Flow<ChatStreamEvent> =
                aiRepository.streamCompletion(_state.value.model.id, history, prefs)

            stream.collect { event ->
                when (event) {
                    is ChatStreamEvent.Token -> {
                        builder.append(event.delta)
                        persist(chatId, assistantId, builder.toString(), reasoning.toString(), MessageStatus.STREAMING)
                    }
                    is ChatStreamEvent.Reasoning -> reasoning.append(event.delta)
                    is ChatStreamEvent.Completed -> {
                        persist(chatId, assistantId, event.fullText.ifBlank { builder.toString() },
                            event.reasoning ?: reasoning.toString(), MessageStatus.COMPLETE, event.tokenCount)
                        _state.update { it.copy(isGenerating = false) }
                        if (isFirstMessage && firstText != null) {
                            generateTitle(chatId, _state.value.model.id, firstText)
                        }
                    }
                    is ChatStreamEvent.Failed -> {
                        persist(chatId, assistantId, builder.toString(), reasoning.toString(), MessageStatus.ERROR)
                        _state.update { it.copy(isGenerating = false, error = event.message) }
                    }
                }
            }
        }
    }

    fun stopGenerating() {
        streamJob?.cancel()
        _state.update { it.copy(isGenerating = false) }
    }

    fun setFeedback(messageId: String, feedback: Feedback) = viewModelScope.launch {
        chatRepository.setMessageFeedback(messageId, feedback)
    }

    fun deleteMessage(messageId: String) = viewModelScope.launch {
        chatRepository.deleteMessage(messageId)
    }

    private suspend fun persist(
        chatId: String, id: String, text: String, reasoning: String,
        status: MessageStatus, tokenCount: Int = 0
    ) {
        chatRepository.upsertMessage(
            Message(
                id = id, chatId = chatId, role = Role.ASSISTANT, text = text,
                reasoning = reasoning.ifBlank { null }, status = status, tokenCount = tokenCount
            )
        )
    }
}

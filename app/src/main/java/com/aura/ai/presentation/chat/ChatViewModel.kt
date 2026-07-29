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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val voiceEnabled: Boolean = true,
    val ttsEnabled: Boolean = true,
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
    private val initialPrompt: String = savedStateHandle[Routes.PROMPT_ARG] ?: ""

    private val _state = MutableStateFlow(ChatUiState(input = initialPrompt))
    val state = _state.asStateFlow()

    private var prefs: AppPreferences = AppPreferences()
    private var streamJob: Job? = null

    init {
        preferencesRepository.preferences.onEach { preferences ->
            prefs = preferences
            _state.update { current ->
                current.copy(
                    model = if (current.chatId == null) {
                        AiModel.fromId(preferences.defaultModel)
                    } else {
                        current.model
                    },
                    voiceEnabled = preferences.voiceEnabled,
                    ttsEnabled = preferences.ttsEnabled
                )
            }
        }.launchIn(viewModelScope)

        connectivity.isOnline.onEach { online ->
            _state.update { it.copy(isOnline = online) }
        }.launchIn(viewModelScope)

        if (argChatId != "new") observeChat(argChatId)
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

    fun onInputChange(v: String) = _state.update { it.copy(input = v, error = null) }
    fun addImage(uri: String) = _state.update { it.copy(pendingImages = it.pendingImages + uri) }
    fun selectModel(model: AiModel) {
        _state.update { it.copy(model = model) }
        viewModelScope.launch {
            preferencesRepository.update { it.copy(defaultModel = model.id) }
            _state.value.chatId?.let { chatRepository.updateChatModel(it, model.id) }
        }
    }

    fun send() {
        val text = _state.value.input.trim()
        if (text.isBlank() || _state.value.isGenerating) return
        if (!_state.value.isOnline) {
            _state.update { it.copy(error = "You're offline. Reconnect to send a message.") }
            return
        }
        val images = _state.value.pendingImages
        val isFirst = _state.value.messages.none { it.role == Role.USER }
        _state.update { it.copy(input = "", pendingImages = emptyList()) }

        viewModelScope.launch {
            try {
                val chatId = ensureChat()
                val userMsg = Message(
                    id = UUID.randomUUID().toString(), chatId = chatId, role = Role.USER,
                    text = text, images = images, status = MessageStatus.COMPLETE
                )
                chatRepository.upsertMessage(userMsg)
                runStream(chatId, isFirst, text)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.update { current ->
                    current.copy(
                        input = current.input.ifBlank { text },
                        pendingImages = if (current.pendingImages.isEmpty()) images else current.pendingImages,
                        error = error.message ?: "Could not save the message"
                    )
                }
            }
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
            val assistantId = UUID.randomUUID().toString()
            val builder = StringBuilder()
            val reasoning = StringBuilder()
            var placeholderCreated = false

            try {
                val history = chatRepository.getChat(chatId)?.messages.orEmpty()
                    .filter { it.role != Role.ASSISTANT || it.text.isNotBlank() }
                chatRepository.upsertMessage(
                    Message(assistantId, chatId, Role.ASSISTANT, "", status = MessageStatus.STREAMING)
                )
                placeholderCreated = true

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
                            if (isFirstMessage && firstText != null) {
                                generateTitle(chatId, _state.value.model.id, firstText)
                            }
                        }
                        is ChatStreamEvent.Failed -> {
                            persist(chatId, assistantId, builder.toString(), reasoning.toString(), MessageStatus.ERROR)
                            _state.update { it.copy(error = event.message) }
                        }
                    }
                }
            } catch (cancelled: CancellationException) {
                // Do not leave a permanently "streaming" placeholder after the user taps Stop.
                if (placeholderCreated) {
                    withContext(NonCancellable) {
                        runCatching {
                            if (builder.isBlank() && reasoning.isBlank()) {
                                chatRepository.deleteMessage(assistantId)
                            } else {
                                persist(chatId, assistantId, builder.toString(), reasoning.toString(), MessageStatus.COMPLETE)
                            }
                        }
                    }
                }
                throw cancelled
            } catch (error: Exception) {
                if (placeholderCreated) {
                    withContext(NonCancellable) {
                        runCatching {
                            persist(chatId, assistantId, builder.toString(), reasoning.toString(), MessageStatus.ERROR)
                        }
                    }
                }
                _state.update { it.copy(error = error.message ?: "Could not generate a response") }
            } finally {
                _state.update { it.copy(isGenerating = false) }
            }
        }
    }

    fun stopGenerating() {
        // The stream's cancellation cleanup updates state after fixing the placeholder message.
        streamJob?.cancel()
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

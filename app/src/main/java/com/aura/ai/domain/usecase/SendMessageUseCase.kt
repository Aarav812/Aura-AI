package com.aura.ai.domain.usecase

import com.aura.ai.domain.model.AppPreferences
import com.aura.ai.domain.model.ChatStreamEvent
import com.aura.ai.domain.model.Message
import com.aura.ai.domain.model.MessageStatus
import com.aura.ai.domain.model.Role
import com.aura.ai.domain.repository.AiRepository
import com.aura.ai.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

/**
 * Orchestrates sending a user message and streaming the assistant reply:
 *  1. persist the user message
 *  2. create a placeholder assistant message (STREAMING)
 *  3. stream tokens (caller updates UI + persists on completion)
 */
class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val aiRepository: AiRepository
) {
    data class Prepared(val userMessage: Message, val assistantId: String, val stream: Flow<ChatStreamEvent>)

    suspend operator fun invoke(
        chatId: String,
        model: String,
        userText: String,
        prefs: AppPreferences,
        images: List<String> = emptyList()
    ): Prepared {
        val now = System.currentTimeMillis()
        val userMessage = Message(
            id = UUID.randomUUID().toString(),
            chatId = chatId,
            role = Role.USER,
            text = userText,
            images = images,
            timestamp = now,
            status = MessageStatus.COMPLETE
        )
        chatRepository.upsertMessage(userMessage)

        val history = chatRepository.getChat(chatId)?.messages ?: listOf(userMessage)
        val assistantId = UUID.randomUUID().toString()
        val placeholder = Message(
            id = assistantId,
            chatId = chatId,
            role = Role.ASSISTANT,
            text = "",
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.STREAMING
        )
        chatRepository.upsertMessage(placeholder)

        val stream = aiRepository.streamCompletion(model, history, prefs)
        return Prepared(userMessage, assistantId, stream)
    }
}

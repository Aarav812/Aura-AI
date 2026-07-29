package com.aura.ai.domain.usecase

import com.aura.ai.domain.repository.AiRepository
import com.aura.ai.domain.repository.ChatRepository
import javax.inject.Inject

class GenerateTitleUseCase @Inject constructor(
    private val aiRepository: AiRepository,
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(chatId: String, model: String, firstUserMessage: String) {
        val title = aiRepository.generateTitle(model, firstUserMessage)
        chatRepository.updateChatTitle(chatId, title)
    }
}

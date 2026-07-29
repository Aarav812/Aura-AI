package com.aura.ai.domain.usecase

import com.aura.ai.domain.model.Chat
import com.aura.ai.domain.model.Message
import com.aura.ai.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class SearchResults(val chats: List<Chat>, val messages: List<Message>)

class GlobalSearchUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(query: String): Flow<SearchResults> =
        combine(
            chatRepository.searchChats(query),
            chatRepository.searchMessages(query)
        ) { chats, messages -> SearchResults(chats, messages) }
}

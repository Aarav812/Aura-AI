package com.aura.ai.domain.repository

import com.aura.ai.domain.model.Chat
import com.aura.ai.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeChats(includeArchived: Boolean = false): Flow<List<Chat>>
    fun observePinnedChats(): Flow<List<Chat>>
    fun observeChat(chatId: String): Flow<Chat?>
    fun searchChats(query: String): Flow<List<Chat>>
    fun searchMessages(query: String): Flow<List<Message>>

    suspend fun createChat(model: String, title: String = "New Chat"): Chat
    suspend fun getChat(chatId: String): Chat?
    suspend fun upsertMessage(message: Message)
    suspend fun updateChatTitle(chatId: String, title: String)
    suspend fun renameChat(chatId: String, title: String)
    suspend fun setPinned(chatId: String, pinned: Boolean)
    suspend fun setFavorite(chatId: String, favorite: Boolean)
    suspend fun setArchived(chatId: String, archived: Boolean)
    suspend fun deleteChat(chatId: String)
    suspend fun duplicateChat(chatId: String): Chat?
    suspend fun setMessageFeedback(messageId: String, feedback: com.aura.ai.domain.model.Feedback)
    suspend fun deleteMessage(messageId: String)
}

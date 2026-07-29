package com.aura.ai.data.repository

import com.aura.ai.core.common.DispatcherProvider
import com.aura.ai.data.local.dao.ChatDao
import com.aura.ai.data.mapper.toDomain
import com.aura.ai.data.mapper.toEntity
import com.aura.ai.domain.model.Chat
import com.aura.ai.domain.model.Feedback
import com.aura.ai.domain.model.Message
import com.aura.ai.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val dao: ChatDao,
    private val dispatchers: DispatcherProvider
) : ChatRepository {

    override fun observeChats(includeArchived: Boolean): Flow<List<Chat>> =
        dao.observeChats(includeArchived).map { list -> list.map { it.toDomain() } }

    override fun observePinnedChats(): Flow<List<Chat>> =
        dao.observePinned().map { list -> list.map { it.toDomain() } }

    override fun observeChat(chatId: String): Flow<Chat?> =
        dao.observeChat(chatId).map { it?.toDomain() }

    override fun searchChats(query: String): Flow<List<Chat>> =
        dao.searchChats(query.escapeLikePattern()).map { list -> list.map { it.toDomain() } }

    override fun searchMessages(query: String): Flow<List<Message>> =
        dao.searchMessages(query.escapeLikePattern()).map { list -> list.map { it.toDomain() } }

    override suspend fun createChat(model: String, title: String): Chat = withContext(dispatchers.io) {
        val now = System.currentTimeMillis()
        val chat = Chat(
            id = UUID.randomUUID().toString(),
            title = title, createdAt = now, updatedAt = now, model = model
        )
        dao.upsertChat(chat.toEntity())
        chat
    }

    override suspend fun getChat(chatId: String): Chat? = withContext(dispatchers.io) {
        dao.getChat(chatId)?.toDomain()
    }

    override suspend fun upsertMessage(message: Message) = withContext(dispatchers.io) {
        dao.upsertMessage(message.toEntity())
        dao.touch(message.chatId, System.currentTimeMillis())
    }

    override suspend fun updateChatTitle(chatId: String, title: String) = withContext(dispatchers.io) {
        dao.updateTitle(chatId, title, System.currentTimeMillis())
    }

    override suspend fun updateChatModel(chatId: String, model: String) = withContext(dispatchers.io) {
        dao.updateModel(chatId, model, System.currentTimeMillis())
    }

    override suspend fun renameChat(chatId: String, title: String) = updateChatTitle(chatId, title)

    override suspend fun setPinned(chatId: String, pinned: Boolean) = withContext(dispatchers.io) {
        dao.setPinned(chatId, pinned)
    }

    override suspend fun setFavorite(chatId: String, favorite: Boolean) = withContext(dispatchers.io) {
        dao.setFavorite(chatId, favorite)
    }

    override suspend fun setArchived(chatId: String, archived: Boolean) = withContext(dispatchers.io) {
        dao.setArchived(chatId, archived)
    }

    override suspend fun deleteChat(chatId: String) = withContext(dispatchers.io) {
        dao.deleteChat(chatId)
    }

    override suspend fun duplicateChat(chatId: String): Chat? = withContext(dispatchers.io) {
        val original = dao.getChat(chatId)?.toDomain() ?: return@withContext null
        val now = System.currentTimeMillis()
        val newId = UUID.randomUUID().toString()
        val copiedMessages = original.messages.map { msg ->
            msg.copy(id = UUID.randomUUID().toString(), chatId = newId)
        }
        val copy = original.copy(
            id = newId,
            title = "${original.title} (copy)",
            createdAt = now,
            updatedAt = now,
            pinned = false,
            archived = false,
            messages = copiedMessages
        )
        dao.upsertChat(copy.toEntity())
        copiedMessages.forEach { dao.upsertMessage(it.toEntity()) }
        copy
    }

    override suspend fun setMessageFeedback(messageId: String, feedback: Feedback) = withContext(dispatchers.io) {
        dao.setFeedback(messageId, feedback.name)
    }

    override suspend fun deleteMessage(messageId: String) = withContext(dispatchers.io) {
        dao.deleteMessage(messageId)
    }

    private fun String.escapeLikePattern(): String =
        replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
}

package com.aura.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.aura.ai.data.local.entity.ChatEntity
import com.aura.ai.data.local.entity.ChatWithMessages
import com.aura.ai.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Transaction
    @Query("SELECT * FROM chats WHERE (:includeArchived = 1 OR archived = 0) ORDER BY pinned DESC, updatedAt DESC")
    fun observeChats(includeArchived: Boolean): Flow<List<ChatWithMessages>>

    @Transaction
    @Query("SELECT * FROM chats WHERE pinned = 1 AND archived = 0 ORDER BY updatedAt DESC")
    fun observePinned(): Flow<List<ChatWithMessages>>

    @Transaction
    @Query("SELECT * FROM chats WHERE id = :chatId")
    fun observeChat(chatId: String): Flow<ChatWithMessages?>

    @Transaction
    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChat(chatId: String): ChatWithMessages?

    @Transaction
    @Query("SELECT * FROM chats WHERE title LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchChats(query: String): Flow<List<ChatWithMessages>>

    @Query("SELECT * FROM messages WHERE text LIKE '%' || :query || '%' ORDER BY timestamp DESC LIMIT 100")
    fun searchMessages(query: String): Flow<List<MessageEntity>>

    @Upsert
    suspend fun upsertChat(chat: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessage(message: MessageEntity)

    @Query("UPDATE chats SET title = :title, updatedAt = :now WHERE id = :chatId")
    suspend fun updateTitle(chatId: String, title: String, now: Long)

    @Query("UPDATE chats SET pinned = :pinned WHERE id = :chatId")
    suspend fun setPinned(chatId: String, pinned: Boolean)

    @Query("UPDATE chats SET favorite = :favorite WHERE id = :chatId")
    suspend fun setFavorite(chatId: String, favorite: Boolean)

    @Query("UPDATE chats SET archived = :archived WHERE id = :chatId")
    suspend fun setArchived(chatId: String, archived: Boolean)

    @Query("UPDATE chats SET updatedAt = :now WHERE id = :chatId")
    suspend fun touch(chatId: String, now: Long)

    @Query("UPDATE messages SET feedback = :feedback WHERE id = :messageId")
    suspend fun setFeedback(messageId: String, feedback: String)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChat(chatId: String)
}

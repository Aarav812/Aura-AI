package com.aura.ai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chatId")]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val role: String,
    val text: String,
    val markdown: Boolean,
    val images: String,        // JSON-encoded List<String>
    val attachments: String,   // JSON-encoded List<Attachment>
    val timestamp: Long,
    val status: String,
    val tokenCount: Int,
    val feedback: String,
    val reasoning: String?
)

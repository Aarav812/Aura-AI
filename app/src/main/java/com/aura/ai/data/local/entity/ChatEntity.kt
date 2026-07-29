package com.aura.ai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val model: String,
    val pinned: Boolean = false,
    val favorite: Boolean = false,
    val archived: Boolean = false
)

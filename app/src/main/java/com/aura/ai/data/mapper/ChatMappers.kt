package com.aura.ai.data.mapper

import com.aura.ai.data.local.entity.ChatEntity
import com.aura.ai.data.local.entity.ChatWithMessages
import com.aura.ai.data.local.entity.MessageEntity
import com.aura.ai.domain.model.Attachment
import com.aura.ai.domain.model.AttachmentType
import com.aura.ai.domain.model.Chat
import com.aura.ai.domain.model.Feedback
import com.aura.ai.domain.model.Message
import com.aura.ai.domain.model.MessageStatus
import com.aura.ai.domain.model.Role
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

@Serializable
private data class AttachmentDto(
    val id: String, val type: String, val uri: String, val name: String, val sizeBytes: Long
)

fun ChatWithMessages.toDomain(): Chat = Chat(
    id = chat.id,
    title = chat.title,
    createdAt = chat.createdAt,
    updatedAt = chat.updatedAt,
    model = chat.model,
    pinned = chat.pinned,
    favorite = chat.favorite,
    archived = chat.archived,
    messages = messages.sortedBy { it.timestamp }.map { it.toDomain() }
)

fun Chat.toEntity(): ChatEntity = ChatEntity(
    id = id, title = title, createdAt = createdAt, updatedAt = updatedAt,
    model = model, pinned = pinned, favorite = favorite, archived = archived
)

fun MessageEntity.toDomain(): Message = Message(
    id = id,
    chatId = chatId,
    role = Role.valueOf(role),
    text = text,
    markdown = markdown,
    images = runCatching { json.decodeFromString(ListSerializer(String.serializer()), images) }.getOrDefault(emptyList()),
    timestamp = timestamp,
    attachments = runCatching {
        json.decodeFromString(ListSerializer(AttachmentDto.serializer()), attachments)
            .map { Attachment(it.id, AttachmentType.valueOf(it.type), it.uri, it.name, it.sizeBytes) }
    }.getOrDefault(emptyList()),
    status = MessageStatus.valueOf(status),
    tokenCount = tokenCount,
    feedback = Feedback.valueOf(feedback),
    reasoning = reasoning
)

fun Message.toEntity(): MessageEntity = MessageEntity(
    id = id,
    chatId = chatId,
    role = role.name,
    text = text,
    markdown = markdown,
    images = json.encodeToString(ListSerializer(String.serializer()), images),
    attachments = json.encodeToString(
        ListSerializer(AttachmentDto.serializer()),
        attachments.map { AttachmentDto(it.id, it.type.name, it.uri, it.name, it.sizeBytes) }
    ),
    timestamp = timestamp,
    status = status.name,
    tokenCount = tokenCount,
    feedback = feedback.name,
    reasoning = reasoning
)

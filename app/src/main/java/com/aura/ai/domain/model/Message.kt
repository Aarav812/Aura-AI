package com.aura.ai.domain.model

enum class Role { USER, ASSISTANT, SYSTEM }

enum class MessageStatus { SENDING, STREAMING, COMPLETE, ERROR, QUEUED }

enum class Feedback { NONE, LIKED, DISLIKED }

data class Attachment(
    val id: String,
    val type: AttachmentType,
    val uri: String,
    val name: String,
    val sizeBytes: Long = 0
)

enum class AttachmentType { IMAGE, PDF, WORD, EXCEL, AUDIO, VIDEO, OTHER }

data class Message(
    val id: String,
    val chatId: String,
    val role: Role,
    val text: String,
    val markdown: Boolean = true,
    val images: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val attachments: List<Attachment> = emptyList(),
    val status: MessageStatus = MessageStatus.COMPLETE,
    val tokenCount: Int = 0,
    val feedback: Feedback = Feedback.NONE,
    val reasoning: String? = null
)

package com.aura.ai.domain.model

/** A conversation thread. Pure domain model — no framework dependencies. */
data class Chat(
    val id: String,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val model: String,
    val pinned: Boolean = false,
    val favorite: Boolean = false,
    val archived: Boolean = false,
    val messages: List<Message> = emptyList()
) {
    val preview: String
        get() = messages.lastOrNull { it.role == Role.ASSISTANT || it.role == Role.USER }
            ?.text?.take(120).orEmpty()
}

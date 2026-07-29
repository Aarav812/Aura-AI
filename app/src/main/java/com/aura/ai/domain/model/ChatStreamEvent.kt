package com.aura.ai.domain.model

/** Events emitted while streaming an assistant response. */
sealed interface ChatStreamEvent {
    data class Token(val delta: String) : ChatStreamEvent
    data class Reasoning(val delta: String) : ChatStreamEvent
    data class Completed(val fullText: String, val reasoning: String?, val tokenCount: Int) : ChatStreamEvent
    data class Failed(val message: String) : ChatStreamEvent
}

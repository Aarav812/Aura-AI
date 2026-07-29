package com.aura.ai.domain.repository

import com.aura.ai.domain.model.ChatStreamEvent
import com.aura.ai.domain.model.Message
import com.aura.ai.domain.model.AppPreferences
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    /**
     * Streams an assistant reply for [history] using [model].
     * Cancellation of the collecting coroutine cancels the network call.
     */
    fun streamCompletion(
        model: String,
        history: List<Message>,
        prefs: AppPreferences
    ): Flow<ChatStreamEvent>

    /** One-shot (non-streaming) completion, used for auto-title generation. */
    suspend fun complete(model: String, history: List<Message>, prefs: AppPreferences): Result<String>

    /** Generates a short title from the first exchange. */
    suspend fun generateTitle(model: String, firstUserMessage: String): String
}

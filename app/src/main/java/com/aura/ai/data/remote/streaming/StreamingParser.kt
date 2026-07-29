package com.aura.ai.data.remote.streaming

import com.aura.ai.data.remote.dto.ChatCompletionChunk
import com.aura.ai.domain.model.ChatStreamEvent
import com.aura.ai.utils.TokenCounter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody

/**
 * Parses an OpenAI-compatible Server-Sent Events (SSE) stream into [ChatStreamEvent]s.
 *
 * The stream is a sequence of lines like:
 *   data: {"choices":[{"delta":{"content":"Hel"}}]}
 *   data: {"choices":[{"delta":{"content":"lo"}}]}
 *   data: [DONE]
 */
class StreamingParser(
    private val json: Json = Json { ignoreUnknownKeys = true; isLenient = true }
) {
    fun parse(body: ResponseBody): Flow<ChatStreamEvent> = flow {
        val builder = StringBuilder()
        val reasoning = StringBuilder()
        body.source().use { source ->
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break
                if (line.isBlank()) continue
                if (!line.startsWith("data:")) continue

                val payload = line.removePrefix("data:").trim()
                if (payload == "[DONE]") break

                val chunk = runCatching {
                    json.decodeFromString<ChatCompletionChunk>(payload)
                }.getOrNull() ?: continue

                val delta = chunk.choices.firstOrNull()?.delta ?: continue

                delta.reasoningContent?.let {
                    if (it.isNotEmpty()) {
                        reasoning.append(it)
                        emit(ChatStreamEvent.Reasoning(it))
                    }
                }
                delta.content?.let {
                    if (it.isNotEmpty()) {
                        builder.append(it)
                        emit(ChatStreamEvent.Token(it))
                    }
                }
            }
        }
        val full = builder.toString()
        emit(
            ChatStreamEvent.Completed(
                fullText = full,
                reasoning = reasoning.toString().ifBlank { null },
                tokenCount = TokenCounter.estimate(full)
            )
        )
    }
}

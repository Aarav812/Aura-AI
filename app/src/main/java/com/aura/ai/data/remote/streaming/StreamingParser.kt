package com.aura.ai.data.remote.streaming

import com.aura.ai.data.remote.dto.ChatCompletionChunk
import com.aura.ai.domain.model.ChatStreamEvent
import com.aura.ai.utils.TokenCounter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
        var completedNormally = false
        body.source().use { source ->
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: break
                if (line.isBlank()) continue
                if (!line.startsWith("data:")) continue

                val payload = line.removePrefix("data:").trim()
                if (payload == "[DONE]") {
                    completedNormally = true
                    break
                }

                val apiError = runCatching {
                    json.parseToJsonElement(payload).jsonObject["error"]
                        ?.jsonObject?.get("message")?.jsonPrimitive?.contentOrNull
                }.getOrNull()
                if (!apiError.isNullOrBlank()) {
                    emit(ChatStreamEvent.Failed(apiError))
                    return@flow
                }

                val chunk = runCatching {
                    json.decodeFromString<ChatCompletionChunk>(payload)
                }.getOrNull() ?: continue

                val choice = chunk.choices.firstOrNull() ?: continue
                if (choice.finishReason != null) completedNormally = true
                val delta = choice.delta

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
        if (!completedNormally) {
            emit(ChatStreamEvent.Failed("The response stream ended unexpectedly."))
            return@flow
        }
        val full = builder.toString()
        if (full.isBlank()) {
            emit(ChatStreamEvent.Failed("Empty response from server"))
            return@flow
        }
        emit(
            ChatStreamEvent.Completed(
                fullText = full,
                reasoning = reasoning.toString().ifBlank { null },
                tokenCount = TokenCounter.estimate(full)
            )
        )
    }
}

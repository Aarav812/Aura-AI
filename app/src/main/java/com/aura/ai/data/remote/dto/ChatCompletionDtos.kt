package com.aura.ai.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * NVIDIA NIM uses the OpenAI-compatible /v1/chat/completions schema.
 * https://docs.api.nvidia.com/nim/reference/
 */
@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessageDto>,
    val temperature: Float = 0.7f,
    @SerialName("top_p") val topP: Float = 0.95f,
    @SerialName("max_tokens") val maxTokens: Int = 2048,
    val stream: Boolean = true
)

@Serializable
data class ChatMessageDto(
    val role: String,
    val content: String
)

// ---- Non-streaming response ----
@Serializable
data class ChatCompletionResponse(
    val id: String? = null,
    val choices: List<Choice> = emptyList(),
    val usage: Usage? = null
) {
    @Serializable
    data class Choice(
        val index: Int = 0,
        val message: ChatMessageDto? = null,
        @SerialName("finish_reason") val finishReason: String? = null
    )
    @Serializable
    data class Usage(
        @SerialName("prompt_tokens") val promptTokens: Int = 0,
        @SerialName("completion_tokens") val completionTokens: Int = 0,
        @SerialName("total_tokens") val totalTokens: Int = 0
    )
}

// ---- Streaming chunk (SSE `data:` payload) ----
@Serializable
data class ChatCompletionChunk(
    val id: String? = null,
    val choices: List<ChunkChoice> = emptyList()
) {
    @Serializable
    data class ChunkChoice(
        val index: Int = 0,
        val delta: Delta = Delta(),
        @SerialName("finish_reason") val finishReason: String? = null
    )
    @Serializable
    data class Delta(
        val role: String? = null,
        val content: String? = null,
        @SerialName("reasoning_content") val reasoningContent: String? = null
    )
}

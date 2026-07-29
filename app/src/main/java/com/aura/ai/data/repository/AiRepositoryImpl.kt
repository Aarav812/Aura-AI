package com.aura.ai.data.repository

import com.aura.ai.core.common.DispatcherProvider
import com.aura.ai.data.remote.NvidiaApiService
import com.aura.ai.data.remote.PromptManager
import com.aura.ai.data.remote.dto.ChatCompletionRequest
import com.aura.ai.data.remote.streaming.StreamingParser
import com.aura.ai.domain.model.AiModel
import com.aura.ai.domain.model.AppPreferences
import com.aura.ai.domain.model.ChatStreamEvent
import com.aura.ai.domain.model.Message
import com.aura.ai.domain.repository.AiRepository
import com.aura.ai.utils.RateLimiter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiRepositoryImpl @Inject constructor(
    private val api: NvidiaApiService,
    private val promptManager: PromptManager,
    private val streamingParser: StreamingParser,
    private val rateLimiter: RateLimiter,
    private val dispatchers: DispatcherProvider
) : AiRepository {

    override fun streamCompletion(
        model: String,
        history: List<Message>,
        prefs: AppPreferences
    ): Flow<ChatStreamEvent> = flow {
        if (!rateLimiter.tryAcquire()) {
            val wait = rateLimiter.retryAfterSeconds()
            emit(ChatStreamEvent.Failed("Rate limit reached. Try again in ${wait}s."))
            return@flow
        }
        val aiModel = AiModel.fromId(model)
        val request = ChatCompletionRequest(
            model = model,
            messages = promptManager.buildMessages(aiModel, history, prefs),
            temperature = prefs.temperature,
            topP = prefs.topP,
            maxTokens = prefs.maxTokens,
            stream = true
        )
        val response = api.streamCompletion(request)
        if (!response.isSuccessful) {
            emit(ChatStreamEvent.Failed("Request failed (${response.code()})"))
            return@flow
        }
        val body = response.body()
        if (body == null) {
            emit(ChatStreamEvent.Failed("Empty response from server"))
            return@flow
        }
        emitAll(streamingParser.parse(body))
    }.catch { e ->
        emit(ChatStreamEvent.Failed(e.message ?: "Streaming error"))
    }.flowOn(dispatchers.io)

    override suspend fun complete(
        model: String,
        history: List<Message>,
        prefs: AppPreferences
    ): Result<String> = runCatching {
        val aiModel = AiModel.fromId(model)
        val request = ChatCompletionRequest(
            model = model,
            messages = promptManager.buildMessages(aiModel, history, prefs),
            temperature = prefs.temperature,
            topP = prefs.topP,
            maxTokens = prefs.maxTokens,
            stream = false
        )
        api.createCompletion(request).choices.firstOrNull()?.message?.content.orEmpty()
    }

    override suspend fun generateTitle(model: String, firstUserMessage: String): String {
        return runCatching {
            val request = ChatCompletionRequest(
                model = model,
                messages = promptManager.titlePrompt(firstUserMessage),
                temperature = 0.3f,
                maxTokens = 20,
                stream = false
            )
            api.createCompletion(request).choices.firstOrNull()?.message?.content
                ?.trim()?.trim('"', '.', '\n')?.take(60)
                ?.takeIf { it.isNotBlank() }
                ?: firstUserMessage.take(40)
        }.getOrElse { firstUserMessage.take(40) }
    }
}

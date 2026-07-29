package com.aura.ai.data.remote

import com.aura.ai.data.remote.dto.ChatCompletionRequest
import com.aura.ai.data.remote.dto.ChatCompletionResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Streaming

/**
 * Reusable NVIDIA NIM API surface. Auth is added by an OkHttp interceptor,
 * so the key never appears here.
 */
interface NvidiaApiService {

    /** Non-streaming completion. */
    @POST("v1/chat/completions")
    suspend fun createCompletion(@Body request: ChatCompletionRequest): ChatCompletionResponse

    /** Streaming completion — returns a raw body we parse as Server-Sent Events. */
    @Streaming
    @Headers("Accept: text/event-stream")
    @POST("v1/chat/completions")
    suspend fun streamCompletion(@Body request: ChatCompletionRequest): Response<ResponseBody>
}

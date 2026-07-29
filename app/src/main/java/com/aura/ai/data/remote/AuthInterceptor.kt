package com.aura.ai.data.remote

import com.aura.ai.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/** Injects the NVIDIA bearer token from BuildConfig (sourced from local.properties). */
@Singleton
class AuthInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val key = BuildConfig.NVIDIA_API_KEY
        val request = chain.request().newBuilder()
            .apply { if (key.isNotBlank()) addHeader("Authorization", "Bearer $key") }
            .addHeader("Content-Type", "application/json")
            .build()
        return chain.proceed(request)
    }
}

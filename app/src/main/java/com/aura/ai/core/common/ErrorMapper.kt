package com.aura.ai.core.common

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** Maps raw exceptions from any layer into a stable [AppError] taxonomy. */
object ErrorMapper {
    fun map(throwable: Throwable): AppError = when (throwable) {
        is CancellationException -> AppError.Cancelled()
        is SocketTimeoutException -> AppError.Timeout()
        is UnknownHostException, is IOException -> AppError.Network()
        is HttpException -> mapHttp(throwable.code(), throwable.message())
        else -> AppError.Unknown(throwable.message ?: "Unexpected error", throwable)
    }

    fun mapHttp(code: Int, message: String?): AppError = when (code) {
        401, 403 -> AppError.Unauthorized()
        408 -> AppError.Timeout()
        429 -> AppError.RateLimited()
        in 500..599 -> AppError.Server(code, message ?: "Server error ($code)")
        else -> AppError.Api(message ?: "Request failed ($code)")
    }
}

package com.aura.ai.core.common

/**
 * Domain-level error taxonomy. Network/SDK exceptions are mapped into these
 * by [ErrorMapper] so the UI never deals with raw exceptions.
 */
sealed class AppError(open val message: String, open val cause: Throwable? = null) {
    data class Network(override val message: String = "No internet connection") : AppError(message)
    data class Timeout(override val message: String = "The request timed out") : AppError(message)
    data class RateLimited(val retryAfterSeconds: Long? = null) :
        AppError("You're sending messages too quickly. Please slow down.")
    data class Unauthorized(override val message: String = "Session expired. Please sign in again.") : AppError(message)
    data class Server(val code: Int, override val message: String) : AppError(message)
    data class Api(override val message: String) : AppError(message)
    data class Auth(override val message: String) : AppError(message)
    data class Cancelled(override val message: String = "Generation stopped") : AppError(message)
    data class Unknown(override val message: String = "Something went wrong", override val cause: Throwable? = null) :
        AppError(message, cause)
}

package com.aura.ai.core.common

/**
 * A discriminated union that encapsulates a successful outcome with a value of type [T]
 * or a failure with an [AppError]. Preferred over throwing across layer boundaries.
 */
sealed interface Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val error: AppError) : Resource<Nothing>
    data object Loading : Resource<Nothing>
}

inline fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<R> = when (this) {
    is Resource.Success -> Resource.Success(transform(data))
    is Resource.Error -> this
    Resource.Loading -> Resource.Loading
}

inline fun <T> Resource<T>.onSuccess(action: (T) -> Unit): Resource<T> {
    if (this is Resource.Success) action(data)
    return this
}

inline fun <T> Resource<T>.onError(action: (AppError) -> Unit): Resource<T> {
    if (this is Resource.Error) action(error)
    return this
}

fun <T> Resource<T>.getOrNull(): T? = (this as? Resource.Success)?.data

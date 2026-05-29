package com.example.cuan.core.utils

/**
 * Extension functions for Result<T> type
 */

/**
 * Executes the given block if the Result is successful
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (isSuccess) {
        action(getOrThrow())
    }
    return this
}

/**
 * Executes the given block if the Result is a failure
 */
inline fun <T> Result<T>.onFailure(action: (Throwable) -> Unit): Result<T> {
    exceptionOrNull()?.let(action)
    return this
}

/**
 * Maps the success value or returns null on failure
 */
inline fun <T, R> Result<T>.mapOrNull(transform: (T) -> R): R? {
    return getOrNull()?.let(transform)
}

/**
 * Returns the success value or the default value on failure
 */
fun <T> Result<T>.getOrElse(default: T): T {
    return getOrNull() ?: default
}

/**
 * Fold the Result into a single value
 */
inline fun <T, R> Result<T>.fold(
    onSuccess: (T) -> R,
    onFailure: (Throwable) -> R
): R {
    return when (val exception = exceptionOrNull()) {
        null -> onSuccess(getOrThrow())
        else -> onFailure(exception)
    }
}
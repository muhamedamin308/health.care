package core.functional

import core.utils.DomainError

sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure(val errorMessage: DomainError) : Result<Nothing>()


    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(value))
        is Failure -> this
    }

    inline fun <R> faltMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(value)
        is Failure -> this
    }

    inline fun <R> fold(
        onSuccess: (T) -> R,
        onFailure: (DomainError) -> R,
    ): R = when (this) {
        is Success -> onSuccess(value)
        is Failure -> onFailure(errorMessage)
    }

    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Failure -> null
    }

    inline fun getOrElse(default: () -> @UnsafeVariance T): T = when (this) {
        is Success -> value
        is Failure -> default()
    }

    companion object {
        fun <T> success(value: T): Result<T> = Success(value)
        fun <T> failure(errorMessage: DomainError): Result<T> = Failure(errorMessage)
    }
}

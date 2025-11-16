package presentation.state

import core.utils.DomainError

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: DomainError) : UiState<Nothing>()


    inline fun <R> fold(
        onIdle: () -> R,
        onLoading: () -> R,
        onSuccess: (T) -> R,
        onError: (DomainError) -> R,
    ): R = when (this) {
        is Idle -> onIdle()
        is Loading -> onLoading()
        is Success -> onSuccess(data)
        is Error -> onError(message)
    }

    fun isLoading(): Boolean = this is Loading
    fun isError(): Boolean = this is Error
    fun isSuccess(): Boolean = this is Success

    fun dataOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }
}
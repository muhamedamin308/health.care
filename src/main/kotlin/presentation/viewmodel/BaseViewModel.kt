package presentation.viewmodel

import core.functional.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import presentation.state.UiEvent
import presentation.state.UiState

abstract class BaseViewModel {
    protected val viewModelScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    protected val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    protected suspend fun <T> Result<T>.toUiState(): UiState<T> = when (this) {
        is Result.Success -> UiState.Success(value)
        is Result.Failure -> UiState.Error(errorMessage)
    }

    protected suspend fun emitEvent(event: UiEvent) {
        _uiEvents.emit(event)
    }

    fun onCleared() {
        viewModelScope.cancel()
    }
}
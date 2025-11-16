package presentation.state

import core.utils.DomainError

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
    object NavigateBack : UiEvent()
    data class ShowError(val message: DomainError) : UiEvent()
}
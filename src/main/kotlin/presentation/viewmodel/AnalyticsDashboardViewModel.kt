package presentation.viewmodel

import domain.usecases.implementation.GetHospitalStatisticsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import presentation.state.AnalyticsDashboardState

class AnalyticsDashboardViewModel(
    private val getStatistics: GetHospitalStatisticsUseCase,
) : BaseViewModel() {
    private val _state = MutableStateFlow(AnalyticsDashboardState())
    val state = _state.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            getStatistics().fold(
                onSuccess = { stats ->
                    _state.value = AnalyticsDashboardState(
                        totalPatients = stats.totalPatients,
                        appointmentsToday = stats.totalAppointmentsToday,
                        completedToday = stats.completedAppointmentsToday,
                        pendingAppointments = stats.pendingAppointments,
                        totalRevenue = stats.totalRevenue,
                        outstandingPayments = stats.outstandingPayments,
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _state.value = AnalyticsDashboardState(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    fun refresh() = loadStatistics()
}
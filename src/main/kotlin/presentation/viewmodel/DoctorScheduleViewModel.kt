package presentation.viewmodel

import core.utils.AppointmentId
import core.utils.DoctorId
import data.repoistories.AppointmentRepository
import data.repoistories.DoctorRepository
import domain.models.AppointmentStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import presentation.state.DoctorScheduleState
import presentation.state.UiEvent
import java.time.LocalDateTime

@Suppress("IDENTITY_SENSITIVE_OPERATIONS_WITH_VALUE_TYPE")
class DoctorScheduleViewModel(
    private val doctorId: DoctorId,
    private val doctorRepository: DoctorRepository,
    private val appointmentRepository: AppointmentRepository,
) : BaseViewModel() {
    private val _state = MutableStateFlow(DoctorScheduleState())
    val state = _state.asStateFlow()

    init {
        loadSchedule()
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val doctorResult = doctorRepository.findById(doctorId)
            val appointmentResult = appointmentRepository.findByDoctorId(doctorId)

            val doctor = doctorResult.getOrNull()
            val allAppointments = appointmentResult.getOrNull().orEmpty()

            val today = LocalDateTime.now().toLocalDate()
            val todayAppointments = allAppointments.filter {
                it.scheduledTime.toLocalDate() == today &&
                        it.status !in listOf(
                    AppointmentStatus.CANCELLED,
                    AppointmentStatus.COMPLETED
                )
            }.sortedBy { it.scheduledTime }

            val now = LocalDateTime.now()
            val currentAppointments = todayAppointments.firstOrNull {
                it.status == AppointmentStatus.IN_PROGRESS ||
                        (it.status == AppointmentStatus.CONFIRMED && it.scheduledTime.isBefore(now.plusMinutes(30)))
            }

            _state.value = DoctorScheduleState(
                doctor = doctor,
                todayAppointments = todayAppointments,
                currentAppointment = currentAppointments,
                isLoading = false
            )
        }
    }

    fun startAppointment(appointmentId: AppointmentId) {
        viewModelScope.launch {
            val appointment = appointmentRepository.findById(appointmentId).getOrNull()
            appointment?.start()?.fold(
                onSuccess = { updated ->
                    appointmentRepository.update(updated)
                    loadSchedule()
                    emitEvent(UiEvent.ShowToast("Appointment started"))
                },
                onFailure = { error ->
                    emitEvent(UiEvent.ShowError(error))
                }
            )
        }
    }
}
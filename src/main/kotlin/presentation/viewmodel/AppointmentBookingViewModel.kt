package presentation.viewmodel

import core.utils.DomainError
import core.utils.PatientId
import data.repoistories.DoctorRepository
import domain.entities.Doctor
import domain.models.AppointmentType
import domain.models.MedicalDepartment
import domain.usecases.implementation.ScheduleAppointmentUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import presentation.state.AppointmentBookingState
import presentation.state.UiEvent
import java.time.LocalDateTime

class AppointmentBookingViewModel(
    private val patientId: PatientId,
    private val doctorRepository: DoctorRepository,
    private val scheduleAppointment: ScheduleAppointmentUseCase,
) : BaseViewModel() {
    private val _state = MutableStateFlow(AppointmentBookingState())
    val state = _state.asStateFlow()

    init {
        loadAvailableDoctors()
    }

    fun loadAvailableDoctors(specialization: MedicalDepartment? = null) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val result = if (specialization != null)
                doctorRepository.findBySpecialization(specialization)
            else
                doctorRepository.findAll()

            result.fold(
                onSuccess = { doctors ->
                    _state.value = _state.value.copy(
                        availableDoctors = doctors.filter { it.isAcceptingPatients },
                        isLoading = false
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error
                    )
                }
            )
        }
    }

    fun selectDoctor(doctor: Doctor) {
        _state.value = _state.value.copy(selectedDoctor = doctor)
    }

    fun setAppointmentType(type: AppointmentType) {
        _state.value = _state.value.copy(appointmentType = type)
    }

    fun setDate(date: String) {
        _state.value = _state.value.copy(selectedDate = date)
    }

    fun setTime(time: String) {
        _state.value = _state.value.copy(selectedTime = time)
    }

    fun setReason(reason: String) {
        _state.value = _state.value.copy(reason = reason)
    }

    fun bookAppointment() {
        viewModelScope.launch {
            val currentState = _state.value

            if (currentState.selectedDoctor == null) {
                emitEvent(UiEvent.ShowToast("Please select a doctor"))
                return@launch
            }

            if (currentState.selectedDate.isEmpty() || currentState.selectedTime.isEmpty()) {
                emitEvent(UiEvent.ShowToast("Please select date and time"))
                return@launch
            }

            if (currentState.reason.isEmpty()) {
                emitEvent(UiEvent.ShowToast("Please provide a reason for the appointment"))
                return@launch
            }

            _state.value = currentState.copy(isLoading = true, error = null)

            try {
                val dateTime = LocalDateTime.parse("${currentState.selectedDate}T${currentState.selectedTime}")

                val input = ScheduleAppointmentUseCase.Input(
                    patientId = patientId,
                    doctorId = currentState.selectedDoctor.id,
                    appointmentType = currentState.appointmentType,
                    scheduledTime = dateTime,
                    duration = 30,
                    reason = currentState.reason
                )

                scheduleAppointment(input).fold(
                    onSuccess = { appointment ->
                        _state.value = currentState.copy(
                            isLoading = false,
                            bookingSuccess = true
                        )
                        emitEvent(UiEvent.ShowToast("Appointment booked successfully"))
                        delay(1000)
                        emitEvent(UiEvent.NavigateBack)
                    },
                    onFailure = { error ->
                        _state.value = currentState.copy(
                            isLoading = false,
                            error = error
                        )
                        emitEvent(UiEvent.ShowError(error))
                    }
                )
            } catch (e: Exception) {
                _state.value = currentState.copy(
                    isLoading = false,
                    error = DomainError.ValidationError("Invalid date or time format")
                )
                emitEvent(UiEvent.ShowError(core.utils.DomainError.SystemError("Failed to book appointment", cause = e)))
            }
        }
    }
}
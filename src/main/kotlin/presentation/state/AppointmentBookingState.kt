package presentation.state

import core.utils.DomainError
import domain.entities.Doctor
import domain.models.AppointmentType

data class AppointmentBookingState(
    val availableDoctors: List<Doctor> = emptyList(),
    val selectedDoctor: Doctor? = null,
    val selectedDate: String = "",
    val selectedTime: String = "",
    val appointmentType: AppointmentType = AppointmentType.CONSULTATION,
    val reason: String = "",
    val isLoading: Boolean = false,
    val bookingSuccess: Boolean = false,
    val error: DomainError? = null
)
package presentation.state

import core.utils.DomainError
import domain.entities.Appointment
import domain.entities.Doctor

data class DoctorScheduleState(
    val doctor: Doctor? = null,
    val todayAppointments: List<Appointment> = emptyList(),
    val currentAppointment: Appointment? = null,
    val isLoading: Boolean = false,
    val error: DomainError? = null
)
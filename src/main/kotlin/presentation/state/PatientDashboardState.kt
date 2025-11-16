package presentation.state

import core.utils.DomainError
import domain.entities.Appointment
import domain.entities.Invoice
import domain.entities.Patient
import domain.entities.Prescription

data class PatientDashboardState(
    val patient: Patient? = null,
    val upcomingAppointments: List<Appointment> = emptyList(),
    val recentPrescription: List<Prescription> = emptyList(),
    val pendingInvoices: List<Invoice> = emptyList(),
    val isLoading: Boolean = false,
    val error: DomainError? = null
)

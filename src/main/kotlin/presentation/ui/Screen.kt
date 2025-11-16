package presentation.ui

import core.utils.DoctorId
import core.utils.PatientId

sealed class Screen {
    object Login : Screen()
    data class PatientDashboard(val patientId: PatientId) : Screen()
    data class BookAppointment(val patientId: PatientId) : Screen()
    data class DoctorSchedule(val doctorId: DoctorId) : Screen()
    data class PharmacyDashboard(val pharmacistId: String) : Screen()
    object Analytics : Screen()
}
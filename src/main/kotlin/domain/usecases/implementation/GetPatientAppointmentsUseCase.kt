package domain.usecases.implementation

import core.functional.Result
import core.utils.PatientId
import data.repoistories.AppointmentRepository
import domain.entities.Appointment
import domain.models.AppointmentStatus
import domain.usecases.intefaces.UseCase

class GetPatientAppointmentsUseCase(
    private val appointmentRepository: AppointmentRepository,
) : UseCase<GetPatientAppointmentsUseCase.Input, List<Appointment>> {
    data class Input(
        val patientId: PatientId,
        val statusFilter: AppointmentStatus? = null,
        val upcomingOnly: Boolean = false,
    )

    override suspend fun invoke(input: Input): Result<List<Appointment>> {
        val appointments = if (input.upcomingOnly) {
            appointmentRepository.findUpComingAppointments(input.patientId)
        } else {
            appointmentRepository.findByPatientId(input.patientId)
        }.fold(
            onSuccess = { it },
            onFailure = { return Result.failure(it) }
        )

        val filtered = input.statusFilter?.let { status ->
            appointments.filter { it.status == status }
        } ?: appointments

        return Result.success(filtered)
    }
}
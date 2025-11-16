package domain.usecases.implementation

import core.functional.Result
import core.utils.AppointmentId
import core.utils.DomainError
import data.repoistories.AppointmentRepository
import domain.entities.Appointment
import domain.usecases.intefaces.UseCase

class ConfirmAppointmentUseCase(
    private val appointmentRepository: AppointmentRepository,
) : UseCase<AppointmentId, Appointment> {
    override suspend fun invoke(input: AppointmentId): Result<Appointment> {
        val appointment = appointmentRepository.findById(input).getOrNull()
            ?: return Result.failure(
                DomainError.NotFoundError("Appointment", input.value)
            )

        val confirmedAppointment = appointment.confirm().fold(
            onSuccess = { it },
            onFailure = { return Result.failure(it) }
        )

        return appointmentRepository.update(confirmedAppointment)
    }
}
package domain.usecases.implementation

import core.functional.Result
import core.utils.AppointmentId
import core.utils.DomainError
import data.repoistories.AppointmentRepository
import domain.entities.Appointment
import domain.usecases.intefaces.UseCase

class CancelAppointmentUseCase(
    private val appointmentRepository: AppointmentRepository,
) : UseCase<CancelAppointmentUseCase.Input, Appointment> {
    data class Input(
        val appointmentId: AppointmentId,
        val reason: String,
    )

    override suspend fun invoke(input: Input): Result<Appointment> {
        val appointment = appointmentRepository.findById(input.appointmentId).getOrNull()
            ?: return Result.failure(
                DomainError.NotFoundError("Appointment", input.appointmentId.value)
            )

        val cancelledAppointment = appointment.cancel(input.reason).fold(
            onSuccess = { it },
            onFailure = { return Result.failure(it) }
        )

        return appointmentRepository.update(cancelledAppointment)
    }
}
package domain.usecases.implementation

import core.functional.Result
import core.utils.DoctorId
import core.utils.DomainError
import core.utils.PatientId
import data.repoistories.AppointmentRepository
import data.repoistories.DoctorRepository
import data.repoistories.PatientRepository
import domain.entities.Appointment
import domain.models.AppointmentType
import domain.usecases.intefaces.UseCase
import java.time.LocalDateTime

class ScheduleAppointmentUseCase(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val doctorRepository: DoctorRepository,
) : UseCase<ScheduleAppointmentUseCase.Input, Appointment> {

    data class Input(
        val patientId: PatientId,
        val doctorId: DoctorId,
        val appointmentType: AppointmentType,
        val scheduledTime: LocalDateTime,
        val duration: Int,
        val reason: String,
    )

    override suspend fun invoke(input: Input): Result<Appointment> {
        patientRepository.findById(input.patientId).fold(
            onSuccess = { },
            onFailure = { return Result.failure(it) }
        )

        doctorRepository.findById(input.doctorId).getOrNull()
            ?: return Result.failure(
                DomainError.NotFoundError("Doctor", input.doctorId.value)
            )

        val isAvailable = appointmentRepository
            .checkDoctorAvailability(input.doctorId, input.scheduledTime)
            .getOrElse { false }

        if (!isAvailable)
            return Result.failure(
                DomainError.BusinessRuleViolation("Doctor is not available at the requested time")
            )

        val appointmentResult = Appointment.schedule(
            patientId = input.patientId,
            doctorId = input.doctorId,
            appointmentType = input.appointmentType,
            scheduledTime = input.scheduledTime,
            duration = input.duration,
            reason = input.reason
        )

        val appointment = when (appointmentResult) {
            is Result.Success -> appointmentResult.value
            is Result.Failure -> return Result.failure(appointmentResult.errorMessage)
        }

        return appointmentRepository.save(appointment)
    }
}
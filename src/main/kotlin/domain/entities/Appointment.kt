package domain.entities

import core.functional.Result
import core.utils.*
import domain.models.AppointmentStatus
import domain.models.AppointmentType
import java.time.LocalDateTime

data class Appointment(
    val id: AppointmentId,
    val patientId: PatientId,
    val doctorId: DoctorId,
    val appointmentType: AppointmentType,
    val scheduledTime: LocalDateTime,
    val duration: Int,
    val status: AppointmentStatus,
    val reason: String,
    val notes: String? = null,
    val diagnosis: String? = null,
    val prescriptionId: PrescriptionId? = null,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
) : Entity {
    val endTime: LocalDateTime
        get() = scheduledTime.plusMinutes(duration.toLong())

    fun confirm(): Result<Appointment> =
        when (status) {
            AppointmentStatus.SCHEDULED -> Result.success(
                copy(
                    status = AppointmentStatus.CONFIRMED,
                    updatedAt = System.currentTimeMillis()
                )
            )

            else -> Result.failure(
                DomainError.BusinessRuleViolation("Only scheduled appointments can be confirmed")
            )
        }

    fun start(): Result<Appointment> =
        when (status) {
            AppointmentStatus.CONFIRMED -> Result.success(
                copy(
                    status = AppointmentStatus.IN_PROGRESS,
                    updatedAt = System.currentTimeMillis()
                )
            )

            else -> Result.failure(
                DomainError.BusinessRuleViolation("Only confirmed appointments can be started")
            )
        }

    fun complete(diagnosis: String, prescriptionId: PrescriptionId?): Result<Appointment> =
        when (status) {
            AppointmentStatus.IN_PROGRESS -> Result.success(
                copy(
                    status = AppointmentStatus.COMPLETED,
                    updatedAt = System.currentTimeMillis(),
                    diagnosis = diagnosis,
                    prescriptionId = prescriptionId
                )
            )

            else -> Result.failure(
                DomainError.BusinessRuleViolation("Only in-progress appointments can be completed")
            )
        }

    fun cancel(reason: String): Result<Appointment> =
        when (status) {
            AppointmentStatus.COMPLETED -> Result.failure(
                DomainError.BusinessRuleViolation("You cannot cancel a completed appointment!")
            )

            else -> Result.success(
                copy(
                    status = AppointmentStatus.CANCELLED,
                    reason = reason,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

    companion object {
        fun schedule(
            patientId: PatientId,
            doctorId: DoctorId,
            appointmentType: AppointmentType,
            scheduledTime: LocalDateTime,
            duration: Int,
            reason: String,
        ): Result<Appointment> {
            if (scheduledTime.isBefore(LocalDateTime.now()))
                return Result.failure(
                    DomainError.ValidationError("Cannot Schedule appointment in the past!")
                )
            if (duration !in 15..240)
                return Result.failure(
                    DomainError.ValidationError("Duration must be from 15 minutes to 4 hours")
                )
            if (reason.isBlank())
                return Result.failure(
                    DomainError.ValidationError("Appointment reason is required")
                )
            return Result.success(
                Appointment(
                    id = AppointmentId.generate(),
                    patientId = patientId,
                    doctorId = doctorId,
                    appointmentType = appointmentType,
                    scheduledTime = scheduledTime,
                    duration = duration,
                    status = AppointmentStatus.SCHEDULED,
                    reason = reason
                )
            )
        }
    }
}

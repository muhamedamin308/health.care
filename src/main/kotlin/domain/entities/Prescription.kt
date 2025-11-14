package domain.entities

import core.functional.Result
import core.utils.*
import domain.models.PrescriptionStatus

data class Prescription(
    val id: PrescriptionId,
    val patientId: PatientId,
    val doctorId: DoctorId,
    val appointmentId: AppointmentId,
    val medications: List<Medication>,
    val status: PrescriptionStatus,
    val notes: String? = null,
    val validUntil: Long,
    val dispensedBy: String? = null,
    val dispensedAt: Long? = null,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
) : Entity {
    fun isExpired(): Boolean =
        System.currentTimeMillis() > validUntil

    fun canBeDispensed(): Boolean =
        status == PrescriptionStatus.PENDING && !isExpired()

    fun dispensed(pharmacistId: String): Result<Prescription> {
        if (!canBeDispensed())
            return Result.failure(
                DomainError.BusinessRuleViolation("Prescription cannot be dispended")
            )

        return Result.success(
            copy(
                status = PrescriptionStatus.DISPENSED,
                dispensedAt = System.currentTimeMillis(),
                dispensedBy = pharmacistId,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun cancel(): Result<Prescription> {
        if (status == PrescriptionStatus.DISPENSED) {
            return Result.failure(
                DomainError.BusinessRuleViolation("Cannot cancel a dispensed prescriptions")
            )
        }

        return Result.success(
            copy(
                status = PrescriptionStatus.CANCELED,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    companion object {
        fun create(
            patientId: PatientId,
            doctorId: DoctorId,
            appointmentId: AppointmentId,
            medications: List<Medication>,
            validityDays: Int = 30,
            notes: String? = null
        ): Result<Prescription> {
            if (medications.isEmpty()) {
                return Result.failure(
                    DomainError.ValidationError("Prescriptions must have at least one medication")
                )
            }
            if (validityDays !in 1..365)
                return Result.failure(
                    DomainError.ValidationError("Validity must be between 1 and 365 day")
                )
            val validUntil = System.currentTimeMillis() + (validityDays * 24 * 60 * 60 * 1000L)

            return Result.success(
                Prescription(
                    id = PrescriptionId.generate(),
                    patientId = patientId,
                    doctorId = doctorId,
                    appointmentId = appointmentId,
                    medications = medications,
                    status = PrescriptionStatus.PENDING,
                    validUntil = validUntil,
                    notes = notes
                )
            )
        }
    }
}

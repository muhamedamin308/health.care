package domain.entities

import core.functional.Result
import core.utils.AppointmentId
import core.utils.DoctorId
import core.utils.DomainError
import core.utils.PatientId
import domain.models.MedicalReportType

data class MedicalReport(
    val id: String,
    val patientId: PatientId,
    val appointmentId: AppointmentId?,
    val recordType: MedicalReportType,
    val chiefComplaint: String,
    val diagnosis: String,
    val treatment: String,
    val vitalSigns: VitalSigns?,
    val labTests: List<LabTest> = emptyList(),
    val notes: String?,
    val followUpRequired: Boolean,
    val followUpDate: Long?,
    val recordedBy: DoctorId,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
) : Entity {

    companion object {
        fun create(
            patientId: PatientId,
            appointmentId: AppointmentId?,
            recordType: MedicalReportType,
            chiefComplaint: String,
            diagnosis: String,
            treatment: String,
            vitalSigns: VitalSigns?,
            recordedBy: DoctorId,
            notes: String? = null,
            followUpRequired: Boolean = false,
            followUpDate: Long? = null,
        ): Result<MedicalReport> {
            if (chiefComplaint.isBlank())
                return Result.failure(
                    DomainError.ValidationError("Chief complaint is required")
                )
            if (diagnosis.isBlank())
                return Result.failure(
                    DomainError.ValidationError("Diagnosis is required")
                )
            if (followUpRequired && followUpDate == null)
                return Result.failure(
                    DomainError.ValidationError("Follow-up date is required when follow-up is needed")
                )

            return Result.success(
                MedicalReport(
                    id = "MR${System.currentTimeMillis()}",
                    patientId = patientId,
                    appointmentId = appointmentId,
                    recordType = recordType,
                    chiefComplaint = chiefComplaint,
                    diagnosis = diagnosis,
                    treatment = treatment,
                    vitalSigns = vitalSigns,
                    notes = notes,
                    followUpRequired = followUpRequired,
                    followUpDate = followUpDate,
                    recordedBy = recordedBy
                )
            )
        }
    }
}
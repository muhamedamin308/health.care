package data.repoistories

import core.functional.Result
import core.utils.AppointmentId
import core.utils.PatientId
import domain.entities.MedicalReport
import domain.models.MedicalReportType

interface MedicalRecordRepository : Repository<MedicalReport, String> {
    suspend fun findByPatient(patientId: PatientId): Result<List<MedicalReport>>
    suspend fun findByAppointment(appointmentId: AppointmentId): Result<MedicalReport>
    suspend fun findByType(recordType: MedicalReportType): Result<List<MedicalReport>>
}
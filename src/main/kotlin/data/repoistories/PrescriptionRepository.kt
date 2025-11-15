package data.repoistories

import core.functional.Result
import core.utils.DoctorId
import core.utils.PatientId
import core.utils.PrescriptionId
import domain.entities.Prescription

interface PrescriptionRepository : Repository<Prescription, PrescriptionId> {
    suspend fun findByPatient(patientId: PatientId): Result<List<Prescription>>
    suspend fun findByDoctor(doctorId: DoctorId): Result<List<Prescription>>
    suspend fun findPendingForDispensing(): Result<List<Prescription>>
    suspend fun findExpired(): Result<List<Prescription>>
}
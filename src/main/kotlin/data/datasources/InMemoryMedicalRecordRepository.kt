package data.datasources

import core.functional.Result
import core.utils.AppointmentId
import core.utils.DomainError
import core.utils.PatientId
import data.repoistories.MedicalRecordRepository
import domain.entities.MedicalReport
import domain.models.MedicalReportType

class InMemoryMedicalRecordRepository : MedicalRecordRepository {
    private val storage = mutableMapOf<String, MedicalReport>()

    override suspend fun findByPatient(patientId: PatientId): Result<List<MedicalReport>> =
        Result.success(storage.values.filter { it.patientId == patientId })

    override suspend fun findByAppointment(appointmentId: AppointmentId): Result<MedicalReport> {
        val found = storage.values.find { it.appointmentId == appointmentId }
        return found?.let { Result.success(it) }
            ?: Result.failure(DomainError.NotFoundError("MedicalReport", appointmentId.value))
    }

    override suspend fun findByType(recordType: MedicalReportType): Result<List<MedicalReport>> =
        Result.success(storage.values.filter { it.recordType == recordType })

    override suspend fun findById(id: String): Result<MedicalReport> =
        storage[id]?.let { Result.success(it) }
            ?: Result.failure(DomainError.NotFoundError("MedicalReport", id))

    override suspend fun save(entity: MedicalReport): Result<MedicalReport> {
        storage[entity.id] = entity
        return Result.success(entity)
    }

    override suspend fun delete(id: String): Result<Boolean> {
        return if (storage.remove(id) != null)
            Result.success(true)
        else
            Result.failure(DomainError.NotFoundError("MedicalReport", id))
    }

    override suspend fun update(entity: MedicalReport): Result<MedicalReport> {
        if (!storage.containsKey(entity.id))
            return Result.failure(DomainError.NotFoundError("MedicalReport", entity.id))
        storage[entity.id] = entity.copy(updatedAt = System.currentTimeMillis())
        return Result.success(entity)
    }

    override suspend fun findAll(): Result<List<MedicalReport>> =
        Result.success(storage.values.toList())
}
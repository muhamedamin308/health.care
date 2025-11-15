package data.datasources

import core.functional.Result
import core.utils.DoctorId
import core.utils.DomainError
import core.utils.PatientId
import core.utils.PrescriptionId
import data.repoistories.PrescriptionRepository
import domain.entities.Prescription
import domain.models.PrescriptionStatus

class InMemoryPrescriptionRepository : PrescriptionRepository {
    private val storage = mutableMapOf<PrescriptionId, Prescription>()
    override suspend fun findByPatient(patientId: PatientId): Result<List<Prescription>> =
        Result.success(storage.values.filter { it.patientId == patientId })

    override suspend fun findByDoctor(doctorId: DoctorId): Result<List<Prescription>> =
        Result.success(storage.values.filter { it.doctorId == doctorId })

    override suspend fun findPendingForDispensing(): Result<List<Prescription>> =
        Result.success(
            storage.values.filter { it.status == PrescriptionStatus.PENDING && !it.isExpired() }
        )

    override suspend fun findExpired(): Result<List<Prescription>> =
        Result.success(storage.values.filter { it.isExpired() })

    override suspend fun findById(id: PrescriptionId): Result<Prescription> =
        storage[id]?.let { Result.success(it) }
            ?: Result.failure(DomainError.NotFoundError("Prescription", id.value))

    override suspend fun findAll(): Result<List<Prescription>> =
        Result.success(storage.values.toList())

    override suspend fun save(entity: Prescription): Result<Prescription> {
        storage[entity.id] = entity
        return Result.success(entity)
    }

    override suspend fun update(entity: Prescription): Result<Prescription> {
        if (!storage.containsKey(entity.id)) {
            return Result.failure(DomainError.NotFoundError("Prescription", entity.id.value))
        }
        storage[entity.id] = entity
        return Result.success(entity)
    }

    override suspend fun delete(id: PrescriptionId): Result<Boolean> =
        if (storage.remove(id) != null) Result.success(true)
        else Result.failure(DomainError.NotFoundError("Prescription", id.value))
}
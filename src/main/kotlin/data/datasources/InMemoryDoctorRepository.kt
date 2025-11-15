package data.datasources

import core.functional.Result
import core.utils.DoctorId
import core.utils.DomainError
import data.repoistories.DoctorRepository
import domain.entities.Doctor
import domain.models.MedicalDepartment
import java.time.LocalDateTime

class InMemoryDoctorRepository : DoctorRepository {
    private val storage = mutableMapOf<DoctorId, Doctor>()
    override suspend fun findBySpecialization(department: MedicalDepartment): Result<List<Doctor>> =
        Result.success(storage.values.filter { it.specialization == department })

    override suspend fun findAvailableDoctors(dateTime: LocalDateTime): Result<List<Doctor>> =
        Result.success(storage.values.filter {
            it.isAcceptingPatients && it.isAvailable(dateTime)
        })

    override suspend fun findByLicenseNumber(licenseNumber: String): Result<Doctor> =
        storage.values.find { it.licenseNumber == licenseNumber }
            ?.let { Result.success(it) }
            ?: Result.failure(DomainError.NotFoundError("Doctor", "license: $licenseNumber"))

    override suspend fun findById(id: DoctorId): Result<Doctor> =
        storage[id]?.let { Result.success(it) }
            ?: Result.failure(DomainError.NotFoundError("Doctor", id.value))

    override suspend fun save(entity: Doctor): Result<Doctor> {
        if (storage.containsKey(entity.id))
            return Result.failure(DomainError.ConflictError("Doctor with id ${entity.id.value} already exists"))
        storage[entity.id] = entity
        return Result.success(entity)
    }

    override suspend fun delete(id: DoctorId): Result<Boolean> =
        if (storage.remove(id) != null)
            Result.success(true)
        else
            Result.failure(DomainError.NotFoundError("Doctor", id.value))

    override suspend fun update(entity: Doctor): Result<Doctor> {
        if (!storage.containsKey(entity.id))
            return Result.failure(DomainError.NotFoundError("Doctor", entity.id.value))
        storage[entity.id] = entity.copy(updatedAt = System.currentTimeMillis())
        return Result.success(entity)
    }

    override suspend fun findAll(): Result<List<Doctor>> =
        Result.success(storage.values.toList())
}
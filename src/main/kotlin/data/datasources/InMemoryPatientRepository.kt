package data.datasources

import core.functional.Result
import core.utils.DomainError
import core.utils.Email
import core.utils.PatientId
import core.utils.PhoneNumber
import data.repoistories.PatientRepository
import domain.entities.Patient
import domain.models.BloodType

class InMemoryPatientRepository : PatientRepository {
    private val storage = mutableMapOf<PatientId, Patient>()
    override suspend fun findByEmail(email: Email): Result<Patient> =
        storage.values.find { it.email == email }
            ?.let { Result.success(it) }
            ?: Result.failure(DomainError.NotFoundError("Patient", "email: ${email.value}"))

    override suspend fun findByPhone(phone: PhoneNumber): Result<Patient> =
        storage.values.find { it.phoneNumber == phone }
            ?.let { Result.success(it) }
            ?: Result.failure(DomainError.NotFoundError("Patient", "phone: ${phone.value}"))

    override suspend fun searchByName(name: String): Result<List<Patient>> {
        val searchQuery = name.lowercase()
        val results = storage.values.filter {
            it.firstName.lowercase().contains(searchQuery) ||
                    it.lastName.lowercase().contains(searchQuery)
        }
        return Result.success(results)
    }

    override suspend fun findWithInsurance(): Result<List<Patient>> =
        Result.success(storage.values.filter { it.hashInsurance() })

    override suspend fun findByBloodType(bloodType: BloodType): Result<List<Patient>> =
        Result.success(storage.values.filter { it.bloodType == bloodType })

    override suspend fun findById(id: PatientId): Result<Patient> =
        storage[id]?.let { Result.success(it) }
            ?: Result.failure(DomainError.NotFoundError("Patient", id.value))

    override suspend fun save(entity: Patient): Result<Patient> {
        if (storage.containsKey(entity.id))
            return Result.failure(DomainError.ConflictError("Patient with id ${entity.id.value} already exists"))
        storage[entity.id] = entity
        return Result.success(entity)
    }

    override suspend fun delete(id: PatientId): Result<Boolean> =
        if (storage.remove(id) != null)
            Result.success(true)
        else
            Result.failure(DomainError.NotFoundError("Patient", id.value))

    override suspend fun update(entity: Patient): Result<Patient> {
        if (!storage.containsKey(entity.id))
            return Result.failure(DomainError.NotFoundError("Patient", entity.id.value))
        storage[entity.id] = entity.copy(updatedAt = System.currentTimeMillis())
        return Result.success(entity)
    }

    override suspend fun findAll(): Result<List<Patient>> =
        Result.success(storage.values.toList())
}
package data.repoistories

import core.functional.Result
import core.utils.Email
import core.utils.PatientId
import core.utils.PhoneNumber
import domain.entities.Patient
import domain.models.BloodType

interface PatientRepository : Repository<Patient, PatientId> {
    suspend fun findByEmail(email: Email): Result<Patient>
    suspend fun findByPhone(phone: PhoneNumber): Result<Patient>
    suspend fun searchByName(name: String): Result<List<Patient>>
    suspend fun findWithInsurance(): Result<List<Patient>>
    suspend fun findByBloodType(bloodType: BloodType): Result<List<Patient>>
}
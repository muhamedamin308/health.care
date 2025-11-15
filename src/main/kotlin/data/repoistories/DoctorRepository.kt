package data.repoistories

import core.functional.Result
import core.utils.DoctorId
import domain.entities.Doctor
import domain.models.MedicalDepartment
import java.time.LocalDateTime

interface DoctorRepository : Repository<Doctor, DoctorId> {
    suspend fun findBySpecialization(department: MedicalDepartment): Result<List<Doctor>>
    suspend fun findAvailableDoctors(dateTime: LocalDateTime): Result<List<Doctor>>
    suspend fun findByLicenseNumber(licenseNumber: String): Result<Doctor>
}
package domain.entities

import core.functional.Money
import core.functional.Result
import core.utils.DoctorId
import core.utils.Email
import core.utils.PhoneNumber
import core.utils.validate
import domain.models.MedicalDepartment
import java.time.LocalDateTime

data class Doctor(
    val id: DoctorId,
    val userId: String,
    val firstName: String,
    val lastName: String,
    val specialization: MedicalDepartment,
    val licenseNumber: String,
    val qualifications: List<String>,
    val phoneNumber: PhoneNumber,
    val email: Email,
    val consultationFee: Money,
    val yearsOfExperience: Int,
    val availableTimeSlots: List<TimeSlot> = emptyList(),
    val isAcceptingPatients: Boolean = true,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
) : Entity {
    val fullName
        get() = "$firstName $lastName"

    fun isAvailable(dateTime: LocalDateTime): Boolean =
        availableTimeSlots.any { it.contains(dateTime) }

    companion object {
        fun create(
            firstName: String,
            lastName: String,
            specialization: MedicalDepartment,
            licenseNumber: String,
            qualifications: List<String>,
            phone: PhoneNumber,
            email: Email,
            consultationFee: Money,
            yearsOfExperience: Int,
        ): Result<Doctor> {
            val validator = validate<Doctor> {
                rule("License number must not be blank") { it.licenseNumber.isNotBlank() }
                rule("Must have at least one qualification") { it.qualifications.isNotEmpty() }
                rule("Years of experience must be non-negative") { it.yearsOfExperience >= 0 }
                rule("Consultation fee must be positive") { it.consultationFee.amount > 0 }
            }

            val doctor = Doctor(
                id = DoctorId.generate(),
                userId = "USR${System.currentTimeMillis()}",
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                specialization = specialization,
                licenseNumber = licenseNumber,
                qualifications = qualifications,
                phoneNumber = phone,
                email = email,
                consultationFee = consultationFee,
                yearsOfExperience = yearsOfExperience
            )

            return validator(doctor)
        }
    }
}
package domain.entities

import core.functional.Result
import core.utils.Email
import core.utils.PatientId
import core.utils.PhoneNumber
import core.utils.validate
import domain.models.BloodType
import domain.models.Gender
import java.time.LocalDate

data class Patient(
    val id: PatientId,
    val userId: String,
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate,
    val gender: Gender,
    val bloodType: BloodType = BloodType.UNKNOWN,
    val phoneNumber: PhoneNumber,
    val email: Email,
    val address: Address,
    val emergencyContact: EmergencyContact,
    val insurance: InsuranceInfo? = null,
    val allergies: List<String> = emptyList(),
    val chronicConditions: List<String> = emptyList(),
    val currentMedications: List<String> = emptyList(),
    val isActive: Boolean = true,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
) : Entity {
    val fullName
        get() = "$firstName $lastName"

    val age: Int
        get() {
            val today = LocalDate.now()
            var age = today.year - dateOfBirth.year
            if (today.monthValue < dateOfBirth.monthValue ||
                today.monthValue == dateOfBirth.monthValue &&
                today.dayOfMonth < dateOfBirth.dayOfMonth
            )
                age--
            return age
        }

    fun hashInsurance(): Boolean = insurance != null && insurance.isActive

    fun hasAllergy(substance: String): Boolean =
        allergies.any { it.equals(substance, ignoreCase = true) }

    companion object {
        fun create(
            firstName: String,
            lastName: String,
            dateOfBirth: LocalDate,
            gender: Gender,
            phone: PhoneNumber,
            email: Email,
            address: Address,
            emergencyContact: EmergencyContact,
        ): Result<Patient> {
            val validator = validate<Patient> {
                rule("First name must not be blank") { it.firstName.isNotBlank() }
                rule("Last name must not be blank") { it.lastName.isNotBlank() }
                rule("Patient must be at least 0 years old") { it.age >= 0 }
                rule("Patient cannot be more than 150 years old") { it.age <= 150 }
            }

            val patient = Patient(
                id = PatientId.generate(),
                userId = "USR${System.currentTimeMillis()}",
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                dateOfBirth = dateOfBirth,
                gender = gender,
                phoneNumber = phone,
                email = email,
                address = address,
                emergencyContact = emergencyContact
            )

            return validator(patient)
        }
    }
}

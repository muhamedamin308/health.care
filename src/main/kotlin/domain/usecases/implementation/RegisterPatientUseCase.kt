package domain.usecases.implementation

import core.functional.Result
import core.utils.DomainError
import core.utils.Email
import core.utils.PhoneNumber
import data.repoistories.PatientRepository
import data.repoistories.UserRepository
import domain.entities.Address
import domain.entities.EmergencyContact
import domain.entities.Patient
import domain.entities.User
import domain.models.BloodType
import domain.models.Gender
import domain.models.UserRole
import domain.usecases.intefaces.UseCase
import java.time.LocalDate

class RegisterPatientUseCase(
    private val patientRepository: PatientRepository,
    private val userRepository: UserRepository,
) : UseCase<RegisterPatientUseCase.Input, Patient> {

    data class Input(
        val email: Email,
        val password: String,
        val firstName: String,
        val lastName: String,
        val dateOfBirth: LocalDate,
        val gender: Gender,
        val phone: PhoneNumber,
        val address: Address,
        val emergencyContact: EmergencyContact,
        val bloodType: BloodType = BloodType.UNKNOWN,
    )

    override suspend fun invoke(input: Input): Result<Patient> {
        userRepository.findByEmail(input.email).fold(
            onSuccess = {
                return Result.failure(
                    DomainError.ConflictError("User with email ${input.email.value} already exists")
                )
            },
            onFailure = {
                // User does not exist, proceed with registration
            }
        )
        val userResult = User.create(
            email = input.email,
            password = input.password,
            role = UserRole.PATIENT
        )
        val user = when (userResult) {
            is Result.Success -> userResult.value
            is Result.Failure -> return Result.failure(userResult.errorMessage)
        }

        userRepository.save(user).fold(
            onSuccess = { /* User saved successfully */ },
            onFailure = { return Result.failure(it) }
        )

        val patientResult = Patient.create(
            firstName = input.firstName,
            lastName = input.lastName,
            dateOfBirth = input.dateOfBirth,
            gender = input.gender,
            phone = input.phone,
            email = input.email,
            address = input.address,
            emergencyContact = input.emergencyContact
        )

        val patient = when (patientResult) {
            is Result.Success -> patientResult.value.copy(
                userId = user.id,
                bloodType = input.bloodType
            )

            is Result.Failure -> {
                return Result.failure(patientResult.errorMessage)
            }
        }
        return patientRepository.save(patient)
    }
}
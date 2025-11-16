package domain.usecases.implementation

import core.functional.Result
import core.utils.DomainError
import core.utils.PatientId
import core.utils.PhoneNumber
import data.repoistories.PatientRepository
import domain.entities.Address
import domain.entities.EmergencyContact
import domain.entities.Patient
import domain.usecases.intefaces.UseCase

class UpdatePatientProfileUseCase(
    private val patientRepository: PatientRepository,
) : UseCase<UpdatePatientProfileUseCase.Input, Patient> {

    data class Input(
        val patientId: PatientId,
        val phone: PhoneNumber? = null,
        val address: Address? = null,
        val emergencyContact: EmergencyContact? = null,
        val allergies: List<String>? = null,
        val chronicConditions: List<String>? = null,
        val currentMedications: List<String>? = null,
    )

    override suspend fun invoke(input: Input): Result<Patient> {
        val existingPatient = patientRepository.findById(input.patientId).getOrNull()
            ?: return Result.failure(DomainError.NotFoundError("Patient", input.patientId.value))

        val updatedPatient = existingPatient.copy(
            phoneNumber = input.phone ?: existingPatient.phoneNumber,
            address = input.address ?: existingPatient.address,
            emergencyContact = input.emergencyContact ?: existingPatient.emergencyContact,
            allergies = input.allergies ?: existingPatient.allergies,
            chronicConditions = input.chronicConditions ?: existingPatient.chronicConditions,
            currentMedications = input.currentMedications ?: existingPatient.currentMedications,
            updatedAt = System.currentTimeMillis()
        )

        return patientRepository.update(updatedPatient)
    }
}
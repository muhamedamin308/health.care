package domain.usecases.implementation

import core.functional.Result
import core.utils.PatientId
import data.repoistories.PatientRepository
import domain.entities.Patient
import domain.usecases.intefaces.UseCase

class GetPatientProfileUseCase(
    private val patientRepository: PatientRepository,
) : UseCase<PatientId, Patient> {

    override suspend fun invoke(input: PatientId): Result<Patient> =
        patientRepository.findById(input)

}
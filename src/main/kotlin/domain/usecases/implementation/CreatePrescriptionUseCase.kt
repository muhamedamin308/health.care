package domain.usecases.implementation

import core.functional.Result
import core.utils.AppointmentId
import core.utils.DoctorId
import core.utils.DomainError
import core.utils.PatientId
import data.repoistories.AppointmentRepository
import data.repoistories.PrescriptionRepository
import domain.entities.Medication
import domain.entities.Prescription
import domain.models.AppointmentStatus
import domain.usecases.intefaces.UseCase

class CreatePrescriptionUseCase(
    private val prescriptionRepository: PrescriptionRepository,
    private val appointmentRepository: AppointmentRepository,
) : UseCase<CreatePrescriptionUseCase.Input, Prescription> {

    data class Input(
        val patientId: PatientId,
        val doctorId: DoctorId,
        val appointmentId: AppointmentId,
        val medications: List<Medication>,
        val validityDays: Int = 30,
        val notes: String? = null,
    )

    override suspend fun invoke(input: Input): Result<Prescription> {
        val appointment = appointmentRepository.findById(input.appointmentId).getOrNull()
            ?: return Result.failure(DomainError.NotFoundError("Appointment", input.appointmentId.value))

        if (appointment.status != AppointmentStatus.COMPLETED)
            return Result.failure(
                DomainError.BusinessRuleViolation("Cannot create prescription for an appointment that is not completed")
            )

        val prescriptionResult = Prescription.create(
            patientId = input.patientId,
            doctorId = input.doctorId,
            appointmentId = input.appointmentId,
            medications = input.medications,
            validityDays = input.validityDays,
            notes = input.notes
        )

        val prescription = when (prescriptionResult) {
            is Result.Failure -> return Result.failure(prescriptionResult.errorMessage)
            is Result.Success -> prescriptionResult.value
        }

        return prescriptionRepository.save(prescription)
    }
}
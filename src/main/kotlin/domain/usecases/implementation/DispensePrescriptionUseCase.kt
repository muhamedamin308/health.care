package domain.usecases.implementation

import core.functional.Result
import core.utils.DomainError
import core.utils.PrescriptionId
import data.repoistories.InventoryRepository
import data.repoistories.PrescriptionRepository
import domain.entities.Prescription
import domain.usecases.intefaces.UseCase

class DispensePrescriptionUseCase(
    private val prescriptionRepository: PrescriptionRepository,
    private val inventoryRepository: InventoryRepository,
) : UseCase<DispensePrescriptionUseCase.Input, Prescription> {

    data class Input(
        val prescriptionId: PrescriptionId,
        val pharmacistId: String,
    )

    override suspend fun invoke(input: Input): Result<Prescription> {
        val prescription = prescriptionRepository.findById(input.prescriptionId).getOrNull()
            ?: return Result.failure(DomainError.NotFoundError("Prescription", input.prescriptionId.value))

        for (medication in prescription.medications) {
            val inventoryItem = inventoryRepository.findByName(medication.name).getOrNull()
                ?: return Result.failure(
                    DomainError.NotFoundError("InventoryItem", medication.name)
                )

            if (inventoryItem.quantity < medication.quantity)
                return Result.failure(
                    DomainError.BusinessRuleViolation(
                        "Insufficient stock for ${medication.name}. " +
                                "Required: ${medication.quantity}, Available: ${inventoryItem.quantity}"
                    )
                )
            val dispensedItem = inventoryItem.dispense(medication.quantity).fold(
                onSuccess = { it },
                onFailure = { return Result.failure(it) }
            )
            inventoryRepository.update(dispensedItem)

        }
        val dispensedPrescription = prescription.dispensed(input.pharmacistId).fold(
            onSuccess = { it },
            onFailure = { return Result.failure(it) }
        )

        return prescriptionRepository.update(dispensedPrescription)
    }
}
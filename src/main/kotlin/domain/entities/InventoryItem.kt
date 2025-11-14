package domain.entities

import core.functional.Money
import core.functional.Result
import core.utils.DomainError
import core.utils.validate

data class InventoryItem(
    val id: String,
    val medicationName: String,
    val genericName: String,
    val manufacturer: String,
    val batchNumber: String,
    val expiryDate: Long,
    val quantity: Int,
    val unitPrice: Money,
    val reorderLevel: Int,
    val location: String,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
) : Entity {

    fun isExpired() = System.currentTimeMillis() > expiryDate

    fun needsReorder() = quantity <= reorderLevel

    fun isLowStock() = quantity <= (reorderLevel * 1.5).toInt()

    fun dispense(requestedQuantity: Int): Result<InventoryItem> {
        if (isExpired())
            return Result.failure(
                DomainError.BusinessRuleViolation("Cannot dispense expired medication")
            )

        if (quantity < requestedQuantity)
            return Result.failure(
                DomainError.BusinessRuleViolation("Insufficient stock. Available: $quantity, Requested: $requestedQuantity")
            )

        return Result.success(
            copy(
                quantity = quantity - requestedQuantity,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun restock(additionalQuantity: Int): Result<InventoryItem> {
        if (additionalQuantity <= 0)
            return Result.failure(
                DomainError.ValidationError("Restock quantity must be positive")
            )

        return Result.success(
            copy(
                quantity = quantity + additionalQuantity,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    companion object {
        fun create(
            medicationName: String,
            genericName: String,
            manufacturer: String,
            batchNumber: String,
            expiryDate: Long,
            quantity: Int,
            unitPrice: Money,
            reorderLevel: Int,
            location: String,
        ): Result<InventoryItem> {

            val validator = validate<InventoryItem> {
                rule("Medication name is required") { it.medicationName.isNotBlank() }
                rule("Quantity must be non-negative") { it.quantity >= 0 }
                rule("Reorder level must be positive") { it.reorderLevel > 0 }
                rule("Expiry date must be in the future") { it.expiryDate > System.currentTimeMillis() }
            }

            val item = InventoryItem(
                id = "INV${System.currentTimeMillis()}",
                medicationName = medicationName,
                genericName = genericName,
                manufacturer = manufacturer,
                batchNumber = batchNumber,
                expiryDate = expiryDate,
                quantity = quantity,
                unitPrice = unitPrice,
                reorderLevel = reorderLevel,
                location = location
            )

            return validator(item)
        }
    }
}
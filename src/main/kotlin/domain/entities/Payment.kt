package domain.entities

import core.functional.Money
import domain.models.PaymentMethod

data class Payment(
    val id: String,
    val amount: Money,
    val method: PaymentMethod,
    val transactionId: String?,
    val paidAt: Long,
    val notes: String?,
) {
    companion object {
        fun create(
            amount: Money,
            method: PaymentMethod,
            transactionId: String? = null,
            notes: String? = null,
        ): Payment = Payment(
            id = "PAY${System.currentTimeMillis()}",
            amount = amount,
            method = method,
            transactionId = transactionId,
            paidAt = System.currentTimeMillis(),
            notes = notes
        )
    }
}

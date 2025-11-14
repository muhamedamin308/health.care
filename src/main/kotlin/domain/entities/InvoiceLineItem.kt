package domain.entities

import core.functional.Money

data class InvoiceLineItem(
    val description: String,
    val quantity: Int,
    val unitPrice: Money,
    val discount: Double = 0.0
) {
    init {
        require(quantity > 0) { "Quantity must be positive" }
        require(discount in 0.0..100.0) { "Discount must be from 0 to 100" }
    }

    val subtotal: Money
        get() = unitPrice * quantity.toDouble()

    val discountAmount: Money
        get() = subtotal * (discount / 100.0)

    val total: Money
        get() = subtotal - discountAmount

}
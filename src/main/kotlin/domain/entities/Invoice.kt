package domain.entities

import core.functional.Money
import core.functional.Result
import core.utils.AppointmentId
import core.utils.DomainError
import core.utils.InvoiceId
import core.utils.PatientId
import domain.models.InvoiceStatus

data class Invoice(
    val id: InvoiceId,
    val patientId: PatientId,
    val appointmentId: AppointmentId?,
    val lineItems: List<InvoiceLineItem>,
    val status: InvoiceStatus,
    val payments: List<Payment> = emptyList(),
    val insuranceClaims: InsuranceClaim? = null,
    val dueDate: Long,
    val notes: String? = null,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
) : Entity {

    val subtotal: Money
        get() = lineItems.fold(Money(0.0)) { acc, item -> acc + item.subtotal }

    val totalDiscount: Money
        get() = lineItems.fold(Money(0.0)) { acc, item -> acc + item.discountAmount }

    val totalAmount: Money
        get() = lineItems.fold(Money(0.0)) { acc, item -> acc + item.total }

    val totalPaid: Money
        get() = payments.fold(Money(0.0)) { acc, payment -> acc + payment.amount }

    val insuranceCoverage: Money
        get() = insuranceClaims?.approvedAmount ?: Money(0.0)

    val outstandingBalance: Money
        get() {
            val covered = insuranceCoverage + totalPaid
            return if (totalAmount.amount > covered.amount)
                totalAmount - covered
            else Money(0.0)
        }

    val isFullyPaid: Boolean
        get() = outstandingBalance.amount == 0.0

    val isOverdue: Boolean
        get() = status == InvoiceStatus.PENDING && System.currentTimeMillis() > dueDate

    fun addPayment(payment: Payment): Result<Invoice> {
        if (status == InvoiceStatus.CANCELLED)
            return Result.failure(
                DomainError.BusinessRuleViolation("Cannot add payment to a cancelled invoice.")
            )

        if (payment.amount.amount > outstandingBalance.amount)
            return Result.failure(
                DomainError.BusinessRuleViolation(
                    "Payment amount exceeds outstanding balance. " +
                            "Outstanding: ${outstandingBalance.amount}, Payment: ${payment.amount.amount}"
                )
            )

        val updatedPayments = payments + payment
        val newStatus = when {
            outstandingBalance.amount - payment.amount.amount == 0.0 -> InvoiceStatus.PAID
            updatedPayments.isNotEmpty() -> InvoiceStatus.PARTIALLY_PAID
            else -> status
        }

        return Result.success(
            copy(
                payments = updatedPayments,
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun addInsuranceClaim(claim: InsuranceClaim): Result<Invoice> {
        if (insuranceClaims != null)
            return Result.failure(
                DomainError.BusinessRuleViolation("Insurance claim already exists for this invoice.")
            )
        return Result.success(
            copy(
                insuranceClaims = claim,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun cancel(): Result<Invoice> {
        if (status == InvoiceStatus.PAID)
            return Result.failure(
                DomainError.BusinessRuleViolation("Cannot cancel a paid invoice.")
            )
        return Result.success(
            copy(
                status = InvoiceStatus.CANCELLED,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    companion object {
        fun create(
            patientId: PatientId,
            appointmentId: AppointmentId?,
            lineItems: List<InvoiceLineItem>,
            dueInDays: Int = 30,
            notes: String? = null,
        ): Result<Invoice> {
            if (lineItems.isEmpty()) {
                return Result.failure(
                    DomainError.ValidationError("Invoice must have at least one line item.")
                )
            }
            if (dueInDays <= 0) {
                return Result.failure(
                    DomainError.ValidationError("Due in days must be a positive integer.")
                )
            }

            val dueDate = System.currentTimeMillis() + (dueInDays * 24 * 60 * 60 * 1000L)

            return Result.success(
                Invoice(
                    id = InvoiceId.generate(),
                    patientId = patientId,
                    appointmentId = appointmentId,
                    lineItems = lineItems,
                    status = InvoiceStatus.PENDING,
                    dueDate = dueDate,
                    notes = notes
                )
            )
        }
    }
}

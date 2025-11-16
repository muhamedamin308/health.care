package domain.usecases.implementation

import core.functional.Money
import core.functional.Result
import core.utils.DomainError
import core.utils.InvoiceId
import data.repoistories.InvoiceRepository
import domain.entities.Invoice
import domain.entities.Payment
import domain.models.PaymentMethod
import domain.usecases.intefaces.UseCase

class ProcessPaymentUseCase(
    private val invoiceRepository: InvoiceRepository
): UseCase<ProcessPaymentUseCase.Input, Invoice> {

    data class Input(
        val invoiceId: InvoiceId,
        val amount: Money,
        val method: PaymentMethod,
        val transactionId: String? = null,
        val notes: String? = null
    )

    override suspend fun invoke(input: Input): Result<Invoice> {
        val invoice = invoiceRepository.findById(input.invoiceId).getOrNull()
            ?: return Result.failure(DomainError.NotFoundError("Invoice", input.invoiceId.value))

        val payment = Payment.create(
            amount = input.amount,
            method = input.method,
            transactionId = input.transactionId,
            notes = input.notes
        )

        val updatedInvoice = invoice.addPayment(payment).fold(
            onSuccess = { it },
            onFailure = { return Result.failure(it) }
        )

        return invoiceRepository.update(updatedInvoice)
    }
}
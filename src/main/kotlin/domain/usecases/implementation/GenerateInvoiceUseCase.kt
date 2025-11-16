package domain.usecases.implementation

import core.functional.Result
import core.utils.AppointmentId
import core.utils.DomainError
import core.utils.PatientId
import data.repoistories.AppointmentRepository
import data.repoistories.DoctorRepository
import data.repoistories.InvoiceRepository
import domain.entities.Invoice
import domain.entities.InvoiceLineItem
import domain.usecases.intefaces.UseCase

class GenerateInvoiceUseCase(
    private val invoiceRepository: InvoiceRepository,
    private val appointmentRepository: AppointmentRepository,
    private val doctorRepository: DoctorRepository,
) : UseCase<GenerateInvoiceUseCase.Input, Invoice> {

    data class Input(
        val patientId: PatientId,
        val appointmentId: AppointmentId,
        val additionalCharges: List<InvoiceLineItem> = emptyList(),
        val dueInDays: Int = 30,
    )

    override suspend fun invoke(input: Input): Result<Invoice> {
        val appointment = appointmentRepository.findById(input.appointmentId).getOrNull()
            ?: return Result.failure(DomainError.NotFoundError("Appointment", input.appointmentId.value))

        val doctor = doctorRepository.findById(appointment.doctorId).getOrNull()
            ?: return Result.failure(DomainError.NotFoundError("Doctor", appointment.doctorId.value))

        val lineItems = mutableListOf(
            InvoiceLineItem(
                description = "Consultation - ${doctor.fullName}",
                quantity = 1,
                unitPrice = doctor.consultationFee
            )
        )

        lineItems.addAll(input.additionalCharges)

        val invoiceResult = Invoice.create(
            patientId = input.patientId,
            appointmentId = input.appointmentId,
            lineItems = lineItems,
            dueInDays = input.dueInDays
        )

        val invoice = when (invoiceResult) {
            is Result.Failure -> return Result.failure(invoiceResult.errorMessage)
            is Result.Success -> invoiceResult.value
        }

        return invoiceRepository.save(invoice)
    }
}
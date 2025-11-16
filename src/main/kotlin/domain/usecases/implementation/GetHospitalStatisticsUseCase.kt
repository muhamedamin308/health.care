package domain.usecases.implementation

import core.functional.Money
import core.functional.Result
import data.repoistories.AppointmentRepository
import data.repoistories.InvoiceRepository
import data.repoistories.PatientRepository
import domain.models.AppointmentStatus
import domain.models.InvoiceStatus
import domain.usecases.intefaces.UseCaseWithoutInputs
import java.time.LocalDateTime

@Suppress("IDENTITY_SENSITIVE_OPERATIONS_WITH_VALUE_TYPE")
class GetHospitalStatisticsUseCase(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val invoiceRepository: InvoiceRepository,
) : UseCaseWithoutInputs<GetHospitalStatisticsUseCase.Output> {

    data class Output(
        val totalPatients: Int,
        val totalAppointmentsToday: Int,
        val completedAppointmentsToday: Int,
        val pendingAppointments: Int,
        val totalRevenue: Money,
        val outstandingPayments: Money,
    )

    override suspend fun invoke(): Result<Output> {
        val allPatients = patientRepository.findAll().fold(
            onSuccess = { it },
            onFailure = { return Result.failure(it) }
        )

        val allAppointments = appointmentRepository.findAll().fold(
            onSuccess = { it },
            onFailure = { return Result.failure(it) }
        )

        val allInvoices = invoiceRepository.findAll().fold(
            onSuccess = { it },
            onFailure = { return Result.failure(it) }
        )

        val today = LocalDateTime.now().toLocalDate()
        val appointmentToday = allAppointments.filter {
            it.scheduledTime.toLocalDate() == today
        }

        val completedToday = appointmentToday.count { it.status == AppointmentStatus.COMPLETED }

        val pending =
            allAppointments.count { it.status in listOf(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED) }

        val totalRevenue = allInvoices
            .filter { it.status == InvoiceStatus.PAID }
            .fold(Money(0.0)) { acc, invoice -> acc + invoice.totalAmount }

        val outstanding = allInvoices
            .filter { it.status != InvoiceStatus.PAID && it.status != InvoiceStatus.CANCELLED }
            .fold(Money(0.0)) { acc, invoice -> acc + invoice.outstandingBalance }

        return Result.success(
            Output(
                totalPatients = allPatients.size,
                totalAppointmentsToday = appointmentToday.size,
                completedAppointmentsToday = completedToday,
                pendingAppointments = pending,
                totalRevenue = totalRevenue,
                outstandingPayments = outstanding,
            )
        )
    }
}
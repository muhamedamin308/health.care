package data.repoistories

import core.functional.Money
import core.functional.Result
import core.utils.InvoiceId
import core.utils.PatientId
import domain.entities.Invoice
import domain.models.InvoiceStatus

interface InvoiceRepository : Repository<Invoice, InvoiceId> {
    suspend fun findByPatient(patientId: PatientId): Result<List<Invoice>>
    suspend fun findByStatus(status: InvoiceStatus): Result<List<Invoice>>
    suspend fun findOverdue(): Result<List<Invoice>>
    suspend fun findByDateRange(startDate: Long, endDate: Long): Result<List<Invoice>>
    suspend fun getTotalRevenue(startDate: Long, endDate: Long): Result<Money>
}
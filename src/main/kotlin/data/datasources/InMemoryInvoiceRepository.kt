package data.datasources

import core.functional.Money
import core.functional.Result
import core.utils.DomainError
import core.utils.InvoiceId
import core.utils.PatientId
import data.repoistories.InvoiceRepository
import domain.entities.Invoice
import domain.models.InvoiceStatus

class InMemoryInvoiceRepository : InvoiceRepository {
    private val storage = mutableMapOf<InvoiceId, Invoice>()

    override suspend fun findByPatient(patientId: PatientId): Result<List<Invoice>> =
        Result.success(storage.values.filter { it.patientId == patientId })

    override suspend fun findByStatus(status: InvoiceStatus): Result<List<Invoice>> =
        Result.success(storage.values.filter { it.status == status })

    override suspend fun findOverdue(): Result<List<Invoice>> =
        Result.success(storage.values.filter { it.isOverdue })

    override suspend fun findByDateRange(
        startDate: Long,
        endDate: Long,
    ): Result<List<Invoice>> =
        Result.success(
            storage.values.filter { it.createdAt in startDate..endDate }
        )

    override suspend fun getTotalRevenue(
        startDate: Long,
        endDate: Long,
    ): Result<Money> {
        val total = storage.values
            .filter { it.createdAt in startDate..endDate && it.status == InvoiceStatus.PAID }
            .fold(Money(0.0)) { acc, invoice -> acc + invoice.totalAmount }
        return Result.success(total)
    }

    override suspend fun findById(id: InvoiceId): Result<Invoice> =
        storage[id]?.let { Result.success(it) }
            ?: Result.failure(DomainError.NotFoundError("Invoice", id.value))

    override suspend fun findAll(): Result<List<Invoice>> =
        Result.success(storage.values.toList())

    override suspend fun save(entity: Invoice): Result<Invoice> {
        storage[entity.id] = entity
        return Result.success(entity)
    }

    override suspend fun update(entity: Invoice): Result<Invoice> {
        if (!storage.containsKey(entity.id)) {
            return Result.failure(DomainError.NotFoundError("Invoice", entity.id.value))
        }
        storage[entity.id] = entity
        return Result.success(entity)
    }

    override suspend fun delete(id: InvoiceId): Result<Boolean> =
        if (storage.remove(id) != null) Result.success(true)
        else Result.failure(DomainError.NotFoundError("Invoice", id.value))

}
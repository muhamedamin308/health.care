package data.datasources

import core.functional.Result
import core.utils.DomainError
import data.repoistories.InventoryRepository
import domain.entities.InventoryItem

class InMemoryInventoryRepository : InventoryRepository {
    private val storage = mutableMapOf<String, InventoryItem>()
    override suspend fun findByName(name: String): Result<InventoryItem> =
        storage.values.find { it.medicationName.equals(name, ignoreCase = true) }?.let {
            Result.success(it)
        } ?: Result.failure(DomainError.NotFoundError("Inventory", "Medication name: $name"))

    override suspend fun findLowStock(): Result<List<InventoryItem>> =
        Result.success(storage.values.filter { it.isLowStock() })

    override suspend fun findExpiringSoon(days: Int): Result<List<InventoryItem>> {
        val currentTime = System.currentTimeMillis()
        val thresholdTime = currentTime + (days * 24 * 60 * 60 * 1000L)
        return Result.success(
            storage.values.filter {
                it.expiryDate in currentTime..thresholdTime
            }
        )
    }

    override suspend fun findExpired(): Result<List<InventoryItem>> =
        Result.success(storage.values.filter { it.isExpired() })

    override suspend fun searchMedications(query: String): Result<List<InventoryItem>> =
        Result.success(
            storage.values.filter {
                it.medicationName.contains(query, ignoreCase = true) ||
                        it.genericName.contains(query, ignoreCase = true) ||
                        it.manufacturer.contains(query, ignoreCase = true)
            }
        )

    override suspend fun findById(id: String): Result<InventoryItem> =
        storage[id]?.let {
            Result.success(it)
        } ?: Result.failure(DomainError.NotFoundError("Inventory", "Inventory id: $id"))

    override suspend fun save(entity: InventoryItem): Result<InventoryItem> {
        storage[entity.id] = entity
        return Result.success(entity)
    }

    override suspend fun delete(id: String): Result<Boolean> {
        return if (storage.remove(id) != null) {
            Result.success(true)
        } else {
            Result.failure(DomainError.NotFoundError("Inventory", "Inventory id: $id"))
        }
    }

    override suspend fun update(entity: InventoryItem): Result<InventoryItem> {
        if (!storage.containsKey(entity.id)) {
            return Result.failure(DomainError.NotFoundError("Inventory", "Inventory id: ${entity.id}"))
        } else {
            storage[entity.id] = entity.copy(updatedAt = System.currentTimeMillis())
            return Result.success(entity)
        }
    }

    override suspend fun findAll(): Result<List<InventoryItem>> =
        Result.success(storage.values.toList())
}
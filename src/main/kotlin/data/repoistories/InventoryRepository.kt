package data.repoistories

import core.functional.Result
import domain.entities.InventoryItem

interface InventoryRepository : Repository<InventoryItem, String> {
    suspend fun findByName(name: String): Result<InventoryItem>
    suspend fun findLowStock(): Result<List<InventoryItem>>
    suspend fun findExpiringSoon(days: Int): Result<List<InventoryItem>>
    suspend fun findExpired(): Result<List<InventoryItem>>
    suspend fun searchMedications(query: String): Result<List<InventoryItem>>
}
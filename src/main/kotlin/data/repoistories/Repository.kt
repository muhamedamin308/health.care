package data.repoistories

import core.functional.Result

interface Repository<T, ID> {
    suspend fun findById(id: ID): Result<T>
    suspend fun save(entity: T): Result<T>
    suspend fun delete(id: ID): Result<Boolean>
    suspend fun update(entity: T): Result<T>
    suspend fun findAll(): Result<List<T>>
}
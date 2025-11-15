package data.datasources

import core.functional.Result
import core.utils.DomainError
import core.utils.Email
import core.utils.verifyPassword
import data.repoistories.UserRepository
import domain.entities.User
import domain.models.UserRole

class InMemoryUserRepository : UserRepository {
    private val storage = mutableMapOf<String, User>()
    override suspend fun findByEmail(email: Email): Result<User> =
        storage.values.find { it.email == email }?.let { Result.success(it) }
            ?: Result.failure(DomainError.NotFoundError("User", "Email: $email"))

    override suspend fun findByRole(role: UserRole): Result<List<User>> =
        Result.success(storage.values.filter { it.role == role })

    override suspend fun authenticate(
        email: Email,
        password: String,
    ): Result<User> {
        val user = storage.values.find { it.email == email }
            ?: return Result.failure(DomainError.UnauthorizedError("Already registered user with email: $email"))
        return if (verifyPassword(password, user.passwordHash)) {
            Result.success(user)
        } else {
            Result.failure(DomainError.UnauthorizedError("Invalid email or password"))
        }
    }

    override suspend fun findById(id: String): Result<User> =
        storage[id]?.let { Result.success(it) }
            ?: Result.failure(DomainError.NotFoundError("User", "User Id: $id"))

    override suspend fun save(entity: User): Result<User> {
        if (storage.containsKey(entity.id))
            return Result.failure(DomainError.ConflictError("User with Id: ${entity.id} already exists"))
        storage[entity.id] = entity
        return Result.success(entity)
    }

    override suspend fun delete(id: String): Result<Boolean> {
        return if (storage.remove(id) != null)
            Result.success(true)
        else
            Result.failure(DomainError.NotFoundError("User", "User Id: $id"))
    }

    override suspend fun update(entity: User): Result<User> {
        if (!storage.containsKey(entity.id))
            return Result.failure(DomainError.NotFoundError("User", "User Id: ${entity.id}"))
        storage[entity.id] = entity.copy(updatedAt = System.currentTimeMillis())
        return Result.success(entity)
    }

    override suspend fun findAll(): Result<List<User>> =
        Result.success(storage.values.toList())
}
package domain.entities

import core.functional.Result
import core.utils.DomainError
import core.utils.Email
import domain.models.UserRole

data class User(
    val id: String,
    val email: Email,
    val passwordHash: String,
    val role: UserRole,
    val isActive: Boolean = true,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
) : Entity {
    companion object {
        fun create(
            email: Email,
            password: String,
            role: UserRole,
        ): Result<User> = runCatching {
            require(password.length >= 6) { "Password must be at least 6 characters" }
            require(password.any { it.isUpperCase() }) { "Password must contain uppercases" }
            require(password.any { it.isDigit() }) { "Password must contain digits" }

            User(
                id = generateUserId(),
                email = email,
                passwordHash = hashPassword(password),
                role = role
            )
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(DomainError.ValidationError(it.message ?: "Invalid user data")) }
        )

        private fun generateUserId(): String = "USR${System.currentTimeMillis()}"

        fun hashPassword(password: String): String = password.hashCode().toString()
    }
}
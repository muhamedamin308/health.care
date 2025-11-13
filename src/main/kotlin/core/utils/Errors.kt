package core.utils

sealed class DomainError {
    abstract val message: String
    abstract val code: String

    data class ValidationError(
        override val message: String,
        override val code: String = "VALIDATION_ERROR",
        val field: String? = null
    ): DomainError()

    data class BusinessRuleViolation(
        override val message: String,
        override val code: String = "BUSINESS_RULE_VIOLATION"
    ): DomainError()

    data class NotFoundError(
        val entity: String,
        val id: String,
        override val message: String = "$entity with id $id not found!",
        override val code: String = "NOT_FOUND"
    ): DomainError()

    data class UnauthorizedError(
        override val message: String = "Unauthorized Access",
        override val code: String = "UNAUTHORIZED"
    ): DomainError()

    data class ConflictError(
        override val message: String,
        override val code: String = "CONFLICT"
    ): DomainError()

    data class SystemError(
        override val message: String,
        override val code: String = "SYSTEM_ERROR",
        val cause: Throwable? = null
    ): DomainError()
}
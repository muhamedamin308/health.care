package domain.entities

sealed interface Auditable {
    val createdBy: String?
    val updatedBy: String?
}
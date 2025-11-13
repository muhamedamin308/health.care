package domain.entities

sealed interface Entity {
    val createdAt: Long
    val updatedAt: Long
}
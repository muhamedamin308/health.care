package domain.models

enum class InvoiceStatus {
    PENDING,
    PAID,
    PARTIALLY_PAID,
    OVERDUE,
    CANCELLED
}
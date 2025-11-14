package domain.entities

data class LabTest(
    val testName: String,
    val category: String,
    val result: String,
    val normalRange: String,
    val unit: String,
    val isAbnormal: Boolean,
    val performedAt: Long,
    val reportedBy: String
)
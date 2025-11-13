package domain.entities

import java.time.LocalDate

data class InsuranceInfo(
    val provider: String,
    val policyNumber: String,
    val groupNumber: String?,
    val coveragePercentage: Int,
    val expiryDate: LocalDate,
    val isActive: Boolean = true,
) {
    init {
        require(coveragePercentage in 0..100) {
            "Coverage Percentage must be from 0 to 100"
        }
    }

    fun isExpired(): Boolean = LocalDate.now().isAfter(expiryDate)
}


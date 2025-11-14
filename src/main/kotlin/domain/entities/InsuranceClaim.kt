package domain.entities

import core.functional.Money
import domain.models.InsuranceClaimStatus

data class InsuranceClaim(
    val claimNumber: String,
    val approvedAmount: Money,
    val submittedAt: Long,
    val status: InsuranceClaimStatus
)
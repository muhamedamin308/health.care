package domain.entities

import core.utils.PhoneNumber

data class EmergencyContact(
    val name: String,
    val relationship: String,
    val phone: PhoneNumber,
)

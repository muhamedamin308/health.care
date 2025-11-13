package domain.entities

import core.utils.PhoneNumber

data class Address(
    val street: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String
) {
    fun toFormattedString(): String =
        "$street, $city, $state $zipCode, $country"
}


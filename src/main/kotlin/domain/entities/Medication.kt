package domain.entities

data class Medication(
    val name: String,
    val dosage: String,
    val frequency: String,
    val duration: String,
    val instructions: String,
    val quantity: Int
) {
    init {
        require(name.isNotBlank()) { "Medicine name is required" }
        require(quantity > 0) { "quantity must be positive" }
    }
}

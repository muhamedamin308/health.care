package domain.entities

data class VitalSigns(
    val bloodPressureSystolic: Int,
    val bloodPressureDiastolic: Int,
    val heartRate: Int,
    val temperature: Double,
    val respiratoryRate: Int,
    val oxygenSaturation: Int,
    val weight: Double,
    val height: Double,
    val recordedAt: Long = System.currentTimeMillis()
) {
    val bmi: Double
        get() = weight / ((height / 100) * (height / 100))

    fun isNormalBloodPressure(): Boolean =
        bloodPressureSystolic in 90..120 && bloodPressureDiastolic in 60..80

    fun isNormalHeartRate() = heartRate in 60..1000

    fun isNormalTemperature() = temperature in 36.1..37.5
}
package domain.entities

import java.time.LocalDateTime

data class TimeSlot(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
) {
    init {
        require(endTime.isAfter(startTime)) {
            "End time must be after the start time"
        }
    }

    fun contains(dateTime: LocalDateTime): Boolean =
        !dateTime.isBefore(startTime) && !dateTime.isAfter(endTime)

    fun overlaps(other: TimeSlot): Boolean =
        !endTime.isBefore(other.startTime) && !startTime.isAfter(other.endTime)

    fun durationInMinutes(): Long =
        java.time.Duration.between(startTime, endTime).toMinutes()
}

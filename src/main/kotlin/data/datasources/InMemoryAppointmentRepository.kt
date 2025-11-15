package data.datasources

import core.functional.Result
import core.utils.AppointmentId
import core.utils.DoctorId
import core.utils.DomainError
import core.utils.PatientId
import data.repoistories.AppointmentRepository
import domain.entities.Appointment
import domain.models.AppointmentStatus
import java.time.LocalDateTime
import java.time.ZoneId

class InMemoryAppointmentRepository : AppointmentRepository {
    private val storage = mutableMapOf<AppointmentId, Appointment>()
    override suspend fun findByPatientId(patientId: PatientId): Result<List<Appointment>> =
        Result.success(storage.values.filter { it.patientId == patientId }
            .sortedByDescending { it.scheduledTime })

    override suspend fun findByDoctorId(doctorId: DoctorId): Result<List<Appointment>> =
        Result.success(storage.values.filter { it.doctorId == doctorId }
            .sortedBy { it.scheduledTime })

    override suspend fun findByDateRange(
        startDate: Long,
        endTime: Long,
    ): Result<List<Appointment>> =
        Result.success(
            storage.values.filter {
                val appointmentTime = ZoneId.systemDefault()
                    .let { zoneId ->
                        it.scheduledTime.atZone(zoneId).toInstant().toEpochMilli()
                    }
                appointmentTime in startDate..endTime
            }
        )

    override suspend fun findByStatus(status: AppointmentStatus): Result<List<Appointment>> =
        Result.success(storage.values.filter { it.status == status })

    override suspend fun findUpComingAppointments(patientId: PatientId): Result<List<Appointment>> {
        val now = LocalDateTime.now()
        return Result.success(
            storage.values.filter {
                it.patientId == patientId &&
                        it.scheduledTime.isAfter(now) &&
                        it.status in listOf(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED)
            }.sortedBy { it.scheduledTime }
        )
    }

    override suspend fun checkDoctorAvailability(
        doctorId: DoctorId,
        dateTime: LocalDateTime,
    ): Result<Boolean> {
        val hasConflicts = storage.values.any {
            it.doctorId == doctorId &&
                    it.status !in listOf(AppointmentStatus.CANCELLED, AppointmentStatus.COMPLETED) &&
                    (dateTime.isAfter(it.scheduledTime) && dateTime.isBefore(it.endTime) ||
                            dateTime.isEqual(it.scheduledTime))
        }
        return Result.success(!hasConflicts)
    }

    override suspend fun findById(id: AppointmentId): Result<Appointment> =
        storage[id]?.let { Result.success(it) }
            ?: Result.failure(DomainError.NotFoundError("Appointment", id.value))

    override suspend fun save(entity: Appointment): Result<Appointment> {
        if (storage.containsKey(entity.id)) {
            return Result.failure(DomainError.ConflictError("Appointment with id ${entity.id.value} already exists"))
        } else {
            storage[entity.id] = entity
            return Result.success(entity)
        }
    }

    override suspend fun delete(id: AppointmentId): Result<Boolean> =
        if (storage.remove(id) != null) {
            Result.success(true)
        } else {
            Result.failure(DomainError.NotFoundError("Appointment", id.value))
        }

    override suspend fun update(entity: Appointment): Result<Appointment> {
        if (!storage.containsKey(entity.id)) {
            return Result.failure(DomainError.NotFoundError("Appointment", entity.id.value))
        } else {
            storage[entity.id] = entity.copy(updatedAt = System.currentTimeMillis())
            return Result.success(entity)
        }
    }

    override suspend fun findAll(): Result<List<Appointment>> =
        Result.success(storage.values.toList())
}
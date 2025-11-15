package data.repoistories

import core.functional.Result
import core.utils.AppointmentId
import core.utils.DoctorId
import core.utils.PatientId
import domain.entities.Appointment
import domain.models.AppointmentStatus
import java.time.LocalDateTime

interface AppointmentRepository : Repository<Appointment, AppointmentId> {
    suspend fun findByPatientId(patientId: PatientId): Result<List<Appointment>>
    suspend fun findByDoctorId(doctorId: DoctorId): Result<List<Appointment>>
    suspend fun findByDateRange(startDate: Long, endTime: Long): Result<List<Appointment>>
    suspend fun findByStatus(status: AppointmentStatus): Result<List<Appointment>>
    suspend fun findUpComingAppointments(patientId: PatientId): Result<List<Appointment>>
    suspend fun checkDoctorAvailability(doctorId: DoctorId, dateTime: LocalDateTime): Result<Boolean>
}
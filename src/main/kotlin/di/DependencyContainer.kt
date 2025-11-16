package di

import core.functional.Result
import core.utils.*
import data.datasources.*
import data.repoistories.*
import domain.entities.InventoryItem
import domain.entities.User
import domain.models.UserRole
import domain.usecases.implementation.*
import presentation.viewmodel.*

object DependencyContainer {

    // repositories
    val patientRepository: PatientRepository by lazy {
        InMemoryPatientRepository()
    }

    val doctorRepository: DoctorRepository by lazy {
        InMemoryDoctorRepository()
    }

    val appointmentRepository: AppointmentRepository by lazy {
        InMemoryAppointmentRepository()
    }

    val prescriptionRepository: PrescriptionRepository by lazy {
        InMemoryPrescriptionRepository()
    }

    val invoiceRepository: InvoiceRepository by lazy {
        InMemoryInvoiceRepository()
    }

    val inventoryRepository: InventoryRepository by lazy {
        object : InventoryRepository {
            private val storage = mutableMapOf<String, InventoryItem>()
            override suspend fun findByName(name: String): Result<InventoryItem> =
                storage.values.find { it.medicationName.equals(name, ignoreCase = true) }?.let {
                    Result.success(it)
                } ?: Result.failure(DomainError.NotFoundError("Inventory", "Medication name: $name"))

            override suspend fun findLowStock(): Result<List<InventoryItem>> =
                Result.success(storage.values.filter { it.isLowStock() })

            override suspend fun findExpiringSoon(days: Int): Result<List<InventoryItem>> {
                val currentTime = System.currentTimeMillis()
                val thresholdTime = currentTime + (days * 24 * 60 * 60 * 1000L)
                return Result.success(
                    storage.values.filter {
                        it.expiryDate < thresholdTime && !it.isExpired()
                    }
                )
            }

            override suspend fun findExpired(): Result<List<InventoryItem>> =
                Result.success(storage.values.filter { it.isExpired() })

            override suspend fun searchMedications(query: String): Result<List<InventoryItem>> =
                Result.success(
                    storage.values.filter {
                        it.medicationName.contains(query, ignoreCase = true) ||
                                it.genericName.contains(query, ignoreCase = true)
                    }
                )

            override suspend fun findById(id: String): Result<InventoryItem> =
                storage[id]?.let {
                    Result.success(it)
                } ?: Result.failure(DomainError.NotFoundError("Inventory", "Inventory id: $id"))

            override suspend fun save(entity: InventoryItem): Result<InventoryItem> {
                storage[entity.id] = entity
                return Result.success(entity)
            }

            override suspend fun delete(id: String): Result<Boolean> =
                if (storage.remove(id) != null) Result.success(true)
                else Result.failure(DomainError.NotFoundError("InventoryItem", id))

            override suspend fun update(entity: InventoryItem): Result<InventoryItem> {
                storage[entity.id] = entity
                return Result.success(entity)
            }

            override suspend fun findAll(): Result<List<InventoryItem>> =
                Result.success(storage.values.toList())

        }
    }

    val userRepository: UserRepository by lazy {
        object : UserRepository {
            private val storage = mutableMapOf<String, User>()
            override suspend fun findByEmail(email: Email): Result<User> =
                storage.values.find { it.email == email }?.let { Result.success(it) }
                    ?: Result.failure(DomainError.NotFoundError("User", "Email: $email"))

            override suspend fun findByRole(role: UserRole): Result<List<User>> =
                Result.success(storage.values.filter { it.role == role })

            override suspend fun authenticate(email: Email, password: String): Result<User> =
                storage.values.find { it.email == email && verifyPassword(password, it.passwordHash) }
                    ?.let { Result.success(it) }
                    ?: Result.failure(DomainError.UnauthorizedError("Invalid credentials"))

            override suspend fun findById(id: String): Result<User> =
                storage[id]?.let { Result.success(it) }
                    ?: Result.failure(DomainError.NotFoundError("User", "User Id: $id"))

            override suspend fun save(entity: User): Result<User> {
                storage[entity.id] = entity
                return Result.success(entity)
            }

            override suspend fun delete(id: String): Result<Boolean> =
                if (storage.remove(id) != null) Result.success(true)
                else Result.failure(DomainError.NotFoundError("User", id))

            override suspend fun update(entity: User): Result<User> {
                storage[entity.id] = entity
                return Result.success(entity)
            }

            override suspend fun findAll(): Result<List<User>> =
                Result.success(storage.values.toList())
        }
    }

    // use-cases

    fun provideRegisterPatientUseCase() = RegisterPatientUseCase(
        patientRepository = patientRepository,
        userRepository = userRepository
    )

    fun provideGetPatientProfileUseCase() = GetPatientProfileUseCase(
        patientRepository = patientRepository
    )

    fun provideUpdatePatientProfileUseCase() = UpdatePatientProfileUseCase(
        patientRepository = patientRepository
    )

    fun provideScheduleAppointmentUseCase() = ScheduleAppointmentUseCase(
        appointmentRepository = appointmentRepository,
        patientRepository = patientRepository,
        doctorRepository = doctorRepository
    )

    fun provideConfirmAppointmentUseCase() = ConfirmAppointmentUseCase(
        appointmentRepository = appointmentRepository
    )

    fun provideCancelAppointmentUseCase() = CancelAppointmentUseCase(
        appointmentRepository = appointmentRepository
    )

    fun provideGetPatientAppointmentsUseCase() = GetPatientAppointmentsUseCase(
        appointmentRepository = appointmentRepository
    )

    fun provideCreatePrescriptionUseCase() = CreatePrescriptionUseCase(
        prescriptionRepository = prescriptionRepository,
        appointmentRepository = appointmentRepository
    )

    fun provideDispensePrescriptionUseCase() = DispensePrescriptionUseCase(
        prescriptionRepository = prescriptionRepository,
        inventoryRepository = inventoryRepository
    )

    fun provideGenerateInvoiceUseCase() = GenerateInvoiceUseCase(
        invoiceRepository = invoiceRepository,
        appointmentRepository = appointmentRepository,
        doctorRepository = doctorRepository
    )

    fun provideProcessPaymentUseCase() = ProcessPaymentUseCase(
        invoiceRepository = invoiceRepository
    )

    fun provideGetHospitalStatisticsUseCase() = GetHospitalStatisticsUseCase(
        appointmentRepository = appointmentRepository,
        patientRepository = patientRepository,
        invoiceRepository = invoiceRepository
    )

    // view-models
    fun providePatientDashboardViewModel(patientId: PatientId) =
        PatientDashboardViewModel(
            patientId = patientId,
            getPatientProfile = provideGetPatientProfileUseCase(),
            getPatientAppointments = provideGetPatientAppointmentsUseCase(),
            prescriptionRepository = prescriptionRepository,
            invoiceRepository = invoiceRepository
        )

    fun provideAppointmentBookingViewModel(patientId: PatientId) =
        AppointmentBookingViewModel(
            patientId = patientId,
            doctorRepository = doctorRepository,
            scheduleAppointment = provideScheduleAppointmentUseCase()
        )

    fun provideDoctorScheduleViewModel(doctorId: DoctorId) =
        DoctorScheduleViewModel(
            doctorId = doctorId,
            doctorRepository = doctorRepository,
            appointmentRepository = appointmentRepository
        )

    fun providePharmacyViewModel(pharmacistId: String) =
        PharmacyViewModel(
            pharmacistId = pharmacistId,
            prescriptionRepository = prescriptionRepository,
            inventoryRepository = inventoryRepository,
            dispensePrescription = provideDispensePrescriptionUseCase()
        )

    fun provideAnalyticsDashboardViewModel() =
        AnalyticsDashboardViewModel(
            getStatistics = provideGetHospitalStatisticsUseCase()
        )
}
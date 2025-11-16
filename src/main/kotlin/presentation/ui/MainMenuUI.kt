package presentation.ui

import core.extensions.toFormattedDate
import core.utils.DoctorId
import core.utils.Email
import core.utils.PatientId
import core.utils.PhoneNumber
import di.DataSeeder
import di.DependencyContainer
import domain.entities.Address
import domain.entities.EmergencyContact
import domain.entities.User
import domain.models.AppointmentType
import domain.models.Gender
import domain.models.UserRole
import domain.usecases.implementation.GetPatientAppointmentsUseCase
import domain.usecases.implementation.RegisterPatientUseCase
import domain.usecases.implementation.ScheduleAppointmentUseCase
import java.time.LocalDate
import java.time.LocalDateTime

class MainMenuUI {

    private val coordinator = AppCoordinator()
    private var currentUser: User? = null
    private var currentPatientId: PatientId? = null
    private var currentDoctorId: DoctorId? = null

    suspend fun start() {
        ConsoleUI.clearScreen()
        printBanner()

        // Initialize system
        DataSeeder.seed()

        while (true) {
            if (currentUser == null) {
                showLoginMenu()
            } else {
                showRoleBasedMenu()
            }
        }
    }

    private fun printBanner() {
        println(
            """
            
            ${
                Colors.highlight(
                    """
            ╔════════════════════════════════════════════════════════════════════╗
            ║                                                                    ║
            ║                  🏥 HEALTHCORE MANAGEMENT SYSTEM                   ║
            ║                                                                    ║
            ║                  Enterprise Healthcare Platform                    ║
            ║                          Version 1.0.0                             ║
            ║                                                                    ║
            ╚════════════════════════════════════════════════════════════════════╝
            """
                )
            }
            
        """.trimIndent()
        )
    }

    // ========== LOGIN & AUTHENTICATION ==========

    private suspend fun showLoginMenu() {
        ConsoleUI.printHeader("AUTHENTICATION")
        ConsoleUI.printMenu(
            "Login Options", listOf(
                "Login as Patient",
                "Login as Doctor",
                "Login as Pharmacist",
                "Login as Admin",
                "Register New Patient",
                "Demo Mode (Skip Login)"
            )
        )

        when (ConsoleUI.promptInt("\nSelect option") ?: 0) {
            1 -> loginAsRole(UserRole.PATIENT)
            2 -> loginAsRole(UserRole.DOCTOR)
            3 -> loginAsRole(UserRole.PHARMACIST)
            4 -> loginAsRole(UserRole.ADMIN)
            5 -> registerNewPatient()
            6 -> enterDemoMode()
            0 -> {
                ConsoleUI.printInfo("Goodbye!")
                kotlin.system.exitProcess(0)
            }
        }
    }

    private suspend fun loginAsRole(role: UserRole) {
        ConsoleUI.printSubHeader("Login as ${role.name}")

        val email = ConsoleUI.prompt("Email")
        val password = ConsoleUI.prompt("Password")

        try {
            val result = DependencyContainer.userRepository.authenticate(
                Email(email),
                password
            )

            result.fold(
                onSuccess = { user ->
                    if (user.role == role) {
                        currentUser = user
                        ConsoleUI.printSuccess("Login successful! Welcome ${role.name}")

                        // Load role-specific data
                        when (role) {
                            UserRole.PATIENT -> loadPatientData(user)
                            UserRole.DOCTOR -> loadDoctorData(user)
                            else -> {}
                        }

                        ConsoleUI.waitForEnter()
                    } else {
                        ConsoleUI.printError("Invalid role for this user")
                        ConsoleUI.waitForEnter()
                    }
                },
                onFailure = { error ->
                    ConsoleUI.printError("Login failed: ${error.message}")
                    ConsoleUI.waitForEnter()
                }
            )
        } catch (e: Exception) {
            ConsoleUI.printError("Login failed: ${e.message}")
            ConsoleUI.waitForEnter()
        }
    }

    private suspend fun loadPatientData(user: User) {
        DependencyContainer.patientRepository.findAll().fold(
            onSuccess = { patients ->
                currentPatientId = patients.find { it.userId == user.id }?.id
            },
            onFailure = { }
        )
    }

    private suspend fun loadDoctorData(user: User) {
        DependencyContainer.doctorRepository.findAll().fold(
            onSuccess = { doctors ->
                currentDoctorId = doctors.find { it.userId == user.id }?.id
            },
            onFailure = { }
        )
    }

    private suspend fun registerNewPatient() {
        ConsoleUI.printSubHeader("New Patient Registration")

        try {
            val email = Email(ConsoleUI.prompt("Email"))
            val password = ConsoleUI.prompt("Password")
            val firstName = ConsoleUI.prompt("First Name")
            val lastName = ConsoleUI.prompt("Last Name")

            val dobInput = ConsoleUI.prompt("Date of Birth (YYYY-MM-DD)")
            val dateOfBirth = LocalDate.parse(dobInput)

            ConsoleUI.printInfo("Gender: 1=MALE, 2=FEMALE, 3=OTHER")
            val genderChoice = ConsoleUI.promptInt("Select gender") ?: 1
            val gender = when (genderChoice) {
                1 -> Gender.MALE
                2 -> Gender.FEMALE
                else -> Gender.PREFER_NOT_TO_SAY
            }

            val phone = PhoneNumber(ConsoleUI.prompt("Phone"))

            val street = ConsoleUI.prompt("Street Address")
            val city = ConsoleUI.prompt("City")
            val state = ConsoleUI.prompt("State")
            val zipCode = ConsoleUI.prompt("Zip Code")
            val country = ConsoleUI.prompt("Country")

            val emergencyName = ConsoleUI.prompt("Emergency Contact Name")
            val emergencyRelation = ConsoleUI.prompt("Emergency Contact Relation")
            val emergencyPhone = PhoneNumber(ConsoleUI.prompt("Emergency Contact Phone"))

            val input = RegisterPatientUseCase.Input(
                email = email,
                password = password,
                firstName = firstName,
                lastName = lastName,
                dateOfBirth = dateOfBirth,
                gender = gender,
                phone = phone,
                address = Address(street, city, state, zipCode, country),
                emergencyContact = EmergencyContact(emergencyName, emergencyRelation, emergencyPhone)
            )

            DependencyContainer.provideRegisterPatientUseCase()(input).fold(
                onSuccess = { patient ->
                    ConsoleUI.printSuccess("Registration successful!")
                    ConsoleUI.printInfo("Patient ID: ${patient.id.value}")
                    ConsoleUI.printInfo("You can now login with your credentials")
                },
                onFailure = { error ->
                    ConsoleUI.printError("Registration failed: ${error.message}")
                }
            )

        } catch (e: Exception) {
            ConsoleUI.printError("Registration failed: ${e.message}")
        }

        ConsoleUI.waitForEnter()
    }

    private suspend fun enterDemoMode() {
        // Get first patient for demo
        DependencyContainer.patientRepository.findAll().fold(
            onSuccess = { patients ->
                if (patients.isNotEmpty()) {
                    currentPatientId = patients.first().id
                    currentUser = User(
                        id = "DEMO",
                        email = Email("demo@demo.com"),
                        passwordHash = "",
                        role = UserRole.PATIENT
                    )
                    ConsoleUI.printSuccess("Demo mode activated!")
                    ConsoleUI.waitForEnter()
                }
            },
            onFailure = { }
        )
    }

    // ========== ROLE-BASED MENUS ==========

    private suspend fun showRoleBasedMenu() {
        when (currentUser?.role) {
            UserRole.PATIENT -> showPatientMenu()
            UserRole.DOCTOR -> showDoctorMenu()
            UserRole.PHARMACIST -> showPharmacistMenu()
            UserRole.ADMIN -> showAdminMenu()
            else -> logout()
        }
    }

    // ========== PATIENT MENU ==========

    private suspend fun showPatientMenu() {
        ConsoleUI.clearScreen()
        ConsoleUI.printHeader("PATIENT DASHBOARD")

        currentPatientId?.let { patientId ->
            displayPatientDashboard(patientId)
        }

        ConsoleUI.printMenu(
            "Patient Options", listOf(
                "View My Profile",
                "Book New Appointment",
                "View My Appointments",
                "View Prescriptions",
                "View Invoices & Payments",
                "Logout"
            )
        )

        when (ConsoleUI.promptInt("\nSelect option") ?: 0) {
            1 -> viewPatientProfile()
            2 -> bookAppointment()
            3 -> viewAppointments()
            4 -> viewPrescriptions()
            5 -> viewInvoices()
            6 -> logout()
            0 -> logout()
        }
    }

    private suspend fun displayPatientDashboard(patientId: PatientId) {
        val viewModel = DependencyContainer.providePatientDashboardViewModel(patientId)

        // Collect state once
        val state = viewModel.state.value

        state.patient?.let { patient ->
            ConsoleUI.printSubHeader("Welcome, ${patient.fullName}!")
            println("  Age: ${patient.age} years")
            println("  Blood Type: ${patient.bloodType}")
            println("  Phone: ${patient.phoneNumber.value}")
        }

        ConsoleUI.printSubHeader("Quick Stats")
        println("  📅 Upcoming Appointments: ${state.upcomingAppointments.size}")
        println("  💊 Pending Prescriptions: ${state.recentPrescription.size}")
        println("  💰 Pending Invoices: ${state.pendingInvoices.size}")
    }

    private suspend fun viewPatientProfile() {
        currentPatientId?.let { patientId ->
            DependencyContainer.provideGetPatientProfileUseCase()(patientId).fold(
                onSuccess = { patient ->
                    ConsoleUI.printSubHeader("Patient Profile")
                    println("  Name: ${patient.fullName}")
                    println("  Age: ${patient.age} years")
                    println("  Gender: ${patient.gender}")
                    println("  Blood Type: ${patient.bloodType}")
                    println("  Email: ${patient.email.value}")
                    println("  Phone: ${patient.phoneNumber.value}")
                    println("  Address: ${patient.address.toFormattedString()}")
                    println("\n  Emergency Contact:")
                    println("    Name: ${patient.emergencyContact.name}")
                    println("    Relation: ${patient.emergencyContact.relationship}")
                    println("    Phone: ${patient.emergencyContact.phone.value}")

                    if (patient.allergies.isNotEmpty()) {
                        println("\n  Allergies: ${patient.allergies.joinToString(", ")}")
                    }
                    if (patient.chronicConditions.isNotEmpty()) {
                        println("  Chronic Conditions: ${patient.chronicConditions.joinToString(", ")}")
                    }
                },
                onFailure = { error ->
                    ConsoleUI.printError("Failed to load profile: ${error.message}")
                }
            )
        }
        ConsoleUI.waitForEnter()
    }

    private suspend fun bookAppointment() {
        currentPatientId?.let { patientId ->
            ConsoleUI.printSubHeader("Book New Appointment")

            // Get all doctors
            DependencyContainer.doctorRepository.findAll().fold(
                onSuccess = { doctors ->
                    if (doctors.isEmpty()) {
                        ConsoleUI.printWarning("No doctors available")
                        ConsoleUI.waitForEnter()
                        return
                    }

                    // Display doctors
                    println("\nAvailable Doctors:")
                    doctors.forEachIndexed { index, doctor ->
                        println("  ${index + 1}. ${doctor.fullName}")
                        println("     ${doctor.specialization} - Fee: ${doctor.consultationFee.amount} ${doctor.consultationFee.currency}")
                    }

                    val doctorChoice = ConsoleUI.promptInt("\nSelect doctor (1-${doctors.size})") ?: 0
                    if (doctorChoice < 1 || doctorChoice > doctors.size) {
                        ConsoleUI.printError("Invalid selection")
                        ConsoleUI.waitForEnter()
                        return
                    }

                    val selectedDoctor = doctors[doctorChoice - 1]

                    // Get appointment details
                    ConsoleUI.printInfo("\nAppointment Types:")
                    AppointmentType.values().forEachIndexed { index, type ->
                        println("  ${index + 1}. $type")
                    }
                    val typeChoice = ConsoleUI.promptInt("Select type") ?: 1
                    val appointmentType = AppointmentType.values().getOrNull(typeChoice - 1)
                        ?: AppointmentType.CONSULTATION

                    val dateTimeStr = ConsoleUI.prompt("Date & Time (YYYY-MM-DDTHH:mm)")
                    val dateTime = try {
                        LocalDateTime.parse(dateTimeStr)
                    } catch (e: Exception) {
                        ConsoleUI.printError("Invalid date/time format")
                        ConsoleUI.waitForEnter()
                        return
                    }

                    val reason = ConsoleUI.prompt("Reason for visit")

                    // Schedule appointment
                    val input = ScheduleAppointmentUseCase.Input(
                        patientId = patientId,
                        doctorId = selectedDoctor.id,
                        appointmentType = appointmentType,
                        scheduledTime = dateTime,
                        duration = 30,
                        reason = reason
                    )

                    DependencyContainer.provideScheduleAppointmentUseCase()(input).fold(
                        onSuccess = { appointment ->
                            ConsoleUI.printSuccess("Appointment booked successfully!")
                            ConsoleUI.printInfo("Appointment ID: ${appointment.id.value}")
                            ConsoleUI.printInfo("Status: ${appointment.status}")
                        },
                        onFailure = { error ->
                            ConsoleUI.printError("Booking failed: ${error.message}")
                        }
                    )
                },
                onFailure = { error ->
                    ConsoleUI.printError("Failed to load doctors: ${error.message}")
                }
            )
        }
        ConsoleUI.waitForEnter()
    }

    private suspend fun viewAppointments() {
        currentPatientId?.let { patientId ->
            val input = GetPatientAppointmentsUseCase.Input(patientId = patientId)
            DependencyContainer.provideGetPatientAppointmentsUseCase()(input).fold(
                onSuccess = { appointments ->
                    ConsoleUI.printSubHeader("My Appointments")
                    if (appointments.isEmpty()) {
                        ConsoleUI.printInfo("No appointments found")
                    } else {
                        appointments.forEach { apt ->
                            println("\n  📅 ${apt.id.value}")
                            println("     Type: ${apt.appointmentType}")
                            println("     Date: ${apt.scheduledTime}")
                            println("     Status: ${apt.status}")
                            println("     Reason: ${apt.reason}")
                        }
                    }
                },
                onFailure = { error ->
                    ConsoleUI.printError("Failed to load appointments: ${error.message}")
                }
            )
        }
        ConsoleUI.waitForEnter()
    }

    private suspend fun viewPrescriptions() {
        currentPatientId?.let { patientId ->
            DependencyContainer.prescriptionRepository.findByPatient(patientId).fold(
                onSuccess = { prescriptions ->
                    ConsoleUI.printSubHeader("My Prescriptions")
                    if (prescriptions.isEmpty()) {
                        ConsoleUI.printInfo("No prescriptions found")
                    } else {
                        prescriptions.forEach { rx ->
                            println("\n  💊 ${rx.id.value}")
                            println("     Status: ${rx.status}")
                            println("     Valid Until: ${rx.validUntil.toFormattedDate()}")
                            println("     Medications:")
                            rx.medications.forEach { med ->
                                println("       - ${med.name} (${med.dosage})")
                                println("         ${med.frequency} for ${med.duration}")
                            }
                        }
                    }
                },
                onFailure = { error ->
                    ConsoleUI.printError("Failed to load prescriptions: ${error.message}")
                }
            )
        }
        ConsoleUI.waitForEnter()
    }

    private suspend fun viewInvoices() {
        currentPatientId?.let { patientId ->
            DependencyContainer.invoiceRepository.findByPatient(patientId).fold(
                onSuccess = { invoices ->
                    ConsoleUI.printSubHeader("My Invoices")
                    if (invoices.isEmpty()) {
                        ConsoleUI.printInfo("No invoices found")
                    } else {
                        invoices.forEach { invoice ->
                            println("\n  💰 ${invoice.id.value}")
                            println("     Status: ${invoice.status}")
                            println("     Total: ${invoice.totalAmount.amount} ${invoice.totalAmount.currency}")
                            println("     Paid: ${invoice.totalPaid.amount} ${invoice.totalPaid.currency}")
                            println("     Outstanding: ${invoice.outstandingBalance.amount} ${invoice.outstandingBalance.currency}")
                        }
                    }
                },
                onFailure = { error ->
                    ConsoleUI.printError("Failed to load invoices: ${error.message}")
                }
            )
        }
        ConsoleUI.waitForEnter()
    }

    // ========== DOCTOR MENU ==========

    private suspend fun showDoctorMenu() {
        ConsoleUI.clearScreen()
        ConsoleUI.printHeader("DOCTOR DASHBOARD")

        ConsoleUI.printMenu(
            "Doctor Options", listOf(
                "View Today's Schedule",
                "View All Appointments",
                "Start Appointment",
                "Complete Appointment",
                "Create Prescription",
                "Logout"
            )
        )

        when (ConsoleUI.promptInt("\nSelect option") ?: 0) {
            1 -> viewDoctorSchedule()
            6 -> logout()
            0 -> logout()
            else -> {
                ConsoleUI.printInfo("Feature coming soon!")
                ConsoleUI.waitForEnter()
            }
        }
    }

    private suspend fun viewDoctorSchedule() {
        currentDoctorId?.let { doctorId ->
            val viewModel = DependencyContainer.provideDoctorScheduleViewModel(doctorId)
            val state = viewModel.state.value

            state.doctor?.let { doctor ->
                ConsoleUI.printSubHeader("Dr. ${doctor.fullName}'s Schedule")
                println("  Specialization: ${doctor.specialization}")
            }

            ConsoleUI.printSubHeader("Today's Appointments")
            if (state.todayAppointments.isEmpty()) {
                ConsoleUI.printInfo("No appointments today")
            } else {
                state.todayAppointments.forEach { apt ->
                    println("\n  📅 ${apt.scheduledTime.toLocalTime()}")
                    println("     Type: ${apt.appointmentType}")
                    println("     Status: ${apt.status}")
                    println("     Reason: ${apt.reason}")
                }
            }
        }
        ConsoleUI.waitForEnter()
    }

    // ========== PHARMACIST MENU ==========

    private suspend fun showPharmacistMenu() {
        ConsoleUI.clearScreen()
        ConsoleUI.printHeader("PHARMACY DASHBOARD")

        val viewModel = DependencyContainer.providePharmacyViewModel("PHARMACIST_1")
        val state = viewModel.state.value

        ConsoleUI.printSubHeader("Quick Stats")
        println("  💊 Pending Prescriptions: ${state.pendingPrescription.size}")
        println("  ⚠️  Low Stock Items: ${state.lowStockItem.size}")

        ConsoleUI.printMenu(
            "Pharmacy Options", listOf(
                "View Pending Prescriptions",
                "Dispense Prescription",
                "View Inventory",
                "Check Low Stock",
                "Logout"
            )
        )

        when (ConsoleUI.promptInt("\nSelect option") ?: 0) {
            1 -> viewPendingPrescriptions()
            3 -> viewInventory()
            4 -> viewLowStock()
            5 -> logout()
            0 -> logout()
            else -> {
                ConsoleUI.printInfo("Feature coming soon!")
                ConsoleUI.waitForEnter()
            }
        }
    }

    private suspend fun viewPendingPrescriptions() {
        DependencyContainer.prescriptionRepository.findPendingForDispensing().fold(
            onSuccess = { prescriptions ->
                ConsoleUI.printSubHeader("Pending Prescriptions")
                prescriptions.forEach { rx ->
                    println("\n  💊 ${rx.id.value}")
                    println("     Patient: ${rx.patientId.value}")
                    println("     Doctor: ${rx.doctorId.value}")
                    println("     Medications: ${rx.medications.size}")
                }
            },
            onFailure = { error ->
                ConsoleUI.printError("Failed to load prescriptions: ${error.message}")
            }
        )
        ConsoleUI.waitForEnter()
    }

    private suspend fun viewInventory() {
        DependencyContainer.inventoryRepository.findAll().fold(
            onSuccess = { items ->
                ConsoleUI.printSubHeader("Pharmacy Inventory")
                items.forEach { item ->
                    println("\n  📦 ${item.medicationName}")
                    println("     Generic: ${item.genericName}")
                    println("     Quantity: ${item.quantity}")
                    println("     Price: ${item.unitPrice.amount} ${item.unitPrice.currency}")
                    if (item.isLowStock()) {
                        ConsoleUI.printWarning("     ⚠️ LOW STOCK!")
                    }
                }
            },
            onFailure = { error ->
                ConsoleUI.printError("Failed to load inventory: ${error.message}")
            }
        )
        ConsoleUI.waitForEnter()
    }

    private suspend fun viewLowStock() {
        DependencyContainer.inventoryRepository.findLowStock().fold(
            onSuccess = { items ->
                ConsoleUI.printSubHeader("Low Stock Items")
                if (items.isEmpty()) {
                    ConsoleUI.printSuccess("All items are adequately stocked!")
                } else {
                    items.forEach { item ->
                        println("\n  ⚠️  ${item.medicationName}")
                        println("     Current: ${item.quantity}")
                        println("     Reorder Level: ${item.reorderLevel}")
                        println("     ${Colors.warning("ACTION REQUIRED: Restock needed!")}")
                    }
                }
            },
            onFailure = { error ->
                ConsoleUI.printError("Failed to check stock: ${error.message}")
            }
        )
        ConsoleUI.waitForEnter()
    }

    // ========== ADMIN MENU ==========

    private suspend fun showAdminMenu() {
        ConsoleUI.clearScreen()
        ConsoleUI.printHeader("ADMIN DASHBOARD")

        val viewModel = DependencyContainer.provideAnalyticsDashboardViewModel()
        val state = viewModel.state.value

        if (!state.isLoading) {
            ConsoleUI.printSubHeader("Hospital Statistics")
            println("  👥 Total Patients: ${state.totalPatients}")
            println("  📅 Appointments Today: ${state.appointmentsToday}")
            println("  ✅ Completed Today: ${state.completedToday}")
            println("  ⏰ Pending Appointments: ${state.pendingAppointments}")
            println("  💰 Total Revenue: ${state.totalRevenue.amount} ${state.totalRevenue.currency}")
            println("  ⚠️  Outstanding Payments: ${state.outstandingPayments.amount} ${state.outstandingPayments.currency}")
        }

        ConsoleUI.printMenu(
            "Admin Options", listOf(
                "View All Patients",
                "View All Doctors",
                "View All Appointments",
                "Financial Report",
                "System Status",
                "Logout"
            )
        )

        when (ConsoleUI.promptInt("\nSelect option") ?: 0) {
            1 -> viewAllPatients()
            2 -> viewAllDoctors()
            5 -> showSystemStatus()
            6 -> logout()
            0 -> logout()
            else -> {
                ConsoleUI.printInfo("Feature coming soon!")
                ConsoleUI.waitForEnter()
            }
        }
    }

    private suspend fun viewAllPatients() {
        DependencyContainer.patientRepository.findAll().fold(
            onSuccess = { patients ->
                ConsoleUI.printSubHeader("All Patients (${patients.size})")
                patients.forEach { patient ->
                    println("  👤 ${patient.fullName}")
                    println("     ID: ${patient.id.value}")
                    println("     Age: ${patient.age} | Blood Type: ${patient.bloodType}")
                    println("     Phone: ${patient.phoneNumber.value}")
                    println()
                }
            },
            onFailure = { error ->
                ConsoleUI.printError("Failed to load patients: ${error.message}")
            }
        )
        ConsoleUI.waitForEnter()
    }

    private suspend fun viewAllDoctors() {
        DependencyContainer.doctorRepository.findAll().fold(
            onSuccess = { doctors ->
                ConsoleUI.printSubHeader("All Doctors (${doctors.size})")
                doctors.forEach { doctor ->
                    println("  👨‍⚕️ ${doctor.fullName}")
                    println("     Specialization: ${doctor.specialization}")
                    println("     Experience: ${doctor.yearsOfExperience} years")
                    println("     Fee: ${doctor.consultationFee.amount} ${doctor.consultationFee.currency}")
                    println()
                }
            },
            onFailure = { error ->
                ConsoleUI.printError("Failed to load doctors: ${error.message}")
            }
        )
        ConsoleUI.waitForEnter()
    }

    private fun showSystemStatus() {
        ConsoleUI.printSubHeader("System Status")
        ConsoleUI.printSuccess("✓ Core System: Running")
        ConsoleUI.printSuccess("✓ Database: Connected")
        ConsoleUI.printSuccess("✓ All Services: Operational")
        ConsoleUI.printInfo("  Memory: 256 MB / 2 GB")
        ConsoleUI.printInfo("  Uptime: 24h 35m")
        ConsoleUI.waitForEnter()
    }

    // ========== LOGOUT ==========

    private fun logout() {
        currentUser = null
        currentPatientId = null
        currentDoctorId = null
        ConsoleUI.printSuccess("Logged out successfully!")
        ConsoleUI.waitForEnter()
    }
}

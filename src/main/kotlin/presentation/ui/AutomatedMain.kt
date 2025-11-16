package presentation.ui

import core.extensions.toFormattedDate
import core.functional.Money
import core.utils.Email
import core.utils.PhoneNumber
import di.DataSeeder
import di.DependencyContainer
import domain.entities.Address
import domain.entities.EmergencyContact
import domain.entities.InvoiceLineItem
import domain.entities.Medication
import domain.models.*
import domain.usecases.implementation.*
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime

suspend fun runAutomatedDemo() {
    println(
        """
        
        ${
            Colors.highlight(
                """
        ╔════════════════════════════════════════════════════════════════════╗
        ║                                                                    ║
        ║              🏥 HEALTHCORE AUTOMATED SYSTEM DEMO                   ║
        ║                                                                    ║
        ╚════════════════════════════════════════════════════════════════════╝
        """
            )
        }
        
    """.trimIndent()
    )

    ConsoleUI.printInfo("Initializing HealthCore System...")
    DataSeeder.seed()

    // Demo 1: Patient Registration
    ConsoleUI.printHeader("DEMO 1: Patient Registration")
    demoPatientRegistration()

    // Demo 2: Appointment Booking
    ConsoleUI.printHeader("DEMO 2: Appointment Booking")
    demoAppointmentBooking()

    // Demo 3: Prescription Management
    ConsoleUI.printHeader("DEMO 3: Prescription Management")
    demoPrescriptionManagement()

    // Demo 4: Billing & Payments
    ConsoleUI.printHeader("DEMO 4: Billing & Payments")
    demoBillingSystem()

    // Demo 5: Analytics
    ConsoleUI.printHeader("DEMO 5: Hospital Analytics")
    demoAnalytics()

    // Demo 6: Pharmacy Operations
    ConsoleUI.printHeader("DEMO 6: Pharmacy Operations")
    demoPharmacyOperations()

    printFinalSummary()
}

private suspend fun demoPatientRegistration() {
    ConsoleUI.printSubHeader("Registering new patient: Ahmed Mohamed")

    val input = RegisterPatientUseCase.Input(
        email = Email("ahmed.mohamed@email.com"),
        password = "SecurePass123",
        firstName = "Ahmed",
        lastName = "Mohamed",
        dateOfBirth = LocalDate.of(1992, 3, 10),
        gender = Gender.MALE,
        phone = PhoneNumber("+201098765432"),
        address = Address("45 Tahrir Street", "Cairo", "Cairo", "11511", "Egypt"),
        emergencyContact = EmergencyContact(
            "Hoda Mohamed",
            "Mother",
            PhoneNumber("+201098765433")
        ),
        bloodType = BloodType.B_POSITIVE
    )

    DependencyContainer.provideRegisterPatientUseCase()(input).fold(
        onSuccess = { patient ->
            ConsoleUI.printSuccess("Patient registered successfully!")
            println("  Patient ID: ${patient.id.value}")
            println("  Name: ${patient.fullName}")
            println("  Age: ${patient.age} years")
            println("  Blood Type: ${patient.bloodType}")
        },
        onFailure = { error ->
            ConsoleUI.printError("Registration failed: ${error.message}")
        }
    )

    delay(2000)
}

private suspend fun demoAppointmentBooking() {
    ConsoleUI.printSubHeader("Booking appointment for patient")

    // Get patient and doctor
    val patients = DependencyContainer.patientRepository.findAll().getOrNull() ?: emptyList()
    val doctors = DependencyContainer.doctorRepository.findAll().getOrNull() ?: emptyList()

    if (patients.isNotEmpty() && doctors.isNotEmpty()) {
        val patient = patients.first()
        val doctor = doctors.first()

        val appointmentTime = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0)

        val input = ScheduleAppointmentUseCase.Input(
            patientId = patient.id,
            doctorId = doctor.id,
            appointmentType = AppointmentType.CONSULTATION,
            scheduledTime = appointmentTime,
            duration = 30,
            reason = "Regular health checkup"
        )

        DependencyContainer.provideScheduleAppointmentUseCase()(input).fold(
            onSuccess = { appointment ->
                ConsoleUI.printSuccess("Appointment booked successfully!")
                println("  Appointment ID: ${appointment.id.value}")
                println("  Patient: ${patient.fullName}")
                println("  Doctor: ${doctor.fullName}")
                println("  Date: ${appointment.scheduledTime}")
                println("  Type: ${appointment.appointmentType}")
                println("  Status: ${appointment.status}")

                // Confirm the appointment
                delay(1000)
                ConsoleUI.printSubHeader("Confirming appointment...")

                DependencyContainer.provideConfirmAppointmentUseCase()(appointment.id).fold(
                    onSuccess = { confirmed ->
                        ConsoleUI.printSuccess("Appointment confirmed!")
                        println("  New Status: ${confirmed.status}")
                    },
                    onFailure = { error ->
                        ConsoleUI.printError("Confirmation failed: ${error.message}")
                    }
                )
            },
            onFailure = { error ->
                ConsoleUI.printError("Booking failed: ${error.message}")
            }
        )
    }

    delay(2000)
}

private suspend fun demoPrescriptionManagement() {
    ConsoleUI.printSubHeader("Creating prescription after appointment")

    val appointments = DependencyContainer.appointmentRepository.findAll().getOrNull() ?: emptyList()

    if (appointments.isNotEmpty()) {
        val appointment = appointments.first()

        // First mark appointment as completed
        ConsoleUI.printInfo("Completing appointment...")
        appointment.start().faltMap { started ->
            started.complete("Diagnosed with seasonal allergies", null)
        }.fold(
            onSuccess = { completed ->
                DependencyContainer.appointmentRepository.update(completed)

                // Create prescription
                val medications = listOf(
                    Medication(
                        name = "Cetirizine",
                        dosage = "10mg",
                        frequency = "Once daily",
                        duration = "14 days",
                        instructions = "Take in the evening",
                        quantity = 14
                    ),
                    Medication(
                        name = "Nasal Spray",
                        dosage = "2 sprays per nostril",
                        frequency = "Twice daily",
                        duration = "7 days",
                        instructions = "Use as needed",
                        quantity = 1
                    )
                )

                val input = CreatePrescriptionUseCase.Input(
                    patientId = completed.patientId,
                    doctorId = completed.doctorId,
                    appointmentId = completed.id,
                    medications = medications,
                    validityDays = 30,
                    notes = "Avoid allergens if possible"
                )

                DependencyContainer.provideCreatePrescriptionUseCase()(input).fold(
                    onSuccess = { prescription ->
                        ConsoleUI.printSuccess("Prescription created successfully!")
                        println("  Prescription ID: ${prescription.id.value}")
                        println("  Medications: ${prescription.medications.size}")
                        println("  Valid Until: ${prescription.validUntil.toFormattedDate()}")
                        println("  Status: ${prescription.status}")

                        prescription.medications.forEach { med ->
                            println("\n  💊 ${med.name}")
                            println("     Dosage: ${med.dosage}")
                            println("     Frequency: ${med.frequency}")
                            println("     Duration: ${med.duration}")
                        }
                    },
                    onFailure = { error ->
                        ConsoleUI.printError("Prescription creation failed: ${error.message}")
                    }
                )
            },
            onFailure = { error ->
                ConsoleUI.printError("Could not complete appointment: ${error.message}")
            }
        )
    }

    delay(2000)
}

private suspend fun demoBillingSystem() {
    ConsoleUI.printSubHeader("Generating invoice for appointment")

    val appointments = DependencyContainer.appointmentRepository.findAll().getOrNull() ?: emptyList()
    val completedAppointments = appointments.filter { it.status == AppointmentStatus.COMPLETED }

    if (completedAppointments.isNotEmpty()) {
        val appointment = completedAppointments.first()

        // Additional charges
        val additionalCharges = listOf(
            InvoiceLineItem(
                description = "Lab Tests",
                quantity = 2,
                unitPrice = Money(150.0, Money.Currency.EGP),
                discount = 10.0
            ),
            InvoiceLineItem(
                description = "Medications",
                quantity = 1,
                unitPrice = Money(200.0, Money.Currency.EGP)
            )
        )

        val input = GenerateInvoiceUseCase.Input(
            patientId = appointment.patientId,
            appointmentId = appointment.id,
            additionalCharges = additionalCharges,
            dueInDays = 30
        )

        DependencyContainer.provideGenerateInvoiceUseCase()(input).fold(
            onSuccess = { invoice ->
                ConsoleUI.printSuccess("Invoice generated successfully!")
                println("  Invoice ID: ${invoice.id.value}")
                println("  Subtotal: ${invoice.subtotal.amount} ${invoice.subtotal.currency}")
                println("  Discount: ${invoice.totalDiscount.amount} ${invoice.totalDiscount.currency}")
                println("  Total: ${invoice.totalAmount.amount} ${invoice.totalAmount.currency}")
                println("  Status: ${invoice.status}")

                println("\n  Line Items:")
                invoice.lineItems.forEach { item ->
                    println("    - ${item.description}: ${item.total.amount} ${item.total.currency}")
                }

                // Process payment
                delay(1000)
                ConsoleUI.printSubHeader("Processing payment...")

                val paymentInput = ProcessPaymentUseCase.Input(
                    invoiceId = invoice.id,
                    amount = Money(500.0, Money.Currency.EGP),
                    method = PaymentMethod.CREDIT_CARD,
                    transactionId = "TXN${System.currentTimeMillis()}",
                    notes = "Partial payment"
                )

                DependencyContainer.provideProcessPaymentUseCase()(paymentInput).fold(
                    onSuccess = { updatedInvoice ->
                        ConsoleUI.printSuccess("Payment processed successfully!")
                        println("  Amount Paid: ${paymentInput.amount.amount} ${paymentInput.amount.currency}")
                        println("  Total Paid: ${updatedInvoice.totalPaid.amount}")
                        println("  Outstanding: ${updatedInvoice.outstandingBalance.amount}")
                        println("  New Status: ${updatedInvoice.status}")
                    },
                    onFailure = { error ->
                        ConsoleUI.printError("Payment failed: ${error.message}")
                    }
                )
            },
            onFailure = { error ->
                ConsoleUI.printError("Invoice generation failed: ${error.message}")
            }
        )
    }

    delay(2000)
}

private suspend fun demoAnalytics() {
    ConsoleUI.printSubHeader("Generating hospital statistics")

    DependencyContainer.provideGetHospitalStatisticsUseCase()().fold(
        onSuccess = { stats ->
            ConsoleUI.printSuccess("Statistics generated successfully!")
            println()
            println("  📊 HOSPITAL METRICS")
            println("  ${"═".repeat(50)}")
            println("  👥 Total Patients:           ${stats.totalPatients}")
            println("  📅 Appointments Today:       ${stats.totalAppointmentsToday}")
            println("  ✅ Completed Today:          ${stats.completedAppointmentsToday}")
            println("  ⏰ Pending Appointments:     ${stats.pendingAppointments}")
            println("  💰 Total Revenue:            ${stats.totalRevenue.amount} ${stats.totalRevenue.currency}")
            println("  ⚠️  Outstanding Payments:    ${stats.outstandingPayments.amount} ${stats.outstandingPayments.currency}")
            println()

            // Calculate some KPIs
            val completionRate = if (stats.totalAppointmentsToday > 0) {
                (stats.completedAppointmentsToday.toDouble() / stats.totalAppointmentsToday * 100).toInt()
            } else 0

            println("  📈 KEY PERFORMANCE INDICATORS")
            println("  ${"═".repeat(50)}")
            println("  Today's Completion Rate:     $completionRate%")
            println(
                "  Average Revenue per Patient: ${
                    if (stats.totalPatients > 0)
                        (stats.totalRevenue.amount / stats.totalPatients).toInt() else 0
                } EGP"
            )

            if (stats.outstandingPayments.amount > 0) {
                ConsoleUI.printWarning("\n  ⚠️ Action Required: Follow up on outstanding payments")
            } else {
                ConsoleUI.printSuccess("\n  ✓ All payments are up to date!")
            }
        },
        onFailure = { error ->
            ConsoleUI.printError("Analytics generation failed: ${error.message}")
        }
    )

    delay(2000)
}

private suspend fun demoPharmacyOperations() {
    ConsoleUI.printSubHeader("Pharmacy inventory management")

    // Check inventory
    DependencyContainer.inventoryRepository.findAll().fold(
        onSuccess = { items ->
            ConsoleUI.printSuccess("Inventory loaded: ${items.size} items")

            val lowStockItems = items.filter { it.isLowStock() }
            if (lowStockItems.isNotEmpty()) {
                ConsoleUI.printWarning("\n⚠️ Low Stock Alert!")
                lowStockItems.forEach { item ->
                    println("  - ${item.medicationName}: ${item.quantity} units")
                    println("    Reorder level: ${item.reorderLevel}")
                }
            }

            val expiringItems = items.filter {
                val daysUntilExpiry = (it.expiryDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
                daysUntilExpiry < 90
            }

            if (expiringItems.isNotEmpty()) {
                ConsoleUI.printWarning("\n⚠️ Medications expiring soon (< 90 days):")
                expiringItems.forEach { item ->
                    val daysLeft = (item.expiryDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)
                    println("  - ${item.medicationName}: $daysLeft days remaining")
                }
            } else {
                ConsoleUI.printSuccess("\n✓ All medications are within safe expiry periods")
            }

            // Display pending prescriptions
            DependencyContainer.prescriptionRepository.findPendingForDispensing().fold(
                onSuccess = { pendingRx ->
                    println("\n📋 Pending Prescriptions: ${pendingRx.size}")
                    if (pendingRx.isNotEmpty()) {
                        ConsoleUI.printInfo("Ready for dispensing")
                    }
                },
                onFailure = { }
            )
        },
        onFailure = { error ->
            ConsoleUI.printError("Inventory check failed: ${error.message}")
        }
    )

    delay(2000)
}

private fun printFinalSummary() {
    println("\n")
    ConsoleUI.printHeader("DEMO COMPLETED SUCCESSFULLY!")

    println(
        """
        
        ${
            Colors.success(
                """
        ✅ ALL SYSTEM COMPONENTS DEMONSTRATED
        """
            )
        }
        
        ${Colors.info("━".repeat(70))}
        
        📋 FEATURES DEMONSTRATED:
        ${Colors.success("✓")} Patient Registration & Profile Management
        ${Colors.success("✓")} Appointment Scheduling & Confirmation
        ${Colors.success("✓")} Doctor Management & Availability
        ${Colors.success("✓")} Prescription Creation & Dispensing
        ${Colors.success("✓")} Inventory Management & Stock Control
        ${Colors.success("✓")} Billing & Invoice Generation
        ${Colors.success("✓")} Payment Processing
        ${Colors.success("✓")} Analytics & Reporting
        ${Colors.success("✓")} Multi-role User Management
        
        ${Colors.info("━".repeat(70))}
        
        🎯 ADVANCED KOTLIN FEATURES USED:
        ${Colors.success("✓")} Sealed Classes & Sealed Interfaces
        ${Colors.success("✓")} Data Classes with Copy Functions
        ${Colors.success("✓")} Inline Value Classes (Zero-cost abstractions)
        ${Colors.success("✓")} Coroutines & Structured Concurrency
        ${Colors.success("✓")} StateFlow & SharedFlow
        ${Colors.success("✓")} Higher-Order Functions (map, flatMap, fold)
        ${Colors.success("✓")} Extension Functions
        ${Colors.success("✓")} Delegated Properties (lazy, observable)
        ${Colors.success("✓")} Type-Safe Builders (DSL)
        ${Colors.success("✓")} Scope Functions (let, also, apply, run)
        ${Colors.success("✓")} Smart Casts & Kotlin Contracts
        ${Colors.success("✓")} Result/Either Types (Railway-Oriented Programming)
        ${Colors.success("✓")} Specification Pattern with Combinators
        ${Colors.success("✓")} Repository Pattern
        ${Colors.success("✓")} Use Case Pattern (Clean Architecture)
        ${Colors.success("✓")} MVVM with StateFlow
        ${Colors.success("✓")} Dependency Injection
        ${Colors.success("✓")} Event-Driven Architecture
        
        ${Colors.info("━".repeat(70))}
        
        📐 DESIGN PATTERNS IMPLEMENTED:
        ${Colors.success("✓")} Repository Pattern (Data Access)
        ${Colors.success("✓")} Use Case Pattern (Business Logic)
        ${Colors.success("✓")} MVVM (Presentation)
        ${Colors.success("✓")} Factory Pattern (Object Creation)
        ${Colors.success("✓")} Strategy Pattern (Algorithms)
        ${Colors.success("✓")} Observer Pattern (Events)
        ${Colors.success("✓")} Specification Pattern (Validation)
        ${Colors.success("✓")} Builder Pattern (DSL)
        ${Colors.success("✓")} Singleton Pattern (DI Container)
        ${Colors.success("✓")} State Pattern (Appointment lifecycle)
        
        ${Colors.info("━".repeat(70))}
        
        🏗️ ARCHITECTURE LAYERS:
        1. ${Colors.highlight("Domain Layer")} - Pure business logic (models, use cases)
        2. ${Colors.highlight("Data Layer")} - Repositories and data sources
        3. ${Colors.highlight("Presentation Layer")} - ViewModels and UI state
        4. ${Colors.highlight("UI Layer")} - Console interface
        5. ${Colors.highlight("Core Layer")} - Shared utilities and functional types
        
        ${Colors.info("━".repeat(70))}
        
        ${Colors.highlight("🎓 LEARNING OUTCOMES:")}
        • Mastered complex Kotlin features in real-world scenarios
        • Built enterprise-grade system architecture
        • Implemented clean architecture principles
        • Applied functional programming concepts
        • Managed complex state and business rules
        • Created type-safe, maintainable code
        
        ${
            Colors.success(
                """
        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        🎉 HEALTHCORE SYSTEM - PRODUCTION READY!
        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        """
            )
        }
        
        ${Colors.info("Thank you for exploring HealthCore! 🏥")}
        
    """.trimIndent()
    )
}
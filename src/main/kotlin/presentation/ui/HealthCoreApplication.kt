package presentation.ui

import di.DataSeeder
import di.DependencyContainer
import domain.entities.Doctor
import domain.entities.Patient
import domain.models.AppointmentType
import domain.usecases.implementation.ScheduleAppointmentUseCase
import java.time.LocalDateTime

class HealthCoreApplication {

    private val coordinator = AppCoordinator()

    suspend fun start() {
        printWelcomeBanner()

        // Initialize database
        println("\n🔄 Initializing database...")
        DataSeeder.seed()

        // Start application
        println("\n🚀 HealthCore System Started!")
        println("=".repeat(60))

        // Demo: Show system capabilities
        demonstrateSystemFeatures()
    }

    private fun printWelcomeBanner() {
        println(
            """
            
            ╔════════════════════════════════════════════════════════════╗
            ║                                                            ║
            ║              🏥 HEALTHCORE MANAGEMENT SYSTEM               ║
            ║                                                            ║
            ║              Enterprise Healthcare Platform                ║
            ║                    Version 1.0.0                           ║
            ║                                                            ║
            ╚════════════════════════════════════════════════════════════╝
            
        """.trimIndent()
        )
    }

    private suspend fun demonstrateSystemFeatures() {
        println("\n📊 SYSTEM DEMONSTRATION")
        println("=".repeat(60))

        // Get sample data
        val patients = DependencyContainer.patientRepository.findAll().getOrNull() ?: emptyList()
        val doctors = DependencyContainer.doctorRepository.findAll().getOrNull() ?: emptyList()

        println("\n✓ Total Patients: ${patients.size}")
        patients.take(2).forEach { patient ->
            println("  - ${patient.fullName} (${patient.age} years, ${patient.bloodType})")
        }

        println("\n✓ Total Doctors: ${doctors.size}")
        doctors.forEach { doctor ->
            println("  - ${doctor.fullName} - ${doctor.specialization}")
            println("    Consultation Fee: ${doctor.consultationFee.amount} ${doctor.consultationFee.currency}")
        }

        // Demonstrate appointment booking
        if (patients.isNotEmpty() && doctors.isNotEmpty()) {
            demonstrateAppointmentBooking(patients.first(), doctors.first())
        }

        // Show analytics
        demonstrateAnalytics()
    }

    private suspend fun demonstrateAppointmentBooking(patient: Patient, doctor: Doctor) {
        println("\n📅 BOOKING APPOINTMENT DEMO")
        println("-".repeat(60))

        val scheduleUseCase = DependencyContainer.provideScheduleAppointmentUseCase()
        val input = ScheduleAppointmentUseCase.Input(
            patientId = patient.id,
            doctorId = doctor.id,
            appointmentType = AppointmentType.CONSULTATION,
            scheduledTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
            duration = 30,
            reason = "Regular checkup"
        )

        scheduleUseCase(input).fold(
            onSuccess = { appointment ->
                println("✓ Appointment booked successfully!")
                println("  Patient: ${patient.fullName}")
                println("  Doctor: ${doctor.fullName}")
                println("  Date: ${appointment.scheduledTime}")
                println("  Status: ${appointment.status}")
            },
            onFailure = { error ->
                println("✗ Failed to book appointment: ${error.message}")
            }
        )
    }

    private suspend fun demonstrateAnalytics() {
        println("\n📈 HOSPITAL ANALYTICS")
        println("-".repeat(60))

        val statsUseCase = DependencyContainer.provideGetHospitalStatisticsUseCase()
        statsUseCase().fold(
            onSuccess = { stats ->
                println("✓ Total Patients: ${stats.totalPatients}")
                println("✓ Appointments Today: ${stats.totalAppointmentsToday}")
                println("✓ Completed Today: ${stats.completedAppointmentsToday}")
                println("✓ Pending Appointments: ${stats.pendingAppointments}")
                println("✓ Total Revenue: ${stats.totalRevenue.amount} ${stats.totalRevenue.currency}")
                println("✓ Outstanding Payments: ${stats.outstandingPayments.amount} ${stats.outstandingPayments.currency}")
            },
            onFailure = { error ->
                println("✗ Failed to load analytics: ${error.message}")
            }
        )
    }
}
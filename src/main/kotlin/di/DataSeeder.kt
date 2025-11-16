package di

import core.extensions.random
import core.functional.Money
import core.utils.Email
import core.utils.PhoneNumber
import domain.entities.*
import domain.models.BloodType
import domain.models.Gender
import domain.models.MedicalDepartment
import domain.models.UserRole
import domain.usecases.implementation.RegisterPatientUseCase
import java.time.LocalDate

object DataSeeder {
    suspend fun seed() {
        seedPatients()
        seedDoctors()
        seedInventory()
        println("✅ Database seeded successfully!")
    }

    private suspend fun seedPatients() {
        val patients = listOf(
            RegisterPatientUseCase.Input(
                email = Email("john.doe@email.com"),
                password = "Password123",
                firstName = "John",
                lastName = "Doe",
                dateOfBirth = LocalDate.of(1985, 5, 15),
                gender = Gender.MALE,
                phone = PhoneNumber("+201234567890"),
                address = Address("123 Main St", "Cairo", "Cairo", "11511", "Egypt"),
                emergencyContact = EmergencyContact("Jane Doe", "Wife", PhoneNumber("+201234567891")),
                bloodType = BloodType.O_POSITIVE
            ),
            RegisterPatientUseCase.Input(
                email = Email("sarah.smith@email.com"),
                password = "Password123",
                firstName = "Sarah",
                lastName = "Smith",
                dateOfBirth = LocalDate.of(1990, 8, 22),
                gender = Gender.FEMALE,
                phone = PhoneNumber("+201234567892"),
                address = Address("456 Oak Ave", "Alexandria", "Alexandria", "21500", "Egypt"),
                emergencyContact = EmergencyContact("Mike Smith", "Husband", PhoneNumber("+201234567893")),
                bloodType = BloodType.A_POSITIVE
            )
        )

        val registerUseCase = DependencyContainer.provideRegisterPatientUseCase()
        patients.forEach { input ->
            registerUseCase(input).fold(
                onSuccess = { println("✓ Created patient: ${it.fullName}") },
                onFailure = { println("✗ Failed to create patient: ${it.message}") }
            )
        }
    }

    private suspend fun seedDoctors() {
        val doctors = listOf(
            Triple(
                "dr.ahmed@hospital.com",
                listOf("MD", "Cardiology Board Certified"),
                MedicalDepartment.CARDIOLOGY
            ) to ("Ahmed" to "Hassan"),
            Triple(
                "dr.fatma@hospital.com",
                listOf("MD", "Pediatrics Specialist"),
                MedicalDepartment.PEDIATRICS
            ) to ("Fatma" to "Ibrahim"),
            Triple(
                "dr.mohamed@hospital.com",
                listOf("MD", "Orthopedic Surgeon"),
                MedicalDepartment.ORTHOPEDICS
            ) to ("Mohamed" to "Ali")
        )

        for ((triple, name) in doctors) {
            val (email, qualification, department) = triple
            val (firstName, lastName) = name

            val userResult = User.create(
                Email(email),
                "Password123",
                UserRole.DOCTOR
            )

            userResult.fold(
                onSuccess = { user ->
                    DependencyContainer.userRepository.save(user)

                    Doctor.create(
                        firstName = firstName,
                        lastName = lastName,
                        specialization = department,
                        licenseNumber = "LTC${(100000..999999).random()}",
                        qualifications = qualification,
                        phone = PhoneNumber("+2010${(10000000..99999999).random()}"),
                        email = Email(email),
                        consultationFee = Money(500.0, Money.Currency.EGP),
                        yearsOfExperience = (5..20).random()
                    ).fold(
                        onSuccess = { doctor ->
                            DependencyContainer.doctorRepository.save(doctor)
                            println("✓ Created doctor: ${doctor.fullName}")
                        },
                        onFailure = { println("✗ Failed to create doctor: ${it.message}") }
                    )
                },
                onFailure = { println("✗ Failed to create user: ${it.message}") }
            )
        }
    }

    private suspend fun seedInventory() {
        val medications = listOf(
            "Aspirin" to "Acetylsalicylic Acid",
            "Paracetamol" to "Acetaminophen",
            "Amoxicillin" to "Amoxicillin",
            "Metformin" to "Metformin HCl",
            "Omeprazole" to "Omeprazole"
        )

        for ((brand, generic) in medications) {
            InventoryItem.create(
                medicationName = brand,
                genericName = generic,
                manufacturer = "Pharma Co.",
                batchNumber = "BATCH${(1000..9999).random()}",
                expiryDate = System.currentTimeMillis() + (365 * 24 * 60 * 60 * 1000L),
                quantity = (100..500).random(),
                unitPrice = Money((10.0..100.0).random(), Money.Currency.EGP),
                reorderLevel = 50,
                location = "Shelf ${('A'..'Z').random()}-${(1..20).random()}"
            ).fold(
                onSuccess = { item ->
                    DependencyContainer.inventoryRepository.save(item)
                    println("✓ Added medication: ${item.medicationName}")
                },
                onFailure = { println("✗ Failed to add medication: ${it.message}") }
            )
        }
    }
}
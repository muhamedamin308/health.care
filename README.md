# 🏥 HealthCare - Hospital Management System

A complete enterprise-grade Hospital Management System built with advanced Kotlin features, demonstrating Clean Architecture and professional software design patterns.

## ✨ Features

- 👥 **Patient Management** - Registration, profiles, medical history
- 📅 **Appointments** - Scheduling, confirmation, tracking
- 👨‍⚕️ **Doctor Management** - Specializations, schedules, availability
- 💊 **Prescriptions** - Digital prescriptions with medication tracking
- 📦 **Pharmacy & Inventory** - Stock management, expiry tracking
- 💰 **Billing & Payments** - Invoice generation, payment processing
- 📊 **Analytics** - Hospital statistics and reports
- 🔐 **Multi-Role Access** - Patient, Doctor, Pharmacist, Admin portals

## 🚀 Quick Start

### Prerequisites
- JDK 11+
- Kotlin 1.9+

### Run the Application

```bash
git clone git@github.com:muhamedamin308/health.care.git
cd healthcare
./gradlew run
```

### Usage

**Interactive Mode:**
```kotlin
suspend fun main() {
    val mainMenu = MainMenuUI()
    mainMenu.start()
}
```

**Automated Demo:**
```kotlin
suspend fun main() {
    runAutomatedDemo()
}
```

## 📁 Project Structure

```
healthcore/
├── core/           # Functional programming utilities (Result, Error handling)
├── domain/         # Business logic (Models, Use Cases)
├── data/           # Data access (Repositories)
├── presentation/   # ViewModels & State Management
├── ui/             # Console Interface
└── di/             # Dependency Injection
```

## 🏗️ Architecture

Built with **Clean Architecture** principles:
- **Domain Layer** - Business logic independent of frameworks
- **Data Layer** - Repository pattern for data access
- **Presentation Layer** - MVVM with StateFlow
- **UI Layer** - Console-based interface

## 🔥 Advanced Kotlin Features

- ✅ Sealed Classes for type-safe state management
- ✅ Inline Value Classes for zero-cost type safety
- ✅ Coroutines & Flow for async operations
- ✅ Higher-Order Functions (map, flatMap, fold)
- ✅ Extension Functions
- ✅ Delegated Properties (lazy)
- ✅ Railway-Oriented Programming with Result types
- ✅ Specification Pattern for composable business rules
- ✅ Type-Safe DSL Builders

## 📝 Code Example

```kotlin
// Register a patient
val input = RegisterPatientUseCase.Input(
    email = Email("john@example.com"),
    password = "Password123",
    firstName = "John",
    lastName = "Doe",
    dateOfBirth = LocalDate.of(1990, 1, 1),
    gender = Gender.MALE,
    phone = PhoneNumber("+1234567890"),
    address = Address("123 Main St", "Cairo", "Cairo", "11511", "Egypt"),
    emergencyContact = EmergencyContact("Jane", "Wife", PhoneNumber("+1234567891"))
)

registerPatientUseCase(input).fold(
    onSuccess = { patient -> println("Registered: ${patient.fullName}") },
    onFailure = { error -> println("Error: ${error.message}") }
)
```

## 🎯 Design Patterns

- Repository Pattern
- Use Case Pattern
- MVVM
- Factory Pattern
- Specification Pattern
- State Pattern
- Observer Pattern

## 🤝 Contributing

1. Fork the repo
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

MIT License - see [LICENSE](LICENSE) file

## 👨‍💻 Author

**Your Name**
- GitHub: [@muhamedamin308](https://github.com/muhamedamin308)

---

⭐ **Star this repo if you find it helpful!**
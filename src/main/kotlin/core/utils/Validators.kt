package core.utils

import core.functional.Result
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


@JvmInline
value class PatientId(val value: String) {
    init {
        require(value.matches(Regex("^PAT\\\\d{8}\$"))) {
            "Patient id must match pattern PAT########"
        }
    }

    companion object {
        fun generate(): PatientId {
            val randomNumber = (10000000..99999999).random()
            return PatientId("PAT$randomNumber")
        }
    }
}

@JvmInline
value class DoctorId(val value: String) {
    init {
        require(value.matches(Regex("^APT\\d{10}$"))) {
            "Doctor id must match pattern DOC########"
        }
    }

    companion object {
        fun generate(): DoctorId {
            val randomNumber = (10000000..99999999).random()
            return DoctorId("DOC$randomNumber")
        }
    }
}

@JvmInline
value class AppointmentId(val value: String) {
    init {
        require(value.matches(Regex("^APT\\d{10}$"))) {
            "Appointment Id must match pattern APT###########"
        }
    }

    companion object {
        fun generate(): AppointmentId {
            val timestamp = System.currentTimeMillis().toString().takeLast(10)
            return AppointmentId("APT$timestamp")
        }
    }
}

@JvmInline
value class PrescriptionId(val value: String) {
    companion object {
        fun generate(): PrescriptionId {
            val randomNumber = (10000000..999999999).random()
            return PrescriptionId("RX$randomNumber")
        }
    }
}

@JvmInline
value class InvoiceId(val value: String) {
    companion object {
        fun generate(): InvoiceId {
            val timestamp = System.currentTimeMillis().toString().takeLast(10)
            return InvoiceId("INV$timestamp")
        }
    }
}

@JvmInline
value class Email(val value: String) {
    init {
        require(value.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
            "Please, Enter a valid email format...."
        }
    }
}

@JvmInline
value class PhoneNumber(val value: String) {
    init {
        require(value.matches(Regex("^\\+?[1-9]\\d{1,14}$"))) {
            "Please, Enter a valid phone number format...."
        }
    }
}

class ValidationBuilders<T> {
    private val rules = mutableListOf<Pair<(T) -> Boolean, String>>()

    fun rule(message: String, predicate: (T) -> Boolean) {
        rules.add(predicate to message)
    }

    fun validate(value: T): Result<T> {
        rules.forEach { (predicate, message) ->
            if (!predicate(value))
                return Result.failure(DomainError.ValidationError(message))
        }
        return Result.success(value)
    }
}

fun <T> validate(block: ValidationBuilders<T>.() -> Unit): (T) -> Result<T> {
    val builder = ValidationBuilders<T>()
    builder.block()
    return { value -> builder.validate(value) }
}

@OptIn(ExperimentalContracts::class)
fun <T> requireNotNull(value: T?, message: String): T {
    contract {
        returns() implies (value != null)
    }
    return value ?: throw IllegalArgumentException(message)
}
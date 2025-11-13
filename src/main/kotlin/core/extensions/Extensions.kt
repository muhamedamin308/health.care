package core.extensions

import core.functional.Result
import core.functional.Specification
import core.utils.DomainError
import java.text.SimpleDateFormat
import java.util.Date

inline fun <R> R?.toResult(error: () -> DomainError): Result<R> =
    this?.let { Result.success(it) } ?: Result.failure(error())

fun <T> T.validate(specification: Specification<T>): Result<T> =
    if (specification.isSatisfiedBy(this))
        Result.success(this)
    else
        Result.failure(DomainError.ValidationError(specification.errorMessage()))

fun Boolean?.orFalse(): Boolean = this ?: false
fun Boolean?.orTrue(): Boolean = this ?: true

fun <T> List<T>?.orEmpty(): List<T> = this ?: emptyList()

fun Long.toFormattedDate(): String {
    val date = Date(this)
    return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)
}


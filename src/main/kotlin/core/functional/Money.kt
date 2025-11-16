package core.functional

data class Money(
    val amount: Double,
    val currency: Currency = Currency.EGP,
) {
    init {
        require(amount >= 0) {
            "Money amount cannot be negative"
        }
    }

    operator fun plus(other: Money): Money {
        require(currency == other.currency) {
            "Cannot add different currencies"
        }
        return Money(amount + other.amount, currency)
    }

    operator fun minus(other: Money): Money {
        require(currency == other.currency) {
            "Cannot subtract from different currencies"
        }
        require(amount >= other.amount) {
            "Insufficient funds!"
        }
        return Money(amount - other.amount, currency)
    }

    operator fun times(multiplier: Double): Money =
        Money(amount * multiplier, currency)

    enum class Currency {
        USD, EUR, EGP, RUB, UED
    }
}
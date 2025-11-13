package core.functional

interface Specification<T> {
    fun isSatisfiedBy(value: T): Boolean
    fun errorMessage(): String

    infix fun add(other: Specification<T>): Specification<T> =
        object : Specification<T> {
            override fun isSatisfiedBy(value: T): Boolean =
                this@Specification.isSatisfiedBy(value) && other.isSatisfiedBy(value)

            override fun errorMessage(): String =
                "${this@Specification.errorMessage()} AND ${other.errorMessage()}"

        }

    infix fun or(other: Specification<T>): Specification<T> =
        object : Specification<T> {
            override fun isSatisfiedBy(value: T): Boolean =
                this@Specification.isSatisfiedBy(value) || other.isSatisfiedBy(value)

            override fun errorMessage(): String =
                "${this@Specification.errorMessage()} OR ${other.errorMessage()}"

        }

    fun not(): Specification<T> =
        object : Specification<T> {
            override fun isSatisfiedBy(value: T): Boolean = !this@Specification.isSatisfiedBy(value)

            override fun errorMessage(): String =
                "NOT ${this@Specification.errorMessage()}"

        }
}
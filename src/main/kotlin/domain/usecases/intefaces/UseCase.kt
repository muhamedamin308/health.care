package domain.usecases.intefaces

import core.functional.Result

interface UseCase<in INPUT, out OUTPUT> {
    suspend operator fun invoke(input: INPUT): Result<OUTPUT>
}
package domain.usecases.intefaces

import core.functional.Result

interface UseCaseWithoutInputs<out OUTPUT> {
    suspend operator fun invoke(): Result<OUTPUT>
}
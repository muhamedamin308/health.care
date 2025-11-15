package data.repoistories

import core.functional.Result
import core.utils.Email
import domain.entities.User
import domain.models.UserRole

interface UserRepository : Repository<User, String> {
    suspend fun findByEmail(email: Email): Result<User>
    suspend fun findByRole(role: UserRole): Result<List<User>>
    suspend fun authenticate(email: Email, password: String): Result<User>
}
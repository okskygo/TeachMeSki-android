package com.teachmeski.app.data.repository

import com.teachmeski.app.R
import com.teachmeski.app.data.model.toDomain
import com.teachmeski.app.data.remote.UserDataSource
import com.teachmeski.app.domain.model.User
import com.teachmeski.app.domain.repository.UserRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDataSource: UserDataSource,
) : UserRepository {
    override suspend fun getUserById(userId: String): Resource<User> =
        try {
            val dto = userDataSource.getUserById(userId)
            Resource.Success(dto.toDomain())
        } catch (e: Exception) {
            Resource.Error(UiText.StringResource(R.string.auth_error_generic))
        }

    override suspend fun updateDisplayName(userId: String, displayName: String): Resource<Unit> =
        try {
            userDataSource.updateDisplayName(userId, displayName)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(UiText.StringResource(R.string.account_save_error))
        }
}

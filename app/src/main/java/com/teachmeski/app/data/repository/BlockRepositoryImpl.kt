package com.teachmeski.app.data.repository

import com.teachmeski.app.R
import com.teachmeski.app.data.remote.BlockDataSource
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.BlockRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockRepositoryImpl @Inject constructor(
    private val blockDataSource: BlockDataSource,
    private val authRepository: AuthRepository,
) : BlockRepository {

    override suspend fun blockUser(blockedUserId: String): Resource<Unit> {
        return try {
            val userId = authRepository.currentUserId()
                ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
            blockDataSource.blockUser(userId, blockedUserId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(UiText.StringResource(R.string.error_generic))
        }
    }

    override suspend fun unblockUser(blockedUserId: String): Resource<Unit> {
        return try {
            val userId = authRepository.currentUserId()
                ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
            blockDataSource.unblockUser(userId, blockedUserId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(UiText.StringResource(R.string.error_generic))
        }
    }

    override suspend fun amIBlockedBy(otherUserId: String): Boolean {
        return try {
            blockDataSource.amIBlockedBy(otherUserId)
        } catch (e: Exception) {
            false
        }
    }
}

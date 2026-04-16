package com.teachmeski.app.data.repository

import com.teachmeski.app.R
import com.teachmeski.app.data.model.toDomain
import com.teachmeski.app.data.remote.ExploreDataSource
import com.teachmeski.app.data.remote.WalletDataSource
import com.teachmeski.app.domain.model.TokenTransaction
import com.teachmeski.app.domain.model.TokenWallet
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.WalletRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val walletDataSource: WalletDataSource,
    private val exploreDataSource: ExploreDataSource,
    private val authRepository: AuthRepository,
) : WalletRepository {

    private suspend fun getInstructorId(): String? {
        val userId = authRepository.currentUserId() ?: return null
        return exploreDataSource.getInstructorProfileId(userId)
    }

    override suspend fun getWallet(): Resource<TokenWallet> = try {
        val instructorId = getInstructorId()
            ?: return Resource.Error(UiText.StringResource(R.string.error_no_instructor_profile))
        val dto = walletDataSource.getWallet(instructorId)
            ?: return Resource.Success(TokenWallet(balance = 0, instructorId = instructorId))
        Resource.Success(dto.toDomain())
    } catch (e: Exception) {
        Resource.Error(UiText.StringResource(R.string.error_load_wallet))
    }

    override suspend fun getTransactions(page: Int): Resource<Pair<List<TokenTransaction>, Int>> = try {
        val instructorId = getInstructorId()
            ?: return Resource.Error(UiText.StringResource(R.string.error_no_instructor_profile))
        val (dtos, total) = walletDataSource.getTransactions(instructorId, page)
        Resource.Success(Pair(dtos.map { it.toDomain() }, total))
    } catch (e: Exception) {
        Resource.Error(UiText.StringResource(R.string.error_load_wallet))
    }
}

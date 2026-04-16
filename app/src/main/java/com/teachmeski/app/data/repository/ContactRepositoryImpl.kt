package com.teachmeski.app.data.repository

import com.teachmeski.app.R
import com.teachmeski.app.data.remote.ContactDataSource
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.ContactRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl @Inject constructor(
    private val contactDataSource: ContactDataSource,
    private val authRepository: AuthRepository,
) : ContactRepository {

    override suspend fun submitContact(name: String, email: String, message: String): Resource<Unit> =
        try {
            contactDataSource.submitContact(name, email, message, authRepository.currentUserId())
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(UiText.StringResource(R.string.error_submit_contact))
        }
}

package com.teachmeski.app.data.repository

import com.teachmeski.app.R
import com.teachmeski.app.data.model.toDomain
import com.teachmeski.app.data.remote.InstructorDataSource
import com.teachmeski.app.domain.model.InstructorProfile
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.InstructorRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstructorRepositoryImpl @Inject constructor(
    private val instructorDataSource: InstructorDataSource,
    private val authRepository: AuthRepository,
) : InstructorRepository {

    override suspend fun getMyProfile(): Resource<InstructorProfile> = try {
        val userId = authRepository.currentUserId()
            ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
        val email = authRepository.currentUserEmail() ?: ""
        val dto = instructorDataSource.getProfileByUserId(userId)
            ?: return Resource.Error(UiText.StringResource(R.string.error_no_instructor_profile))
        val resortNames = if (dto.resortIds.isNotEmpty()) {
            val resorts = instructorDataSource.getResortNamesByIds(dto.resortIds)
            dto.resortIds.mapNotNull { id ->
                resorts.find { it.id == id }?.let { "${it.nameZh} (${it.nameEn})" }
            }
        } else emptyList()
        Resource.Success(dto.toDomain(email = email, resortNames = resortNames))
    } catch (e: Exception) {
        Resource.Error(UiText.StringResource(R.string.error_load_instructor_profile))
    }

    override suspend fun getProfileByShortId(shortId: String): Resource<InstructorProfile> = try {
        val dto = instructorDataSource.getProfileByShortId(shortId)
            ?: return Resource.Error(UiText.StringResource(R.string.error_no_instructor_profile))
        val resortNames = if (dto.resortIds.isNotEmpty()) {
            val resorts = instructorDataSource.getResortNamesByIds(dto.resortIds)
            dto.resortIds.mapNotNull { id ->
                resorts.find { it.id == id }?.let { "${it.nameZh} (${it.nameEn})" }
            }
        } else emptyList()
        Resource.Success(dto.toDomain(resortNames = resortNames))
    } catch (e: Exception) {
        Resource.Error(UiText.StringResource(R.string.error_load_instructor_profile))
    }

    override suspend fun updateProfile(updates: Map<String, Any?>): Resource<Unit> = try {
        val userId = authRepository.currentUserId()
            ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
        val dto = instructorDataSource.getProfileByUserId(userId)
            ?: return Resource.Error(UiText.StringResource(R.string.error_no_instructor_profile))
        val displayName = updates["display_name"] as? String
        if (displayName != null) {
            instructorDataSource.updateDisplayName(userId, displayName.trim())
        }
        val profileUpdates = updates.filterKeys { it != "display_name" }
        if (profileUpdates.isNotEmpty()) {
            instructorDataSource.updateProfile(dto.id, profileUpdates)
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.StringResource(R.string.error_update_profile))
    }

    override suspend fun toggleAcceptingRequests(isAccepting: Boolean): Resource<Unit> = try {
        val userId = authRepository.currentUserId()
            ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
        val dto = instructorDataSource.getProfileByUserId(userId)
            ?: return Resource.Error(UiText.StringResource(R.string.error_no_instructor_profile))
        instructorDataSource.toggleAcceptingRequests(dto.id, isAccepting)
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.StringResource(R.string.error_update_profile))
    }

    override suspend fun uploadCertificate(bytes: ByteArray, contentType: String): Resource<String> = try {
        val userId = authRepository.currentUserId()
            ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
        val url = instructorDataSource.uploadCertificate(userId, bytes, contentType)
        val dto = instructorDataSource.getProfileByUserId(userId)
            ?: return Resource.Error(UiText.StringResource(R.string.error_no_instructor_profile))
        val updated = dto.certificateUrls + url
        instructorDataSource.updateProfile(dto.id, mapOf("certificate_urls" to updated))
        Resource.Success(url)
    } catch (e: Exception) {
        Resource.Error(UiText.StringResource(R.string.error_upload_certificate))
    }

    override suspend fun deleteCertificate(imageUrl: String): Resource<Unit> = try {
        val userId = authRepository.currentUserId()
            ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
        val dto = instructorDataSource.getProfileByUserId(userId)
            ?: return Resource.Error(UiText.StringResource(R.string.error_no_instructor_profile))
        instructorDataSource.deleteCertificate(dto.id, userId, imageUrl, dto.certificateUrls)
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.StringResource(R.string.error_delete_certificate))
    }

    override suspend fun createProfile(
        discipline: String,
        teachableLevels: List<Int>,
        resortIds: List<String>,
        certifications: List<String>,
        certificationOther: String?,
        displayName: String,
        bio: String?,
        languages: List<String>,
        priceHalfDay: Int?,
        priceFullDay: Int?,
        offersTransport: Boolean,
        offersPhotography: Boolean,
    ): Resource<Unit> = try {
        val userId = authRepository.currentUserId()
            ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
        instructorDataSource.createProfile(
            userId = userId, discipline = discipline, teachableLevels = teachableLevels,
            resortIds = resortIds, certifications = certifications,
            certificationOther = certificationOther, displayName = displayName,
            bio = bio, languages = languages, priceHalfDay = priceHalfDay,
            priceFullDay = priceFullDay, offersTransport = offersTransport,
            offersPhotography = offersPhotography,
        )
        instructorDataSource.updateUserRole(userId, "both")
        try { instructorDataSource.grantWelcomeBonus() } catch (_: Exception) {}
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.StringResource(R.string.error_create_instructor_profile))
    }

    override suspend fun checkProfileExists(): Resource<Boolean> = try {
        val userId = authRepository.currentUserId()
            ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
        Resource.Success(instructorDataSource.checkProfileExists(userId))
    } catch (e: Exception) {
        Resource.Error(UiText.StringResource(R.string.error_generic))
    }
}

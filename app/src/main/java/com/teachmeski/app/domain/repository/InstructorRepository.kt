package com.teachmeski.app.domain.repository

import com.teachmeski.app.domain.model.InstructorProfile
import com.teachmeski.app.util.Resource

interface InstructorRepository {
    suspend fun getMyProfile(): Resource<InstructorProfile>
    suspend fun getProfileByShortId(shortId: String): Resource<InstructorProfile>
    suspend fun updateProfile(updates: Map<String, Any?>): Resource<Unit>
    suspend fun toggleAcceptingRequests(isAccepting: Boolean): Resource<Unit>
    suspend fun uploadCertificate(bytes: ByteArray, contentType: String): Resource<String>
    suspend fun deleteCertificate(imageUrl: String): Resource<Unit>
    suspend fun createProfile(
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
    ): Resource<Unit>
    suspend fun checkProfileExists(): Resource<Boolean>
}

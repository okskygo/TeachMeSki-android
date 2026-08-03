package com.teachmeski.app.domain.repository

import com.teachmeski.app.domain.model.InstructorCertificate
import com.teachmeski.app.domain.model.InstructorProfile
import com.teachmeski.app.domain.model.ReferralSubmissionError
import com.teachmeski.app.util.Resource

interface InstructorRepository {
    suspend fun getMyProfile(): Resource<InstructorProfile>
    suspend fun getProfileByShortId(shortId: String): Resource<InstructorProfile>
    suspend fun updateProfile(updates: Map<String, Any?>): Resource<Unit>
    suspend fun toggleAcceptingRequests(isAccepting: Boolean): Resource<Unit>

    /** F-117: the signed-in instructor's own certificates, all statuses, oldest first. */
    suspend fun getMyCertificates(): Resource<List<InstructorCertificate>>

    /** F-117: approved certificates of any instructor (public detail page). */
    suspend fun getApprovedCertificates(userId: String): Resource<List<InstructorCertificate>>

    /** F-117: upload a certificate image and INSERT a pending `instructor_certificates` row. Client-side limit is 4. */
    suspend fun uploadCertificateImage(bytes: ByteArray, contentType: String): Resource<InstructorCertificate>

    /** F-117: delete a certificate row (owner-only) + best-effort storage removal. */
    suspend fun deleteCertificate(id: String, imageUrl: String): Resource<Unit>

    /** F-117: upload a portfolio image, append to `portfolio_urls`. */
    suspend fun uploadPortfolioImage(bytes: ByteArray, contentType: String): Resource<String>

    /** F-117: remove a portfolio image URL + best-effort storage removal. */
    suspend fun deletePortfolioImage(imageUrl: String): Resource<Unit>
    /**
     * F-116: `referralCode` is the optional code the new instructor typed in
     * during onboarding (submitted via `submit_referral_code` RPC after the
     * profile row is created). `Resource.Success(null)` means the profile
     * was created fine and either no code was entered or it was accepted;
     * a non-null [ReferralSubmissionError] means the profile still succeeded
     * but the referral code itself was rejected — surfaced as a muted,
     * non-blocking notice on the wizard's Success phase (FR-116-010).
     */
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
        referralCode: String?,
    ): Resource<ReferralSubmissionError?>
    suspend fun checkProfileExists(): Resource<Boolean>
}

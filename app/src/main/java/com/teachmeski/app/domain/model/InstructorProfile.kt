package com.teachmeski.app.domain.model

data class InstructorProfile(
    val id: String,
    val userId: String,
    val shortId: String,
    val displayName: String,
    val bio: String?,
    val avatarUrl: String?,
    val email: String,
    val discipline: Discipline,
    val teachableLevels: List<Int>,
    val certifications: List<String>,
    val certificationOther: String?,
    val languages: List<String>,
    val priceHalfDay: Int?,
    val priceFullDay: Int?,
    val offersTransport: Boolean,
    val offersPhotography: Boolean,
    val isAcceptingRequests: Boolean,
    /** F-117 free-form portfolio images (instant public, max 8). */
    val portfolioUrls: List<String>,
    /**
     * F-117 trigger-maintained flag: true when the instructor has at
     * least one approved certificate. Drives the CertifiedBadge TAG.
     */
    val hasVerifiedCertificate: Boolean,
    val ratingAvg: Double?,
    val ratingCount: Int,
    val viewCount: Int,
    /**
     * F-108 LINE identity binding. Non-null means the instructor has
     * completed LINE identity verification and may unlock lesson
     * requests; null means unverified.
     */
    val lineUserId: String?,
    val resortIds: List<String>,
    val resortNames: List<String>,
)

package com.teachmeski.app.domain.model

enum class Discipline(val value: String) {
    Ski("ski"),
    Snowboard("snowboard"),
    Both("both");

    companion object {
        fun fromString(value: String): Discipline =
            when (value) {
                "snowboard" -> Snowboard
                "both" -> Both
                else -> Ski
            }
    }
}

enum class EquipmentRental(val value: String) {
    All("all"),
    Partial("partial"),
    None("none");

    companion object {
        fun fromString(value: String): EquipmentRental =
            when (value) {
                "all" -> All
                "partial" -> Partial
                else -> None
            }
    }
}

enum class LessonRequestStatus(val value: String) {
    Active("active"),
    Expired("expired"),
    ClosedByUser("closed_by_user"),
    PendingEmailVerification("pending_email_verification"),
    ExpiredUnverified("expired_unverified");

    companion object {
        fun fromString(value: String): LessonRequestStatus =
            when (value) {
                "expired" -> Expired
                "closed_by_user" -> ClosedByUser
                "pending_email_verification" -> PendingEmailVerification
                "expired_unverified" -> ExpiredUnverified
                else -> Active
            }
    }
}

data class LessonRequest(
    val id: String,
    val userId: String,
    val discipline: Discipline,
    val skillLevel: Int,
    val groupSize: Int,
    val hasChildren: Boolean,
    val dateStart: String?,
    val dateEnd: String?,
    val datesFlexible: Boolean,
    val durationDays: Double,
    val equipmentRental: EquipmentRental,
    val needsTransport: Boolean,
    val transportNote: String?,
    val languages: List<String>,
    val additionalNotes: String?,
    val allRegionsSelected: Boolean,
    val resortIds: List<String>,
    val status: LessonRequestStatus,
    val quotaLimit: Int,
    val createdAt: String,
    val expiresAt: String?,
    val resortNames: List<String> = emptyList(),
    val certPreferences: List<String> = emptyList(),
)

package com.teachmeski.app.domain.model

data class ChatRoomDetail(
    val roomId: String,
    val lessonRequestId: String,
    val instructorId: String,
    val userId: String,
    val status: String,
    val otherParty: OtherParty,
    val isBlocked: Boolean,
    val hasSentMessage: Boolean,
    val needsUnlock: Boolean,
    val unlockInfo: UnlockInfo?,
    val infoPanelData: InfoPanelData,
    val firstCategoryLabel: String?,
    val role: ChatRole,
)

data class OtherParty(
    val userId: String,
    val name: String,
    val avatarUrl: String?,
)

data class UnlockInfo(
    val cost: Int,
    val balance: Int,
    val lessonRequestId: String,
)

data class LessonRequestDisplay(
    val discipline: String,
    val skillLevel: Int?,
    val groupSize: Int,
    val hasChildren: Boolean,
    val durationDays: Double?,
    val dateStart: String?,
    val dateEnd: String?,
    val datesFlexible: Boolean,
    val languages: List<String>,
    val allRegionsSelected: Boolean,
    val resortNames: List<String>,
    val equipmentRental: String?,
    val needsTransport: Boolean,
    val transportNote: String?,
    val certPrefs: List<String>,
    val additionalNotes: String?,
)

sealed class InfoPanelData {
    data class StudentPanel(
        val instructorShortId: String,
        val instructorName: String,
        val instructorAvatarUrl: String?,
        val instructorRatingAvg: Double?,
        val instructorRatingCount: Int,
        val instructorBio: String?,
        val instructorLineUserId: String?,
        val instructorId: String,
        val instructorUserId: String,
        val isReviewed: Boolean,
        val lessonRequest: LessonRequestDisplay,
    ) : InfoPanelData()

    data class InstructorPanel(
        val seekerName: String,
        val seekerAvatarUrl: String?,
        val seekerUserId: String,
        val lessonRequest: LessonRequestDisplay,
    ) : InfoPanelData()
}

enum class ChatRole { Seeker, Instructor }

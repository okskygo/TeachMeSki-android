package com.teachmeski.app.data.repository

import com.teachmeski.app.R
import com.teachmeski.app.data.model.toExploreLessonRequest
import com.teachmeski.app.data.remote.ExploreDataSource
import com.teachmeski.app.domain.model.ExploreLessonRequest
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.ExploreRepository
import com.teachmeski.app.util.PricingCalculator
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExploreRepositoryImpl @Inject constructor(
    private val exploreDataSource: ExploreDataSource,
    private val authRepository: AuthRepository,
) : ExploreRepository {

    override suspend fun getExploreLessonRequests(
        page: Int,
        disciplineFilter: List<String>?,
        resortFilter: List<String>?,
    ): Resource<Pair<List<ExploreLessonRequest>, Int>> {
        return try {
            val userId = authRepository.currentUserId()
                ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
            val instructorProfileId = exploreDataSource.getInstructorProfileId(userId)

            val (rawRequests, totalCount) = exploreDataSource.getExploreLessonRequests(
                currentUserId = userId, page = page,
                disciplineFilter = disciplineFilter, resortFilter = resortFilter,
            )
            if (rawRequests.isEmpty()) {
                return Resource.Success(Pair(emptyList(), totalCount))
            }

            val requestIds = rawRequests.map { it.id }
            val creatorIds = rawRequests.map { it.userId }.distinct()
            val allResortIds = rawRequests.flatMap { it.resortIds }.distinct()

            val unlockRows = exploreDataSource.getUnlockRows(requestIds)
            val chatRoomRows = if (instructorProfileId != null) {
                exploreDataSource.getChatRoomsForInstructor(instructorProfileId, requestIds)
            } else emptyList()
            val userRows = exploreDataSource.getUserRows(creatorIds)
            val resortNameRows = exploreDataSource.getResortNames(allResortIds)

            val unlockCountByRequest = mutableMapOf<String, Int>()
            val unlockedRequestIds = mutableSetOf<String>()
            for (row in unlockRows) {
                unlockCountByRequest[row.lessonRequestId] = (unlockCountByRequest[row.lessonRequestId] ?: 0) + 1
                if (instructorProfileId != null && row.instructorId == instructorProfileId) {
                    unlockedRequestIds.add(row.lessonRequestId)
                }
            }

            val userMap = userRows.associateBy { it.id }
            val resortNameMap = resortNameRows.associateBy { it.id }
            val chatRoomMap = chatRoomRows.associateBy { it.lessonRequestId }

            val result = rawRequests.map { raw ->
                val u = userMap[raw.userId]
                val resortNames = raw.resortIds.mapNotNull { rid ->
                    resortNameMap[rid]?.let { "${it.nameZh} (${it.nameEn})" }
                }
                val baseTokenCost = PricingCalculator.calculateUnlockCost(raw.durationDays, raw.groupSize)
                raw.toExploreLessonRequest(
                    unlockCount = unlockCountByRequest[raw.id] ?: 0,
                    isUnlockedByMe = raw.id in unlockedRequestIds,
                    myChatRoomId = chatRoomMap[raw.id]?.id,
                    userDisplayName = u?.displayName ?: "",
                    userAvatarUrl = u?.avatarUrl,
                    resortNames = resortNames,
                    baseTokenCost = baseTokenCost,
                )
            }

            Resource.Success(Pair(result, totalCount))
        } catch (e: Exception) {
            Resource.Error(UiText.StringResource(R.string.error_load_explore))
        }
    }

    override suspend fun unlockLessonRequest(
        lessonRequestId: String,
        message: String,
    ): Resource<String> {
        return try {
            val trimmed = message.trim()
            if (trimmed.isEmpty()) return Resource.Error(UiText.StringResource(R.string.explore_error_message_required))
            if (trimmed.length > 500) return Resource.Error(UiText.StringResource(R.string.explore_error_message_too_long))

            val userId = authRepository.currentUserId()
                ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
            val instructorProfileId = exploreDataSource.getInstructorProfileId(userId)
                ?: return Resource.Error(UiText.StringResource(R.string.error_no_instructor_profile))

            val result = exploreDataSource.executeUnlock(
                authUserId = userId, instructorProfileId = instructorProfileId,
                lessonRequestId = lessonRequestId, instructorMessage = trimmed,
            )

            val error = result["error"] as? String
            if (error != null) {
                val errorResId = when (error) {
                    "insufficient_balance" -> R.string.explore_error_insufficient_balance
                    "already_unlocked" -> R.string.explore_error_already_unlocked
                    "quota_full" -> R.string.explore_error_quota_full
                    "lesson_request_not_active" -> R.string.explore_error_request_not_active
                    else -> R.string.error_generic
                }
                return Resource.Error(UiText.StringResource(errorResId))
            }

            val chatRoomId = result["chat_room_id"] as? String
                ?: return Resource.Error(UiText.StringResource(R.string.error_generic))
            Resource.Success(chatRoomId)
        } catch (e: Exception) {
            Resource.Error(UiText.StringResource(R.string.error_generic))
        }
    }
}

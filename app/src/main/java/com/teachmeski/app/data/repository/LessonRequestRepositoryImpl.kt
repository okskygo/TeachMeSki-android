package com.teachmeski.app.data.repository

import com.teachmeski.app.R
import com.teachmeski.app.data.model.toDomain
import com.teachmeski.app.data.model.toInstructorPreview
import com.teachmeski.app.data.remote.LessonRequestDataSource
import com.teachmeski.app.domain.model.Discipline
import com.teachmeski.app.domain.model.InstructorPreview
import com.teachmeski.app.domain.model.LessonRequest
import com.teachmeski.app.domain.model.LessonRequestListItem
import com.teachmeski.app.domain.model.LessonRequestStatus
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.LessonRequestRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRequestRepositoryImpl @Inject constructor(
    private val lessonRequestDataSource: LessonRequestDataSource,
    private val authRepository: AuthRepository,
) : LessonRequestRepository {

    override suspend fun submitLessonRequest(
        discipline: String,
        skillLevel: Int,
        groupSize: Int,
        hasChildren: Boolean,
        dateStart: String?,
        dateEnd: String?,
        datesFlexible: Boolean,
        durationDays: Double,
        equipmentRental: String,
        needsTransport: Boolean,
        transportNote: String,
        languages: List<String>,
        additionalNotes: String,
        allRegionsSelected: Boolean,
        resortIds: List<String>,
        certPreferences: List<String>,
    ): Resource<String> = try {
        val userId = authRepository.currentUserId()
            ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
        val id = lessonRequestDataSource.submitLessonRequest(
            userId = userId,
            discipline = discipline,
            skillLevel = skillLevel,
            groupSize = groupSize,
            hasChildren = hasChildren,
            dateStart = dateStart,
            dateEnd = dateEnd,
            datesFlexible = datesFlexible,
            durationDays = durationDays,
            equipmentRental = equipmentRental,
            needsTransport = needsTransport,
            transportNote = transportNote,
            languages = languages,
            additionalNotes = additionalNotes,
            allRegionsSelected = allRegionsSelected,
            resortIds = resortIds,
            certPreferences = certPreferences,
        )
        Resource.Success(id)
    } catch (e: Exception) {
        Resource.Error(UiText.StringResource(R.string.error_submit_request))
    }

    override suspend fun getMyLessonRequests(): Resource<List<LessonRequestListItem>> = try {
        val userId = authRepository.currentUserId()
            ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
        val dtos = lessonRequestDataSource.getMyLessonRequests(userId)
        val items = dtos.map { dto ->
            LessonRequestListItem(
                id = dto.id,
                discipline = Discipline.fromString(dto.discipline),
                skillLevel = dto.skillLevel,
                groupSize = dto.groupSize,
                dateStart = dto.dateStart,
                dateEnd = dto.dateEnd,
                datesFlexible = dto.datesFlexible,
                durationDays = dto.durationDays,
                status = LessonRequestStatus.fromString(dto.status),
                createdAt = dto.createdAt,
                chatCount = dto.chatRooms.size,
                instructorPreviews = dto.chatRooms.map { it.toInstructorPreview(userId) },
            )
        }
        Resource.Success(items)
    } catch (e: Exception) {
        Resource.Error(UiText.StringResource(R.string.error_load_requests))
    }

    override suspend fun getLessonRequestDetail(id: String): Resource<LessonRequest> = try {
        val userId = authRepository.currentUserId()
            ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
        val dto = lessonRequestDataSource.getLessonRequestDetail(id, userId)
        val resortNames = if (dto.resortIds.isNotEmpty()) {
            val resorts = lessonRequestDataSource.getResortsByIds(dto.resortIds)
            dto.resortIds.mapNotNull { resortId ->
                resorts.find { it.id == resortId }?.let { "${it.nameZh} (${it.nameEn})" }
            }
        } else {
            emptyList()
        }
        val certPrefs = lessonRequestDataSource.getCertPreferences(id)
        Resource.Success(dto.toDomain(resortNames = resortNames, certPreferences = certPrefs))
    } catch (e: Exception) {
        Resource.Error(UiText.StringResource(R.string.error_load_request_detail))
    }

    override suspend fun closeLessonRequest(id: String): Resource<Unit> = try {
        val userId = authRepository.currentUserId()
            ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
        lessonRequestDataSource.closeLessonRequest(id, userId)
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.StringResource(R.string.error_close_request))
    }

    override suspend fun getUnlockedInstructors(lessonRequestId: String): Resource<List<InstructorPreview>> = try {
        val userId = authRepository.currentUserId()
            ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
        val chatRooms = lessonRequestDataSource.getChatRoomsForRequest(lessonRequestId, userId)
        val previews = chatRooms.map { room ->
            room.toInstructorPreview(userId).copy(isReviewed = false)
        }
        Resource.Success(previews)
    } catch (e: Exception) {
        Resource.Error(UiText.StringResource(R.string.error_load_instructors))
    }

    override suspend fun getRecommendedInstructors(lessonRequestId: String): Resource<List<InstructorPreview>> = try {
        val userId = authRepository.currentUserId()
            ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
        val detail = lessonRequestDataSource.getLessonRequestDetail(lessonRequestId, userId)
        val chatRooms = lessonRequestDataSource.getChatRoomsForRequest(lessonRequestId, userId)
        val unlockedIds = lessonRequestDataSource.getUnlockedInstructorIds(lessonRequestId)
        val excludeIds = (chatRooms.map { it.instructorId } + unlockedIds).distinct()
        val instructors = lessonRequestDataSource.getRecommendedInstructors(
            discipline = detail.discipline,
            resortIds = detail.resortIds,
            excludeInstructorIds = excludeIds,
        )
        val previews = instructors.map { dto ->
            InstructorPreview(
                instructorId = dto.id,
                userId = dto.userId,
                displayName = dto.displayName,
                avatarUrl = dto.avatarUrl,
                hasUnread = false,
                roomId = null,
                ratingAvg = dto.ratingAvg,
                ratingCount = dto.ratingCount,
                phoneVerifiedAt = dto.phoneVerifiedAt,
                shortId = dto.shortId,
            )
        }
        Resource.Success(previews)
    } catch (e: Exception) {
        Resource.Error(UiText.StringResource(R.string.error_load_instructors))
    }
}

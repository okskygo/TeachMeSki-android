package com.teachmeski.app.data.repository

import com.teachmeski.app.R
import com.teachmeski.app.data.model.toDomain
import com.teachmeski.app.data.remote.ChatDataSource
import com.teachmeski.app.domain.model.ChatMessage
import com.teachmeski.app.domain.model.ChatRole
import com.teachmeski.app.domain.model.ChatRoom
import com.teachmeski.app.domain.model.ChatRoomDetail
import com.teachmeski.app.domain.model.InfoPanelData
import com.teachmeski.app.domain.model.LessonRequestDisplay
import com.teachmeski.app.domain.model.OtherParty
import com.teachmeski.app.domain.model.UnlockInfo
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.ChatRepository
import com.teachmeski.app.util.PricingCalculator
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import java.time.Instant
import java.time.OffsetDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val chatDataSource: ChatDataSource,
    private val authRepository: AuthRepository,
) : ChatRepository {

    override suspend fun getChatRooms(offset: Int): Resource<Pair<List<ChatRoom>, Boolean>> {
        return try {
            val userId = authRepository.currentUserId()
                ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
            val instructorProfileId = chatDataSource.getInstructorProfileId(userId)
            val isInstructor = instructorProfileId != null

            val roomDtos = if (instructorProfileId != null) {
                chatDataSource.getChatRoomsForInstructor(instructorProfileId, offset)
            } else {
                chatDataSource.getChatRoomsForSeeker(userId, offset)
            }

            val hasMore = roomDtos.size >= 20

            if (roomDtos.isEmpty()) return Resource.Success(Pair(emptyList(), false))

            val otherPartyIds = if (isInstructor) {
                roomDtos.map { it.userId }.distinct()
            } else {
                roomDtos.map { it.instructorId }.distinct()
            }

            val otherPartyInfo = if (isInstructor) {
                chatDataSource.getUserDisplayInfo(otherPartyIds)
            } else {
                chatDataSource.getInstructorDisplayInfo(otherPartyIds)
            }

            val requestIds = roomDtos.map { it.lessonRequestId }.distinct()
            val disciplines = chatDataSource.getLessonRequestDisciplines(requestIds)

            val chatRooms = roomDtos.map { dto ->
                val otherPartyId = if (isInstructor) dto.userId else dto.instructorId
                val info = otherPartyInfo[otherPartyId]
                val hasUnread = computeUnread(
                    lastMessageAt = dto.lastMessageAt,
                    lastMessageSenderId = dto.lastMessageSenderId,
                    currentUserId = userId,
                    lastReadAt = if (isInstructor) dto.instructorLastReadAt else dto.lastReadAt,
                )
                ChatRoom(
                    id = dto.id,
                    lessonRequestId = dto.lessonRequestId,
                    instructorId = dto.instructorId,
                    userId = dto.userId,
                    otherPartyName = info?.first ?: "",
                    otherPartyAvatarUrl = info?.second,
                    lastMessage = dto.lastMessageContent,
                    lastMessageAt = dto.lastMessageAt,
                    lastMessageSenderId = dto.lastMessageSenderId,
                    unreadCount = if (hasUnread) 1 else 0,
                    discipline = disciplines[dto.lessonRequestId],
                )
            }

            Resource.Success(Pair(chatRooms, hasMore))
        } catch (e: Exception) {
            Resource.Error(UiText.StringResource(R.string.error_load_chat_rooms))
        }
    }

    override suspend fun getChatRoomDetail(roomId: String): Resource<ChatRoomDetail> {
        return try {
            val userId = authRepository.currentUserId()
                ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))

            val room = chatDataSource.getChatRoom(roomId)
            val instructorProfileId = chatDataSource.getInstructorProfileId(userId)
            val isInstructor = instructorProfileId != null && instructorProfileId == room.instructorId
            val role = if (isInstructor) ChatRole.Instructor else ChatRole.Seeker

            val otherUserId = if (isInstructor) room.userId else {
                chatDataSource.getInstructorUserIds(listOf(room.instructorId))[room.instructorId] ?: ""
            }

            val isBlocked = if (otherUserId.isNotBlank()) {
                chatDataSource.checkIsBlocked(userId, otherUserId)
            } else false

            val lr = chatDataSource.getLessonRequestDetail(room.lessonRequestId)
            val certPrefs = chatDataSource.getCertPrefs(room.lessonRequestId)
            val resortNames = chatDataSource.getResortNames(lr.resortIds).map { "${it.nameZh} (${it.nameEn})" }

            val lessonRequestDisplay = LessonRequestDisplay(
                discipline = lr.discipline,
                skillLevel = lr.skillLevel,
                groupSize = lr.groupSize,
                hasChildren = lr.hasChildren,
                durationDays = lr.durationDays,
                dateStart = lr.dateStart,
                dateEnd = lr.dateEnd,
                datesFlexible = lr.datesFlexible,
                languages = lr.languages,
                allRegionsSelected = lr.allRegionsSelected,
                resortNames = resortNames,
                equipmentRental = lr.equipmentRental,
                needsTransport = lr.needsTransport,
                transportNote = lr.transportNote,
                certPrefs = certPrefs.map { it.certificationCode },
                additionalNotes = lr.additionalNotes,
            )

            val infoPanelData: InfoPanelData
            val otherParty: OtherParty
            var needsUnlock = false
            var unlockInfo: UnlockInfo? = null
            val hasSentMessage: Boolean
            val firstCategoryLabel: String? = lr.discipline

            if (role == ChatRole.Seeker) {
                val instructor = chatDataSource.getInstructorProfileDetail(room.instructorId)
                val isReviewed = chatDataSource.checkIsReviewed(userId, room.instructorId)
                hasSentMessage = chatDataSource.checkHasSentMessage(roomId, userId)
                otherParty = OtherParty(
                    userId = instructor.userId,
                    name = instructor.displayName ?: "",
                    avatarUrl = instructor.avatarUrl,
                )
                infoPanelData = InfoPanelData.StudentPanel(
                    instructorShortId = instructor.shortId ?: "",
                    instructorName = instructor.displayName ?: "",
                    instructorAvatarUrl = instructor.avatarUrl,
                    instructorRatingAvg = instructor.ratingAvg,
                    instructorRatingCount = instructor.ratingCount,
                    instructorBio = instructor.bio,
                    instructorPhoneVerifiedAt = instructor.phoneVerifiedAt,
                    instructorId = room.instructorId,
                    instructorUserId = instructor.userId,
                    isReviewed = isReviewed,
                    lessonRequest = lessonRequestDisplay,
                )
            } else {
                val student = chatDataSource.getUserProfile(room.userId)
                val hasUnlock = chatDataSource.checkHasUnlock(room.instructorId, room.lessonRequestId)
                hasSentMessage = hasUnlock
                needsUnlock = !hasUnlock

                if (needsUnlock && instructorProfileId != null) {
                    val cost = PricingCalculator.calculateUnlockCost(lr.durationDays, lr.groupSize)
                    val balance = chatDataSource.getWalletBalance(instructorProfileId)
                    unlockInfo = UnlockInfo(cost = cost, balance = balance, lessonRequestId = room.lessonRequestId)
                }

                otherParty = OtherParty(
                    userId = room.userId,
                    name = student.displayName ?: "",
                    avatarUrl = student.avatarUrl,
                )
                infoPanelData = InfoPanelData.InstructorPanel(
                    seekerName = student.displayName ?: "",
                    seekerAvatarUrl = student.avatarUrl,
                    seekerUserId = room.userId,
                    lessonRequest = lessonRequestDisplay,
                )
            }

            Resource.Success(
                ChatRoomDetail(
                    roomId = roomId,
                    lessonRequestId = room.lessonRequestId,
                    instructorId = room.instructorId,
                    userId = room.userId,
                    status = room.status,
                    otherParty = otherParty,
                    isBlocked = isBlocked,
                    hasSentMessage = hasSentMessage,
                    needsUnlock = needsUnlock,
                    unlockInfo = unlockInfo,
                    infoPanelData = infoPanelData,
                    firstCategoryLabel = firstCategoryLabel,
                    role = role,
                ),
            )
        } catch (e: Exception) {
            Resource.Error(UiText.StringResource(R.string.error_load_chat_detail))
        }
    }

    override suspend fun getMessages(roomId: String): Resource<Pair<List<ChatMessage>, Boolean>> {
        return try {
            val dtos = chatDataSource.getChatMessages(roomId)
            val messages = dtos.map { it.toDomain() }
            Resource.Success(Pair(messages, dtos.size >= 50))
        } catch (e: Exception) {
            Resource.Error(UiText.StringResource(R.string.error_load_messages))
        }
    }

    override suspend fun getOlderMessages(roomId: String, beforeSentAt: String): Resource<Pair<List<ChatMessage>, Boolean>> {
        return try {
            val dtos = chatDataSource.getOlderMessages(roomId, beforeSentAt)
            val messages = dtos.map { it.toDomain() }
            Resource.Success(Pair(messages, dtos.size >= 50))
        } catch (e: Exception) {
            Resource.Error(UiText.StringResource(R.string.error_load_messages))
        }
    }

    override suspend fun sendMessage(roomId: String, content: String): Resource<ChatMessage> {
        return try {
            val userId = authRepository.currentUserId()
                ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
            val sentAt = Instant.now().toString()
            val dto = chatDataSource.insertMessage(roomId, userId, content, sentAt)
            Resource.Success(dto.toDomain())
        } catch (e: Exception) {
            Resource.Error(UiText.StringResource(R.string.error_send_message))
        }
    }

    override suspend fun markRoomAsRead(roomId: String): Resource<Unit> {
        return try {
            val userId = authRepository.currentUserId()
                ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))

            val room = chatDataSource.getChatRoom(roomId)
            val instructorProfileId = chatDataSource.getInstructorProfileId(userId)
            when {
                instructorProfileId != null && instructorProfileId == room.instructorId ->
                    chatDataSource.markRoomAsReadForInstructor(roomId, instructorProfileId)
                else ->
                    chatDataSource.markRoomAsReadForSeeker(roomId, userId)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(UiText.StringResource(R.string.error_generic))
        }
    }

    override suspend fun getUnreadCount(): Resource<Int> {
        return try {
            val userId = authRepository.currentUserId()
                ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
            val instructorProfileId = chatDataSource.getInstructorProfileId(userId)

            val count = if (instructorProfileId != null) {
                chatDataSource.getUnreadCountForInstructor(instructorProfileId, userId)
            } else {
                chatDataSource.getUnreadCountForSeeker(userId)
            }
            Resource.Success(count)
        } catch (e: Exception) {
            Resource.Success(0)
        }
    }

    override suspend fun createPathBChatRoom(
        instructorProfileId: String,
        lessonRequestId: String,
        firstMessage: String,
    ): Resource<String> {
        return try {
            val userId = authRepository.currentUserId()
                ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
            val roomId = chatDataSource.createPathBChatRoom(instructorProfileId, lessonRequestId, userId, firstMessage)
            Resource.Success(roomId)
        } catch (e: Exception) {
            Resource.Error(UiText.StringResource(R.string.error_generic))
        }
    }

    override suspend fun unlockPathBConversation(roomId: String, message: String): Resource<String> {
        return try {
            val userId = authRepository.currentUserId()
                ?: return Resource.Error(UiText.StringResource(R.string.auth_error_not_authenticated))
            val instructorProfileId = chatDataSource.getInstructorProfileId(userId)
                ?: return Resource.Error(UiText.StringResource(R.string.error_no_instructor_profile))
            val room = chatDataSource.getChatRoom(roomId)

            val result = chatDataSource.executeUnlock(
                authUserId = userId,
                instructorProfileId = instructorProfileId,
                lessonRequestId = room.lessonRequestId,
                instructorMessage = message,
            )

            val error = result["error"] as? String
            if (error != null) {
                val resId = when (error) {
                    "insufficient_balance" -> R.string.explore_error_insufficient_balance
                    "already_unlocked" -> R.string.explore_error_already_unlocked
                    else -> R.string.error_generic
                }
                return Resource.Error(UiText.StringResource(resId))
            }
            Resource.Success(roomId)
        } catch (e: Exception) {
            Resource.Error(UiText.StringResource(R.string.error_generic))
        }
    }

    private fun computeUnread(
        lastMessageAt: String?,
        lastMessageSenderId: String?,
        currentUserId: String,
        lastReadAt: String?,
    ): Boolean {
        if (lastMessageAt.isNullOrBlank()) return false
        if (lastMessageSenderId.isNullOrBlank()) return false
        if (lastMessageSenderId == currentUserId) return false
        if (lastReadAt.isNullOrBlank()) return true
        return try {
            OffsetDateTime.parse(lastMessageAt).isAfter(OffsetDateTime.parse(lastReadAt))
        } catch (_: Exception) {
            lastMessageAt > lastReadAt
        }
    }
}

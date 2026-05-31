package com.teachmeski.app.ui.explore

import com.teachmeski.app.domain.model.Discipline
import com.teachmeski.app.domain.model.ExploreLessonRequest
import com.teachmeski.app.domain.model.LessonRequestStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ExploreCtaStateTest {

    private fun req(
        status: LessonRequestStatus = LessonRequestStatus.Active,
        quotaLimit: Int = 3,
        unlockCount: Int = 0,
        baseTokenCost: Int = 5,
        isUnlockedByMe: Boolean = false,
        myChatRoomId: String? = null,
    ) = ExploreLessonRequest(
        id = "r1",
        status = status,
        createdAt = "2026-05-01T00:00:00Z",
        discipline = Discipline.Ski,
        skillLevel = 2,
        groupSize = 1,
        hasChildren = false,
        durationDays = 1.0,
        startDate = null,
        endDate = null,
        datesFlexible = false,
        preferredLanguages = emptyList(),
        additionalNotes = null,
        equipmentRental = null,
        needsTransport = false,
        transportNote = null,
        certPreferences = emptyList(),
        quotaLimit = quotaLimit,
        unlockCount = unlockCount,
        baseTokenCost = baseTokenCost,
        userDisplayName = "Sam",
        userAvatarUrl = null,
        allRegionsSelected = false,
        resortNames = emptyList(),
        isUnlockedByMe = isUnlockedByMe,
        myChatRoomId = myChatRoomId,
    )

    @Test
    fun unlocked_with_room_is_ViewChat() {
        val state = exploreCtaState(req(isUnlockedByMe = true, myChatRoomId = "room-9"))
        assertEquals(ExploreCtaState.ViewChat("room-9"), state)
    }

    @Test
    fun unlocked_without_room_is_AlreadyUnlocked() {
        val state = exploreCtaState(req(isUnlockedByMe = true, myChatRoomId = null))
        assertEquals(ExploreCtaState.AlreadyUnlocked, state)
    }

    @Test
    fun active_with_remaining_is_Unlock_with_cost() {
        val state = exploreCtaState(req(quotaLimit = 3, unlockCount = 1, baseTokenCost = 7))
        assertEquals(ExploreCtaState.Unlock(7), state)
    }

    @Test
    fun active_no_remaining_is_SlotsFull() {
        val state = exploreCtaState(req(quotaLimit = 3, unlockCount = 3))
        assertEquals(ExploreCtaState.SlotsFull, state)
    }

    @Test
    fun `closed request is Unavailable`() {
        val state = exploreCtaState(
            req(status = LessonRequestStatus.ClosedByUser, quotaLimit = 3, unlockCount = 0),
        )
        assertEquals(ExploreCtaState.Unavailable, state)
    }

    @Test
    fun `expired request is Unavailable`() {
        val state = exploreCtaState(
            req(status = LessonRequestStatus.Expired, quotaLimit = 3, unlockCount = 0),
        )
        assertEquals(ExploreCtaState.Unavailable, state)
    }

    @Test
    fun `active but quota full is still SlotsFull`() {
        val state = exploreCtaState(req(quotaLimit = 3, unlockCount = 3))
        assertEquals(ExploreCtaState.SlotsFull, state)
    }

    @Test
    fun inactive_but_already_unlocked_with_room_is_ViewChat() {
        val state = exploreCtaState(
            req(status = LessonRequestStatus.ClosedByUser, isUnlockedByMe = true, myChatRoomId = "room-5"),
        )
        assertEquals(ExploreCtaState.ViewChat("room-5"), state)
    }
}

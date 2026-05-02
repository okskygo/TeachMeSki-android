package com.teachmeski.app.data.repository

import com.teachmeski.app.data.remote.ExploreDataSource
import com.teachmeski.app.domain.model.UnlockStatus
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * F-008 P3 — `ExploreRepositoryImpl.getUnlockedRooms` must surface
 * `request_unlocks.status` (active / refunded / completed) onto every
 * `UnlockedRoom.unlockStatus`. When a (lesson_request, instructor) pair has
 * multiple `request_unlocks` rows (refunded, then re-unlocked → active), the
 * NEWEST row's status wins because the data source returns rows ordered by
 * `unlocked_at DESC`.
 */
class ExploreRepositoryImplUnlockStatusTest {

    private fun roomDto(
        id: String,
        lessonRequestId: String,
        instructorId: String = "instr-1",
        userId: String = "student-$id",
    ) = ExploreDataSource.UnlockedRoomDto(
        id = id,
        lessonRequestId = lessonRequestId,
        instructorId = instructorId,
        userId = userId,
        createdAt = "2026-05-01T00:00:00Z",
        lastMessageContent = null,
        lastMessageAt = null,
        lastMessageSenderId = null,
        instructorLastReadAt = null,
    )

    private fun summary(id: String) = ExploreDataSource.UnlockedRequestSummaryDto(
        id = id,
        discipline = "ski",
        status = "active",
    )

    private fun unlockRow(
        lessonRequestId: String,
        status: String,
        unlockedAt: String,
    ) = ExploreDataSource.UnlockedRequestUnlockRow(
        lessonRequestId = lessonRequestId,
        status = status,
        unlockedAt = unlockedAt,
    )

    private fun buildMocks(): Triple<ExploreDataSource, AuthRepository, ExploreRepositoryImpl> {
        val ds = mockk<ExploreDataSource>()
        val auth = mockk<AuthRepository>()
        coEvery { auth.currentUserId() } returns "user-1"
        coEvery { ds.getInstructorProfileId("user-1") } returns "instr-1"
        coEvery { ds.getUserRows(any()) } returns emptyList()
        coEvery { ds.getResortNames(any()) } returns emptyList()
        return Triple(ds, auth, ExploreRepositoryImpl(ds, auth))
    }

    @Test
    fun `maps request_unlocks status onto UnlockedRoom unlockStatus`() = runTest {
        val (ds, _, repo) = buildMocks()
        coEvery { ds.getUnlockedRooms("instr-1") } returns listOf(
            roomDto(id = "room-A", lessonRequestId = "lr-A"),
            roomDto(id = "room-B", lessonRequestId = "lr-B"),
            roomDto(id = "room-C", lessonRequestId = "lr-C"),
        )
        coEvery { ds.getLessonRequestSummaries(any()) } returns listOf(
            summary("lr-A"),
            summary("lr-B"),
            summary("lr-C"),
        )
        coEvery { ds.getMyRequestUnlockRows("instr-1", any()) } returns listOf(
            unlockRow("lr-A", "active", "2026-05-01T00:00:00Z"),
            unlockRow("lr-B", "refunded", "2026-04-29T00:00:00Z"),
            unlockRow("lr-C", "completed", "2026-04-28T00:00:00Z"),
        )

        val result = repo.getUnlockedRooms()

        assertTrue(result is Resource.Success)
        val rooms = (result as Resource.Success).data.associateBy { it.roomId }
        assertEquals(UnlockStatus.Active, rooms.getValue("room-A").unlockStatus)
        assertEquals(UnlockStatus.Refunded, rooms.getValue("room-B").unlockStatus)
        assertEquals(UnlockStatus.Completed, rooms.getValue("room-C").unlockStatus)
    }

    @Test
    fun `picks newest request_unlocks row when an instructor re-unlocked a refunded room`() =
        runTest {
            val (ds, _, repo) = buildMocks()
            coEvery { ds.getUnlockedRooms("instr-1") } returns listOf(
                roomDto(id = "room-A", lessonRequestId = "lr-A"),
            )
            coEvery { ds.getLessonRequestSummaries(any()) } returns listOf(summary("lr-A"))
            // DESC order: newest (active) first, older (refunded) second.
            coEvery { ds.getMyRequestUnlockRows("instr-1", any()) } returns listOf(
                unlockRow("lr-A", "active", "2026-05-01T00:00:00Z"),
                unlockRow("lr-A", "refunded", "2026-04-28T00:00:00Z"),
            )

            val result = repo.getUnlockedRooms()

            assertTrue(result is Resource.Success)
            val rooms = (result as Resource.Success).data
            assertEquals(1, rooms.size)
            assertEquals(UnlockStatus.Active, rooms.first().unlockStatus)
        }

    @Test
    fun `defaults to Active when no request_unlocks row is found`() = runTest {
        val (ds, _, repo) = buildMocks()
        coEvery { ds.getUnlockedRooms("instr-1") } returns listOf(
            roomDto(id = "room-A", lessonRequestId = "lr-A"),
        )
        coEvery { ds.getLessonRequestSummaries(any()) } returns listOf(summary("lr-A"))
        coEvery { ds.getMyRequestUnlockRows("instr-1", any()) } returns emptyList()

        val result = repo.getUnlockedRooms()

        assertTrue(result is Resource.Success)
        assertEquals(
            UnlockStatus.Active,
            (result as Resource.Success).data.first().unlockStatus,
        )
    }
}

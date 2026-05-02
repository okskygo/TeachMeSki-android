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
 * `UnlockedRoom.unlockStatus`.
 *
 * After the F-008 v2.0 perf refactor, `getMyRequestUnlockRows` reads from
 * the Postgres view `v_latest_request_unlocks` (DISTINCT ON
 * (lesson_request_id, instructor_id) ORDER BY unlocked_at DESC), which
 * guarantees one row per pair. The repository therefore no longer dedupes
 * client-side and trusts the view to deliver the newest unlock per pair.
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
    fun `passes through view-supplied newest unlock for a re-unlocked room`() =
        runTest {
            // F-008 v2.0 perf: the data source now reads from
            // `v_latest_request_unlocks`, so the database has already
            // collapsed any (instructor, lesson_request) fanout down to
            // a single newest row before the repository sees it. We
            // simulate that by feeding the mocked data source exactly
            // one row per pair and assert the repo trusts it as-is.
            val (ds, _, repo) = buildMocks()
            coEvery { ds.getUnlockedRooms("instr-1") } returns listOf(
                roomDto(id = "room-A", lessonRequestId = "lr-A"),
            )
            coEvery { ds.getLessonRequestSummaries(any()) } returns listOf(summary("lr-A"))
            coEvery { ds.getMyRequestUnlockRows("instr-1", any()) } returns listOf(
                unlockRow("lr-A", "active", "2026-05-01T00:00:00Z"),
            )

            val result = repo.getUnlockedRooms()

            assertTrue(result is Resource.Success)
            val rooms = (result as Resource.Success).data
            assertEquals(1, rooms.size)
            assertEquals(UnlockStatus.Active, rooms.first().unlockStatus)
        }

    @Test
    fun `defaults to Pending when no request_unlocks row is found (My Cases rename)`() = runTest {
        // FR-008-053 (2026-05-02): a chat_room without any unlock row
        // means the student opened a Path-B conversation but the
        // instructor hasn't paid yet — surface as Pending so the UI
        // can render the "待解鎖 / Pending unlock" badge instead of
        // falsely claiming the room is unlocked.
        val (ds, _, repo) = buildMocks()
        coEvery { ds.getUnlockedRooms("instr-1") } returns listOf(
            roomDto(id = "room-A", lessonRequestId = "lr-A"),
        )
        coEvery { ds.getLessonRequestSummaries(any()) } returns listOf(summary("lr-A"))
        coEvery { ds.getMyRequestUnlockRows("instr-1", any()) } returns emptyList()

        val result = repo.getUnlockedRooms()

        assertTrue(result is Resource.Success)
        assertEquals(
            UnlockStatus.Pending,
            (result as Resource.Success).data.first().unlockStatus,
        )
    }
}

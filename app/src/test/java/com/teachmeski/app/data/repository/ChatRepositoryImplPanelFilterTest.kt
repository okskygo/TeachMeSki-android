package com.teachmeski.app.data.repository

import com.teachmeski.app.data.model.ChatRoomDto
import com.teachmeski.app.data.remote.ChatDataSource
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.ui.component.ActiveRole
import com.teachmeski.app.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * F-113 FR-113-001..004 / AC-113-001..AC-113-004:
 *
 * `ChatRepositoryImpl.getChatRooms` MUST filter by the caller's `activeRole`,
 * NOT by the "user has an instructor profile" heuristic. A `Both` user in
 * the student panel must see the seeker-side rooms even though the lookup
 * `getInstructorProfileId` would return non-null.
 */
class ChatRepositoryImplPanelFilterTest {

    private val sampleUserId = "user-1"
    private val sampleInstructorId = "instructor-profile-1"

    private fun roomDto(id: String) = ChatRoomDto(
        id = id,
        lessonRequestId = "req-$id",
        instructorId = sampleInstructorId,
        userId = sampleUserId,
    )

    @Test
    fun `getChatRooms with Student role calls seeker query and never resolves instructor profile`() = runTest {
        val dataSource = mockk<ChatDataSource>(relaxed = true)
        val authRepo = mockk<AuthRepository>()
        coEvery { authRepo.currentUserId() } returns sampleUserId
        coEvery { dataSource.getChatRoomsForSeeker(sampleUserId, 0) } returns listOf(roomDto("a"))
        coEvery { dataSource.getInstructorDisplayInfo(any()) } returns mapOf(
            sampleInstructorId to (null to null),
        )
        coEvery { dataSource.getLessonRequestDisciplines(any()) } returns emptyMap()

        val repo = ChatRepositoryImpl(dataSource, authRepo)
        val result = repo.getChatRooms(ActiveRole.Student, 0)

        assertTrue(result is Resource.Success)
        coVerify(exactly = 1) { dataSource.getChatRoomsForSeeker(sampleUserId, 0) }
        coVerify(exactly = 0) { dataSource.getChatRoomsForInstructor(any(), any()) }
        // FR-113-002 acceptance: student panel does NOT consult
        // instructor_profiles, even for a `Both` user.
        coVerify(exactly = 0) { dataSource.getInstructorProfileId(any()) }
    }

    @Test
    fun `getChatRooms with Instructor role resolves profile and calls instructor query`() = runTest {
        val dataSource = mockk<ChatDataSource>(relaxed = true)
        val authRepo = mockk<AuthRepository>()
        coEvery { authRepo.currentUserId() } returns sampleUserId
        coEvery { dataSource.getInstructorProfileId(sampleUserId) } returns sampleInstructorId
        coEvery { dataSource.getChatRoomsForInstructor(sampleInstructorId, 0) } returns listOf(roomDto("b"))
        coEvery { dataSource.getUserDisplayInfo(any()) } returns mapOf(
            sampleUserId to (null to null),
        )
        coEvery { dataSource.getLessonRequestDisciplines(any()) } returns emptyMap()

        val repo = ChatRepositoryImpl(dataSource, authRepo)
        val result = repo.getChatRooms(ActiveRole.Instructor, 0)

        assertTrue(result is Resource.Success)
        coVerify(exactly = 1) { dataSource.getChatRoomsForInstructor(sampleInstructorId, 0) }
        coVerify(exactly = 0) { dataSource.getChatRoomsForSeeker(any(), any()) }
    }

    @Test
    fun `getChatRooms with Instructor role and missing profile returns empty list`() = runTest {
        val dataSource = mockk<ChatDataSource>(relaxed = true)
        val authRepo = mockk<AuthRepository>()
        coEvery { authRepo.currentUserId() } returns sampleUserId
        // Defensive: should not happen for `Instructor` / `Both` users, but
        // we surface an empty list rather than crashing or falling back to
        // the seeker query (which would leak student rooms into the
        // instructor panel).
        coEvery { dataSource.getInstructorProfileId(sampleUserId) } returns null

        val repo = ChatRepositoryImpl(dataSource, authRepo)
        val result = repo.getChatRooms(ActiveRole.Instructor, 0)

        assertTrue(result is Resource.Success)
        val (rooms, hasMore) = (result as Resource.Success).data
        assertTrue(rooms.isEmpty())
        assertEquals(false, hasMore)
        coVerify(exactly = 0) { dataSource.getChatRoomsForInstructor(any(), any()) }
        coVerify(exactly = 0) { dataSource.getChatRoomsForSeeker(any(), any()) }
    }

    @Test
    fun `getUnreadCountForBothPanels returns sum-able pair`() = runTest {
        val dataSource = mockk<ChatDataSource>(relaxed = true)
        val authRepo = mockk<AuthRepository>()
        coEvery { authRepo.currentUserId() } returns sampleUserId
        coEvery { dataSource.getInstructorProfileId(sampleUserId) } returns sampleInstructorId
        coEvery { dataSource.getUnreadCountForInstructor(sampleInstructorId, sampleUserId) } returns 3
        coEvery { dataSource.getUnreadCountForSeeker(sampleUserId) } returns 2

        val repo = ChatRepositoryImpl(dataSource, authRepo)
        val result = repo.getUnreadCountForBothPanels()

        assertTrue(result is Resource.Success)
        assertEquals(3 to 2, (result as Resource.Success).data)
    }

    @Test
    fun `getUnreadCountForBothPanels returns zero instructor count when no profile`() = runTest {
        val dataSource = mockk<ChatDataSource>(relaxed = true)
        val authRepo = mockk<AuthRepository>()
        coEvery { authRepo.currentUserId() } returns sampleUserId
        coEvery { dataSource.getInstructorProfileId(sampleUserId) } returns null
        coEvery { dataSource.getUnreadCountForSeeker(sampleUserId) } returns 5

        val repo = ChatRepositoryImpl(dataSource, authRepo)
        val result = repo.getUnreadCountForBothPanels()

        assertTrue(result is Resource.Success)
        assertEquals(0 to 5, (result as Resource.Success).data)
    }
}

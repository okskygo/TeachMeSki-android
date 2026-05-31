package com.teachmeski.app.data.repository

import com.teachmeski.app.data.model.ExploreRawRequestDto
import com.teachmeski.app.data.remote.ExploreDataSource
import com.teachmeski.app.domain.model.LessonRequestStatus
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExploreRepositoryByIdTest {

    private fun raw(
        id: String = "lr-1",
        status: String = "closed_by_user",
        userId: String = "owner-1",
    ) = ExploreRawRequestDto(
        id = id,
        status = status,
        createdAt = "2026-05-01T00:00:00Z",
        discipline = "ski",
        skillLevel = 2,
        groupSize = 2,
        hasChildren = false,
        durationDays = 1.0,
        quotaLimit = 5,
        unlockCount = 5,
        resortIds = emptyList(),
        userId = userId,
    )

    private fun buildMocks(): Triple<ExploreDataSource, AuthRepository, ExploreRepositoryImpl> {
        val ds = mockk<ExploreDataSource>()
        val auth = mockk<AuthRepository>()
        coEvery { auth.currentUserId() } returns "user-1"
        coEvery { ds.getInstructorProfileId("user-1") } returns "instr-1"
        coEvery { ds.getMyUnlockRows(any(), any()) } returns emptyList()
        coEvery { ds.getChatRoomsForInstructor(any(), any()) } returns emptyList()
        coEvery { ds.getUserRows(any()) } returns emptyList()
        coEvery { ds.getResortNames(any()) } returns emptyList()
        coEvery { ds.getCertPrefs(any()) } returns emptyList()
        return Triple(ds, auth, ExploreRepositoryImpl(ds, auth))
    }

    @Test
    fun `maps a closed request to a full ExploreLessonRequest`() = runTest {
        val (ds, _, repo) = buildMocks()
        coEvery { ds.getExploreLessonRequestById("user-1", "lr-1") } returns raw()

        val result = repo.getLessonRequestById("lr-1")

        assertTrue(result is Resource.Success)
        val request = (result as Resource.Success).data
        assertEquals("lr-1", request.id)
        assertEquals(LessonRequestStatus.ClosedByUser, request.status)
        assertEquals(false, request.isUnlockedByMe)
    }

    @Test
    fun `absent row returns Error`() = runTest {
        val (ds, _, repo) = buildMocks()
        coEvery { ds.getExploreLessonRequestById("user-1", "missing") } returns null

        val result = repo.getLessonRequestById("missing")

        assertTrue(result is Resource.Error)
    }
}

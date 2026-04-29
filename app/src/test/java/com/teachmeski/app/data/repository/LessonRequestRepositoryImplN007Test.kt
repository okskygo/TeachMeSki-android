package com.teachmeski.app.data.repository

import com.teachmeski.app.data.remote.LessonRequestDataSource
import com.teachmeski.app.data.remote.PushNotificationDispatcher
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * F-109-N007 (FR-N007-005, AC-N007-001, AC-N007-007):
 *
 * `LessonRequestRepositoryImpl.expandQuota` must:
 *  1. Invoke `PushNotificationDispatcher.fireN007QuotaExpanded(requestId)` after
 *     a successful RPC.
 *  2. Skip the dispatcher when the RPC throws.
 *  3. Still return Success even when the dispatcher itself throws (best-effort).
 */
class LessonRequestRepositoryImplN007Test {

    @Test
    fun `expandQuota success fires N-007 dispatcher`() = runTest {
        val dataSource = mockk<LessonRequestDataSource>()
        val authRepo = mockk<AuthRepository>()
        val dispatcher = mockk<PushNotificationDispatcher>(relaxed = true)
        coEvery { dataSource.expandLessonRequestQuota("req-1") } returns 10

        val repo = LessonRequestRepositoryImpl(dataSource, authRepo, dispatcher)
        val result = repo.expandQuota("req-1")

        assertTrue(result is Resource.Success)
        assertEquals(10, (result as Resource.Success).data)
        coVerify(exactly = 1) { dispatcher.fireN007QuotaExpanded("req-1") }
    }

    @Test
    fun `expandQuota RPC failure does not fire dispatcher`() = runTest {
        val dataSource = mockk<LessonRequestDataSource>()
        val authRepo = mockk<AuthRepository>()
        val dispatcher = mockk<PushNotificationDispatcher>(relaxed = true)
        coEvery { dataSource.expandLessonRequestQuota("req-1") } throws IllegalStateException("quota_not_full")

        val repo = LessonRequestRepositoryImpl(dataSource, authRepo, dispatcher)
        val result = repo.expandQuota("req-1")

        assertTrue(result is Resource.Error)
        coVerify(exactly = 0) { dispatcher.fireN007QuotaExpanded(any()) }
    }

    @Test
    fun `expandQuota success returns success even when dispatcher throws`() = runTest {
        val dataSource = mockk<LessonRequestDataSource>()
        val authRepo = mockk<AuthRepository>()
        val dispatcher = mockk<PushNotificationDispatcher>()
        coEvery { dataSource.expandLessonRequestQuota("req-1") } returns 15
        coEvery { dispatcher.fireN007QuotaExpanded("req-1") } throws RuntimeException("network")

        val repo = LessonRequestRepositoryImpl(dataSource, authRepo, dispatcher)
        val result = repo.expandQuota("req-1")

        assertTrue(result is Resource.Success)
        assertEquals(15, (result as Resource.Success).data)
    }
}

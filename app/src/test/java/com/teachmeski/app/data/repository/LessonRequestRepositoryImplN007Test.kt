package com.teachmeski.app.data.repository

import com.teachmeski.app.data.remote.LessonRequestDataSource
import com.teachmeski.app.data.remote.PushNotificationDispatcher
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.util.Resource
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
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
 *
 * As of the F-109 latency fix, `fireN007QuotaExpanded` is a non-suspend
 * fire-and-forget (returns immediately, errors are swallowed inside the
 * launched coroutine). The repo therefore can no longer observe a
 * dispatcher throw — case (3) is verified at the unit level by
 * `PushNotificationDispatcher` itself; here we only check the repo
 * orchestration.
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
        verify(exactly = 1) { dispatcher.fireN007QuotaExpanded("req-1") }
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
        verify(exactly = 0) { dispatcher.fireN007QuotaExpanded(any()) }
    }

    // Note: the previous `dispatcher throws → still Success` test was
    // removed when `fireN007QuotaExpanded` became truly fire-and-forget.
    // The dispatcher swallows all transport errors inside its internal
    // `scope.launch`, so the repo can no longer observe a throw. The
    // best-effort guarantee (AC-N007-007) is now upheld structurally
    // by `PushNotificationDispatcher` itself rather than by the repo's
    // try/catch.
}

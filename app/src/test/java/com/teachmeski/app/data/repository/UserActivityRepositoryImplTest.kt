package com.teachmeski.app.data.repository

import com.teachmeski.app.domain.repository.UserActivityRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UserActivityRepositoryImplTest {

    private class FakeRpcCaller(
        private val shouldThrow: Boolean = false,
    ) : SupabaseRpcCalling {
        val invocations = mutableListOf<String>()
        override suspend fun rpc(name: String) {
            invocations.add(name)
            if (shouldThrow) throw RuntimeException("boom")
        }
    }

    @Test
    fun `touch invokes touch_last_active rpc`() = runTest {
        val spy = FakeRpcCaller()
        val repo: UserActivityRepository = UserActivityRepositoryImpl(spy)
        repo.touch()
        assertEquals(listOf("touch_last_active"), spy.invocations)
    }

    @Test
    fun `touch swallows errors`() = runTest {
        val spy = FakeRpcCaller(shouldThrow = true)
        val repo: UserActivityRepository = UserActivityRepositoryImpl(spy)
        repo.touch()  // must not throw
    }
}

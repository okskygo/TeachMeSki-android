package com.teachmeski.app.ui.explore

import com.teachmeski.app.domain.model.Discipline
import com.teachmeski.app.domain.model.ExploreLessonRequest
import com.teachmeski.app.domain.model.LessonRequestStatus
import com.teachmeski.app.domain.repository.ExploreRepository
import com.teachmeski.app.domain.repository.InstructorRepository
import com.teachmeski.app.domain.repository.WalletRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreViewModelEnsureRequestLoadedTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(testDispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun sample(id: String) = ExploreLessonRequest(
        id = id,
        status = LessonRequestStatus.Active,
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
        quotaLimit = 5,
        unlockCount = 0,
        baseTokenCost = 5,
        userDisplayName = "Sam",
        userAvatarUrl = null,
        allRegionsSelected = false,
        resortNames = emptyList(),
        isUnlockedByMe = false,
        myChatRoomId = null,
    )

    private fun makeVm(explore: ExploreRepository): ExploreViewModel {
        val wallet = mockk<WalletRepository>()
        coEvery { wallet.getWallet() } returns Resource.Error(UiText.DynamicString("ignored"))
        val instructor = mockk<InstructorRepository>(relaxed = true)
        return ExploreViewModel(explore, wallet, instructor)
    }

    @Test
    fun `does not fetch when id already in feed list`() = runTest(testDispatcher) {
        val explore = mockk<ExploreRepository>()
        coEvery { explore.getExploreLessonRequests(any(), any(), any()) } returns
            Resource.Success(listOf(sample("lr-1")) to 1)
        val vm = makeVm(explore)
        advanceUntilIdle()

        vm.ensureRequestLoaded("lr-1")
        advanceUntilIdle()

        coVerify(exactly = 0) { explore.getLessonRequestById(any()) }
        assertNull(vm.uiState.value.detailFallback)
    }

    @Test
    fun `fetches and stores fallback when id not in list`() = runTest(testDispatcher) {
        val explore = mockk<ExploreRepository>()
        coEvery { explore.getExploreLessonRequests(any(), any(), any()) } returns
            Resource.Success(emptyList<ExploreLessonRequest>() to 0)
        coEvery { explore.getLessonRequestById("lr-9") } returns
            Resource.Success(sample("lr-9"))
        val vm = makeVm(explore)
        advanceUntilIdle()

        vm.ensureRequestLoaded("lr-9")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("lr-9", state.detailFallback?.id)
        assertEquals(false, state.detailLoading)
        assertEquals(false, state.detailNotFound)
    }

    @Test
    fun `sets detailNotFound when fetch fails`() = runTest(testDispatcher) {
        val explore = mockk<ExploreRepository>()
        coEvery { explore.getExploreLessonRequests(any(), any(), any()) } returns
            Resource.Success(emptyList<ExploreLessonRequest>() to 0)
        coEvery { explore.getLessonRequestById("gone") } returns
            Resource.Error(UiText.DynamicString("not found"))
        val vm = makeVm(explore)
        advanceUntilIdle()

        vm.ensureRequestLoaded("gone")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertNull(state.detailFallback)
        assertEquals(false, state.detailLoading)
        assertTrue(state.detailNotFound)
    }
}

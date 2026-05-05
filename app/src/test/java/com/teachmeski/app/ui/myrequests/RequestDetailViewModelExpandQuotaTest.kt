package com.teachmeski.app.ui.myrequests

import androidx.lifecycle.SavedStateHandle
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.ChatMessage
import com.teachmeski.app.domain.model.ChatRoom
import com.teachmeski.app.domain.model.ChatRoomDetail
import com.teachmeski.app.domain.model.Discipline
import com.teachmeski.app.domain.model.EquipmentRental
import com.teachmeski.app.domain.model.InboxRoomUpdate
import com.teachmeski.app.domain.model.InstructorPreview
import com.teachmeski.app.domain.model.LessonRequest
import com.teachmeski.app.domain.model.LessonRequestStatus
import com.teachmeski.app.domain.repository.ChatRepository
import com.teachmeski.app.domain.repository.LessonRequestRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RequestDetailViewModelExpandQuotaTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun sampleDetail(
        id: String = "req-1",
        quotaLimit: Int = 5,
        unlockCount: Int = 5,
    ) = LessonRequest(
        id = id,
        userId = "user-1",
        discipline = Discipline.Ski,
        skillLevel = 2,
        groupSize = 1,
        hasChildren = false,
        dateStart = null,
        dateEnd = null,
        datesFlexible = true,
        durationDays = 1.0,
        equipmentRental = EquipmentRental.None,
        needsTransport = false,
        transportNote = null,
        languages = listOf("zh"),
        additionalNotes = null,
        allRegionsSelected = true,
        resortIds = emptyList(),
        status = LessonRequestStatus.Active,
        quotaLimit = quotaLimit,
        createdAt = "2026-04-29T00:00:00Z",
        expiresAt = null,
        resortNames = emptyList(),
        certPreferences = emptyList(),
        unlockCount = unlockCount,
    )

    private class FakeLessonRequestRepository(
        private val detail: LessonRequest,
        var expandResult: Resource<Int> = Resource.Success(10),
    ) : LessonRequestRepository {
        var expandCalledWith: String? = null

        override suspend fun submitLessonRequest(
            discipline: String,
            skillLevel: Int,
            groupSize: Int,
            hasChildren: Boolean,
            dateStart: String?,
            dateEnd: String?,
            datesFlexible: Boolean,
            durationDays: Double,
            equipmentRental: String,
            needsTransport: Boolean,
            transportNote: String,
            languages: List<String>,
            additionalNotes: String,
            allRegionsSelected: Boolean,
            resortIds: List<String>,
            certPreferences: List<String>,
        ): Resource<String> = Resource.Success("req-x")

        override suspend fun getMyLessonRequests() = Resource.Success(emptyList<com.teachmeski.app.domain.model.LessonRequestListItem>())

        override suspend fun getLessonRequestDetail(id: String) = Resource.Success(detail)

        override suspend fun closeLessonRequest(id: String) = Resource.Success(Unit)

        override suspend fun getUnlockedInstructors(lessonRequestId: String) =
            Resource.Success(emptyList<InstructorPreview>())

        override suspend fun getRecommendedInstructors(lessonRequestId: String) =
            Resource.Success(emptyList<InstructorPreview>())

        override suspend fun expandQuota(lessonRequestId: String): Resource<Int> {
            expandCalledWith = lessonRequestId
            return expandResult
        }
    }

    private class FakeChatRepository : ChatRepository {
        override suspend fun getChatRooms(activeRole: com.teachmeski.app.ui.component.ActiveRole, offset: Int) =
            Resource.Success(Pair(emptyList<ChatRoom>(), false))
        override suspend fun getChatRoomDetail(roomId: String): Resource<ChatRoomDetail> =
            Resource.Error(UiText.DynamicString("not used"))
        override suspend fun getMessages(roomId: String) =
            Resource.Success(Pair(emptyList<ChatMessage>(), false))
        override suspend fun getOlderMessages(roomId: String, beforeSentAt: String) =
            Resource.Success(Pair(emptyList<ChatMessage>(), false))
        override suspend fun sendMessage(roomId: String, content: String): Resource<ChatMessage> =
            Resource.Error(UiText.DynamicString("not used"))
        override suspend fun markRoomAsRead(roomId: String) = Resource.Success(Unit)
        override suspend fun getUnreadCount(activeRole: com.teachmeski.app.ui.component.ActiveRole) = Resource.Success(0)
        override suspend fun getUnreadCountForBothPanels() = Resource.Success(0 to 0)
        override suspend fun createPathBChatRoom(
            instructorProfileId: String,
            lessonRequestId: String,
            firstMessage: String,
        ) = Resource.Success("room-1")
        override suspend fun unlockPathBConversation(roomId: String) =
            Resource.Success("room-1")
        override fun subscribeToRoomFlow(roomId: String): Flow<ChatMessage> = emptyFlow()
        override fun subscribeToInboxFlow(): Flow<InboxRoomUpdate> = emptyFlow()
    }

    private fun makeVm(repo: FakeLessonRequestRepository): RequestDetailViewModel {
        val handle = SavedStateHandle(mapOf("id" to "req-1"))
        return RequestDetailViewModel(
            lessonRequestRepository = repo,
            chatRepository = FakeChatRepository(),
            savedStateHandle = handle,
        )
    }

    @Test
    fun `expandQuota success updates quotaLimit and emits success toast`() = runTest(testDispatcher) {
        val repo = FakeLessonRequestRepository(
            detail = sampleDetail(quotaLimit = 5, unlockCount = 5),
            expandResult = Resource.Success(10),
        )
        val vm = makeVm(repo)
        advanceUntilIdle() // let init load() complete

        vm.expandQuota()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals("req-1", repo.expandCalledWith)
        assertEquals(10, state.detail?.quotaLimit)
        assertFalse(state.isExpandingQuota)
        val toast = state.expandQuotaToast
        assertNotNull(toast)
        assertTrue(toast is UiText.StringResource)
        assertEquals(
            R.string.my_requests_find_more_success_toast,
            (toast as UiText.StringResource).resId,
        )
    }

    @Test
    fun `expandQuota failure emits error toast and keeps quotaLimit unchanged`() = runTest(testDispatcher) {
        val repo = FakeLessonRequestRepository(
            detail = sampleDetail(quotaLimit = 5, unlockCount = 5),
            expandResult = Resource.Error(UiText.DynamicString("quota_not_full")),
        )
        val vm = makeVm(repo)
        advanceUntilIdle()

        vm.expandQuota()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(5, state.detail?.quotaLimit)
        assertFalse(state.isExpandingQuota)
        val toast = state.expandQuotaToast
        assertNotNull(toast)
        assertTrue(toast is UiText.StringResource)
        assertEquals(
            R.string.my_requests_find_more_error_toast,
            (toast as UiText.StringResource).resId,
        )
    }

    @Test
    fun `consumeExpandQuotaToast clears toast`() = runTest(testDispatcher) {
        val repo = FakeLessonRequestRepository(
            detail = sampleDetail(),
            expandResult = Resource.Success(10),
        )
        val vm = makeVm(repo)
        advanceUntilIdle()
        vm.expandQuota()
        advanceUntilIdle()
        assertNotNull(vm.uiState.value.expandQuotaToast)

        vm.consumeExpandQuotaToast()

        assertEquals(null, vm.uiState.value.expandQuotaToast)
    }
}

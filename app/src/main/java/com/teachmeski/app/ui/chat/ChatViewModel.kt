package com.teachmeski.app.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.domain.model.ChatMessage
import com.teachmeski.app.domain.model.ChatRoomDetail
import com.teachmeski.app.R
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.BlockRepository
import com.teachmeski.app.domain.repository.ChatRepository
import com.teachmeski.app.domain.repository.InstructorRepository
import com.teachmeski.app.domain.repository.ReportRepository
import com.teachmeski.app.domain.repository.ReviewRepository
import com.teachmeski.app.notifications.ActiveRoomTracker
import com.teachmeski.app.notifications.NotificationDisplay
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

data class ChatUiState(
    val roomDetail: ChatRoomDetail? = null,
    val messages: List<ChatMessage> = emptyList(),
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val isLoadingOlder: Boolean = false,
    val hasMoreOlder: Boolean = true,
    val isSending: Boolean = false,
    val infoPanelExpanded: Boolean = false,
    val error: UiText? = null,
    val isBlockedByMe: Boolean = false,
    val isBlockedByOther: Boolean = false,
    val showReviewDialog: Boolean = false,
    val showReportDialog: Boolean = false,
    val isSubmittingReview: Boolean = false,
    val isSubmittingReport: Boolean = false,
    val isBlockInFlight: Boolean = false,
    val toast: UiText? = null,
    /** Path-B confirm dialog visibility. Opened by tapping `ChatUnlockBar`'s
     *  unlock button after the F-108 identity pre-check passes. */
    val showUnlockConfirm: Boolean = false,
    val isUnlocking: Boolean = false,
    /** F-108: Path-B identity gate. */
    val showIdentityRequired: Boolean = false,
    /// After a successful older-message page prepends, this holds the
    /// id of the message that was the first visible row immediately
    /// before the prepend. The view anchors the scroll position to
    /// this id (with offset 0, anchor top) so the user keeps reading
    /// from the same row instead of being yanked to either end of the
    /// list. The view consumes the value via `consumePrependAnchor`.
    val prependAnchorMessageId: String? = null,
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val blockRepository: BlockRepository,
    private val reportRepository: ReportRepository,
    private val reviewRepository: ReviewRepository,
    private val instructorRepository: InstructorRepository,
    private val activeRoomTracker: ActiveRoomTracker,
    @ApplicationContext private val appContext: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val roomId: String = savedStateHandle.get<String>("roomId") ?: ""

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var lastMarkReadAt = 0L
    private var realtimeJob: Job? = null

    init {
        if (roomId.isNotBlank()) {
            activeRoomTracker.setActiveRoom(roomId)
            NotificationDisplay.clearRoomNotifications(appContext, roomId)
        }
        load(showFullScreenLoader = true)
    }

    private fun startRealtimeSubscription() {
        if (roomId.isBlank()) return
        if (realtimeJob?.isActive == true) return
        realtimeJob = viewModelScope.launch {
            chatRepository.subscribeToRoomFlow(roomId).collect { message ->
                onRealtimeMessage(message)
            }
        }
    }

    private fun stopRealtimeSubscription() {
        realtimeJob?.cancel()
        realtimeJob = null
    }

    private fun onRealtimeMessage(message: ChatMessage) {
        if (message.senderId == _uiState.value.currentUserId) return
        _uiState.update { s ->
            if (s.messages.any { it.id == message.id }) return@update s
            s.copy(messages = s.messages + message)
        }
        markRead()
    }

    private fun load(showFullScreenLoader: Boolean) {
        if (roomId.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = UiText.StringResource(R.string.error_load_chat_detail),
                )
            }
            return
        }
        viewModelScope.launch {
            if (showFullScreenLoader) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            } else {
                _uiState.update { it.copy(error = null) }
            }
            val userId = authRepository.currentUserId() ?: ""
            coroutineScope {
                val detailDef = async { chatRepository.getChatRoomDetail(roomId) }
                val msgsDef = async { chatRepository.getMessages(roomId) }
                val detailRes = detailDef.await()
                val msgsRes = msgsDef.await()

                when {
                    detailRes is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = detailRes.message,
                            )
                        }
                    }
                    msgsRes is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = msgsRes.message,
                            )
                        }
                    }
                    detailRes is Resource.Success && msgsRes is Resource.Success -> {
                        val detail = detailRes.data
                        val (msgs, hasMore) = msgsRes.data
                        // F-110: best-effort reverse-block check via
                        // SECURITY DEFINER RPC. Failure leaves the flag
                        // false (input stays enabled and the DB trigger
                        // remains the authoritative gate).
                        val otherUserId = detail.otherParty.userId
                        val blockedByOther = if (otherUserId.isNotBlank()) {
                            blockRepository.amIBlockedBy(otherUserId)
                        } else {
                            false
                        }
                        val blockedByMe = if (otherUserId.isNotBlank()) {
                            blockRepository.haveIBlocked(otherUserId)
                        } else {
                            false
                        }
                        _uiState.update {
                            it.copy(
                                roomDetail = detail,
                                messages = msgs,
                                currentUserId = userId,
                                hasMoreOlder = hasMore,
                                isLoading = false,
                                error = null,
                                isBlockedByMe = blockedByMe,
                                isBlockedByOther = blockedByOther,
                            )
                        }
                        startRealtimeSubscription()
                        markRead()
                    }
                    else -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }
        }
    }

    fun sendMessage(body: String) {
        val trimmed = body.trim()
        if (trimmed.isEmpty() || trimmed.length > 2000) return
        val tempId = "opt-" + UUID.randomUUID().toString()
        val optimistic = ChatMessage(
            id = tempId,
            roomId = roomId,
            senderId = _uiState.value.currentUserId,
            content = trimmed,
            sentAt = Instant.now().toString(),
            isOptimistic = true,
            isFailed = false,
        )
        _uiState.update { it.copy(messages = it.messages + optimistic, isSending = true) }
        viewModelScope.launch {
            when (val res = chatRepository.sendMessage(roomId, trimmed)) {
                is Resource.Success -> {
                    val realMessage = res.data
                    _uiState.update { s ->
                        s.copy(
                            messages = s.messages.map { if (it.id == tempId) realMessage else it },
                            isSending = false,
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { s ->
                        s.copy(
                            messages = s.messages.map {
                                if (it.id == tempId) {
                                    it.copy(isFailed = true, isOptimistic = false)
                                } else {
                                    it
                                }
                            },
                            isSending = false,
                            error = res.message,
                        )
                    }
                }
                Resource.Loading -> {
                    _uiState.update { it.copy(isSending = false) }
                }
            }
        }
    }

    fun loadOlderMessages() {
        val s = _uiState.value
        if (!s.hasMoreOlder || s.isLoadingOlder || s.messages.isEmpty()) return
        viewModelScope.launch {
            val anchorId = s.messages.first().id
            _uiState.update { it.copy(isLoadingOlder = true) }
            val beforeSentAt = s.messages.first().sentAt
            when (val res = chatRepository.getOlderMessages(roomId, beforeSentAt)) {
                is Resource.Success -> {
                    val (older, more) = res.data
                    _uiState.update { prev ->
                        val existingIds = prev.messages.map { it.id }.toSet()
                        val newOnes = older.filter { it.id !in existingIds }
                        prev.copy(
                            messages = newOnes + prev.messages,
                            hasMoreOlder = more,
                            isLoadingOlder = false,
                            prependAnchorMessageId = if (newOnes.isNotEmpty()) anchorId else null,
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoadingOlder = false,
                            error = res.message,
                        )
                    }
                }
                Resource.Loading -> {
                    _uiState.update { it.copy(isLoadingOlder = false) }
                }
            }
        }
    }

    fun toggleInfoPanel() {
        _uiState.update { it.copy(infoPanelExpanded = !it.infoPanelExpanded) }
    }

    fun markRead() {
        val now = System.currentTimeMillis()
        if (now - lastMarkReadAt < 2000L) return
        lastMarkReadAt = now
        viewModelScope.launch {
            chatRepository.markRoomAsRead(roomId)
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }

    fun consumePrependAnchor() {
        _uiState.update { it.copy(prependAnchorMessageId = null) }
    }

    fun consumeToast() {
        _uiState.update { it.copy(toast = null) }
    }

    fun showReviewDialog() {
        if (_uiState.value.roomDetail?.hasSentMessage != true) return
        _uiState.update { it.copy(showReviewDialog = true) }
    }

    fun dismissReviewDialog() {
        _uiState.update { it.copy(showReviewDialog = false) }
    }

    fun showReportDialog() {
        _uiState.update { it.copy(showReportDialog = true) }
    }

    fun dismissReportDialog() {
        _uiState.update { it.copy(showReportDialog = false) }
    }

    fun submitReview(rating: Int, comment: String?) {
        val detail = _uiState.value.roomDetail ?: return
        val panel = detail.infoPanelData as? com.teachmeski.app.domain.model.InfoPanelData.StudentPanel
            ?: return
        _uiState.update { it.copy(isSubmittingReview = true) }
        viewModelScope.launch {
            when (val res = reviewRepository.submitReview(panel.instructorId, rating, comment)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingReview = false,
                            showReviewDialog = false,
                            toast = UiText.StringResource(R.string.review_success),
                        )
                    }
                    refresh()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingReview = false,
                            error = res.message,
                        )
                    }
                }
                Resource.Loading -> {
                    _uiState.update { it.copy(isSubmittingReview = false) }
                }
            }
        }
    }

    fun submitReport(reason: String) {
        val detail = _uiState.value.roomDetail ?: return
        val otherUserId = detail.otherParty.userId
        _uiState.update { it.copy(isSubmittingReport = true) }
        viewModelScope.launch {
            when (val res = reportRepository.reportUser(otherUserId, reason, detail.roomId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingReport = false,
                            showReportDialog = false,
                            isBlockedByMe = true,
                            toast = UiText.StringResource(R.string.report_success),
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmittingReport = false,
                            error = res.message,
                        )
                    }
                }
                Resource.Loading -> {
                    _uiState.update { it.copy(isSubmittingReport = false) }
                }
            }
        }
    }

    fun dismissIdentityRequired() {
        _uiState.update { it.copy(showIdentityRequired = false) }
    }

    /**
     * Path-B button entry point. Runs the F-108 LINE identity gate
     * (AC-LINE-UNLOCK-003); on pass, opens the confirm dialog instead
     * of charging immediately. The actual RPC call happens in
     * [confirmUnlock] when the user taps the dialog's confirm button.
     */
    fun requestUnlock() {
        if (_uiState.value.isUnlocking) return
        if (_uiState.value.showUnlockConfirm) return
        viewModelScope.launch {
            val verified = when (val r = instructorRepository.getMyProfile()) {
                is Resource.Success -> r.data.lineUserId != null
                else -> false
            }
            if (!verified) {
                _uiState.update { it.copy(showIdentityRequired = true) }
                return@launch
            }
            _uiState.update { it.copy(showUnlockConfirm = true) }
        }
    }

    fun dismissUnlockConfirm() {
        if (_uiState.value.isUnlocking) return
        _uiState.update { it.copy(showUnlockConfirm = false) }
    }

    /**
     * Confirm-side of the Path-B unlock. Triggered by the dialog's
     * primary CTA. Backend `execute_unlock` enforces the same identity
     * gate (AC-LINE-UNLOCK-004) as a race fallback against the
     * pre-check in [requestUnlock].
     */
    fun confirmUnlock() {
        if (_uiState.value.isUnlocking) return
        _uiState.update { it.copy(isUnlocking = true) }
        viewModelScope.launch {
            when (val res = chatRepository.unlockPathBConversation(roomId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isUnlocking = false, showUnlockConfirm = false)
                    }
                    refresh()
                }
                is Resource.Error -> {
                    if (res.message is UiText.StringResource &&
                        res.message.resId == R.string.error_identity_not_verified
                    ) {
                        _uiState.update {
                            it.copy(
                                isUnlocking = false,
                                showUnlockConfirm = false,
                                showIdentityRequired = true,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isUnlocking = false,
                                showUnlockConfirm = false,
                                error = res.message,
                            )
                        }
                    }
                }
                Resource.Loading -> {
                    _uiState.update { it.copy(isUnlocking = false) }
                }
            }
        }
    }

    fun toggleBlock() {
        val detail = _uiState.value.roomDetail ?: return
        if (_uiState.value.isBlockInFlight) return
        val otherUserId = detail.otherParty.userId
        val currentlyBlocked = _uiState.value.isBlockedByMe
        _uiState.update { it.copy(isBlockInFlight = true) }
        viewModelScope.launch {
            val res = if (currentlyBlocked) {
                blockRepository.unblockUser(otherUserId)
            } else {
                blockRepository.blockUser(otherUserId)
            }
            when (res) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isBlockInFlight = false,
                            isBlockedByMe = !currentlyBlocked,
                            toast = UiText.StringResource(
                                if (currentlyBlocked) R.string.unblock_success
                                else R.string.block_success,
                            ),
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isBlockInFlight = false,
                            error = res.message,
                        )
                    }
                }
                Resource.Loading -> {
                    _uiState.update { it.copy(isBlockInFlight = false) }
                }
            }
        }
    }

    fun refresh() {
        stopRealtimeSubscription()
        load(showFullScreenLoader = false)
    }

    override fun onCleared() {
        stopRealtimeSubscription()
        if (roomId.isNotBlank()) activeRoomTracker.clearActiveRoom(roomId)
        super.onCleared()
    }
}

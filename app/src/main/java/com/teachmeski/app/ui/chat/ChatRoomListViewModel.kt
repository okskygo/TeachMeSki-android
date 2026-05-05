package com.teachmeski.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.domain.model.ChatRoom
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.ChatRepository
import com.teachmeski.app.ui.component.ActiveRole
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class ChatRoomListUiState(
    val rooms: List<ChatRoom> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val currentOffset: Int = 0,
    val error: UiText? = null,
    val selectedTab: Tab = Tab.All,
) {
    enum class Tab {
        All,
        Unread,
    }
}

@HiltViewModel
class ChatRoomListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatRoomListUiState())
    val uiState: StateFlow<ChatRoomListUiState> = _uiState.asStateFlow()

    private val loadMutex = Mutex()
    private var inboxJob: Job? = null

    /**
     * F-113 FR-113-001..004: the chat list is panel-scoped. The Composable
     * passes the current `activeRole` via `bind(role)` on first composition
     * and again whenever the role flips. On role change we reset state and
     * re-fetch from offset 0 so cross-panel rows never bleed into the list.
     */
    private var activeRole: ActiveRole? = null

    fun bind(role: ActiveRole) {
        if (activeRole == role) return
        val isFirstBind = activeRole == null
        activeRole = role
        if (isFirstBind) {
            // First bind: kick off initial load + inbox subscription.
            viewModelScope.launch { loadPage(reset = true) }
            startInboxSubscription()
        } else {
            // Role flipped: reset list and reload from page 0.
            _uiState.update { ChatRoomListUiState() }
            viewModelScope.launch { loadPage(reset = true) }
        }
    }

    private fun startInboxSubscription() {
        if (inboxJob?.isActive == true) return
        inboxJob = viewModelScope.launch {
            chatRepository.subscribeToInboxFlow()
                .catch { /* Non-fatal: list will still refresh on resume/pull-to-refresh */ }
                .collect { update -> applyInboxUpdate(update) }
        }
    }

    private suspend fun applyInboxUpdate(update: com.teachmeski.app.domain.model.InboxRoomUpdate) {
        val currentUserId = authRepository.currentUserId() ?: return
        val existing = _uiState.value.rooms.firstOrNull { it.id == update.roomId }
        if (existing == null) {
            // F-113 FR-113-015: realtime channel is shared across panels
            // (`inbox:{userId}`). Filter out updates for rooms not in the
            // current panel — they still bump the icon badge via the
            // `MainViewModel.refreshUnreadCount()` path, but must not
            // appear in the panel-scoped list.
            loadPage(reset = true)
            return
        }

        val senderIsMe = update.senderId != null && update.senderId == currentUserId
        val newUnread = when {
            senderIsMe -> existing.unreadCount
            else -> existing.unreadCount + 1
        }

        val updatedRoom = existing.copy(
            lastMessage = update.lastMessage ?: existing.lastMessage,
            lastMessageAt = update.lastMessageAt ?: existing.lastMessageAt,
            lastMessageSenderId = update.senderId ?: existing.lastMessageSenderId,
            unreadCount = newUnread,
        )

        _uiState.update { s ->
            val reordered = buildList<ChatRoom>(s.rooms.size) {
                add(updatedRoom)
                s.rooms.forEach { if (it.id != updatedRoom.id) add(it) }
            }
            s.copy(rooms = reordered)
        }
    }

    override fun onCleared() {
        inboxJob?.cancel()
        inboxJob = null
        super.onCleared()
    }

    fun loadPage(reset: Boolean) {
        if (!reset) {
            loadMore()
            return
        }
        val role = activeRole ?: return
        viewModelScope.launch {
            loadMutex.withLock {
                val showBlockingLoader = _uiState.value.rooms.isEmpty()
                _uiState.update {
                    it.copy(
                        isLoading = showBlockingLoader,
                        error = null,
                    )
                }

                when (val result = chatRepository.getChatRooms(role, 0)) {
                    is Resource.Success -> {
                        val (list, more) = result.data
                        _uiState.update {
                            it.copy(
                                rooms = list,
                                hasMore = more,
                                currentOffset = list.size,
                                isLoading = false,
                                isLoadingMore = false,
                                error = null,
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message,
                            )
                        }
                    }

                    Resource.Loading -> Unit
                }
            }
        }
    }

    fun loadMore() {
        val s = _uiState.value
        if (s.isLoading || s.isLoadingMore || !s.hasMore) return
        val role = activeRole ?: return
        viewModelScope.launch {
            loadMutex.withLock {
                val offset = _uiState.value.rooms.size
                _uiState.update {
                    it.copy(
                        isLoadingMore = true,
                        error = null,
                    )
                }

                when (val result = chatRepository.getChatRooms(role, offset)) {
                    is Resource.Success -> {
                        val (list, more) = result.data
                        _uiState.update { prev ->
                            val merged = prev.rooms + list
                            prev.copy(
                                rooms = merged,
                                hasMore = more,
                                currentOffset = merged.size,
                                isLoading = false,
                                isLoadingMore = false,
                                error = null,
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoadingMore = false,
                                error = result.message,
                            )
                        }
                    }

                    Resource.Loading -> {
                        _uiState.update { it.copy(isLoadingMore = false) }
                    }
                }
            }
        }
    }

    fun pullToRefresh() {
        val role = activeRole ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            loadMutex.withLock {
                when (val result = chatRepository.getChatRooms(role, 0)) {
                    is Resource.Success -> {
                        val (list, more) = result.data
                        _uiState.update {
                            it.copy(
                                rooms = list,
                                hasMore = more,
                                currentOffset = list.size,
                                isLoading = false,
                                isLoadingMore = false,
                                isRefreshing = false,
                                error = null,
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isRefreshing = false,
                                error = result.message,
                            )
                        }
                    }

                    Resource.Loading -> Unit
                }
            }
        }
    }

    fun refreshOnResume() {
        if (loadMutex.isLocked) return
        val role = activeRole ?: return
        viewModelScope.launch {
            loadMutex.withLock {
                val silent = _uiState.value.rooms.isNotEmpty()
                if (!silent) {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }

                when (val result = chatRepository.getChatRooms(role, 0)) {
                    is Resource.Success -> {
                        val (list, more) = result.data
                        _uiState.update {
                            it.copy(
                                rooms = list,
                                hasMore = more,
                                currentOffset = list.size,
                                isLoading = false,
                                isLoadingMore = false,
                                error = null,
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }

                    Resource.Loading -> Unit
                }
            }
        }
    }

    fun setTab(tab: ChatRoomListUiState.Tab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }
}

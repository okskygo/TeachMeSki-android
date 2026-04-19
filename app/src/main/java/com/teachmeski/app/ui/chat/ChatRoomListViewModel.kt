package com.teachmeski.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.domain.model.ChatRoom
import com.teachmeski.app.domain.repository.ChatRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatRoomListUiState())
    val uiState: StateFlow<ChatRoomListUiState> = _uiState.asStateFlow()

    private val loadMutex = Mutex()

    init {
        viewModelScope.launch {
            loadPage(reset = true)
        }
    }

    fun loadPage(reset: Boolean) {
        if (!reset) {
            loadMore()
            return
        }
        viewModelScope.launch {
            loadMutex.withLock {
                val showBlockingLoader = _uiState.value.rooms.isEmpty()
                _uiState.update {
                    it.copy(
                        isLoading = showBlockingLoader,
                        error = null,
                    )
                }

                when (val result = chatRepository.getChatRooms(0)) {
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
        viewModelScope.launch {
            loadMutex.withLock {
                val offset = _uiState.value.rooms.size
                _uiState.update {
                    it.copy(
                        isLoadingMore = true,
                        error = null,
                    )
                }

                when (val result = chatRepository.getChatRooms(offset)) {
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
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            loadMutex.withLock {
                when (val result = chatRepository.getChatRooms(0)) {
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
        viewModelScope.launch {
            loadMutex.withLock {
                val silent = _uiState.value.rooms.isNotEmpty()
                if (!silent) {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }

                when (val result = chatRepository.getChatRooms(0)) {
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

package com.teachmeski.app.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.domain.model.ExploreLessonRequest
import com.teachmeski.app.domain.repository.ExploreRepository
import com.teachmeski.app.domain.repository.WalletRepository
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

data class ExploreUiState(
    val requests: List<ExploreLessonRequest> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: UiText? = null,
    val currentPage: Int = 1,
    val totalCount: Int = 0,
    val hasMore: Boolean = false,
    val disciplineFilter: String? = null,
    val tokenBalance: Int = 0,
    val unlockDialogRequest: ExploreLessonRequest? = null,
    val unlockMessage: String = "",
    val isUnlocking: Boolean = false,
    val unlockError: UiText? = null,
    val unlockSuccessChatRoomId: String? = null,
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val walletRepository: WalletRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val loadMutex = Mutex()

    init {
        viewModelScope.launch { refreshTokenBalance() }
        viewModelScope.launch { loadPage(1) }
    }

    fun loadPage(page: Int) {
        viewModelScope.launch {
            loadMutex.withLock {
                val disciplineList = _uiState.value.disciplineFilter?.let { listOf(it) }
                if (page == 1) {
                    _uiState.update {
                        it.copy(
                            isLoading = true,
                            isLoadingMore = false,
                            error = null,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoadingMore = true,
                            error = null,
                        )
                    }
                }

                when (
                    val result = exploreRepository.getExploreLessonRequests(
                        page = page,
                        disciplineFilter = disciplineList,
                        resortFilter = null,
                    )
                ) {
                    is Resource.Success -> {
                        val (list, total) = result.data
                        _uiState.update { s ->
                            val merged = if (page == 1) list else s.requests + list
                            s.copy(
                                requests = merged,
                                isLoading = false,
                                isLoadingMore = false,
                                currentPage = page,
                                totalCount = total,
                                hasMore = merged.size < total,
                                error = null,
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                error = result.message,
                            )
                        }
                    }

                    Resource.Loading -> {
                        _uiState.update {
                            it.copy(
                                isLoading = page == 1 && it.isLoading,
                                isLoadingMore = page > 1 && it.isLoadingMore,
                            )
                        }
                    }
                }
            }
        }
    }

    fun loadMore() {
        val s = _uiState.value
        if (s.isLoading || s.isLoadingMore || !s.hasMore) return
        loadPage(s.currentPage + 1)
    }

    fun setDisciplineFilter(discipline: String?) {
        _uiState.update { it.copy(disciplineFilter = discipline) }
        loadPage(1)
    }

    fun openUnlockDialog(request: ExploreLessonRequest) {
        _uiState.update {
            it.copy(
                unlockDialogRequest = request,
                unlockMessage = "",
                unlockError = null,
                isUnlocking = false,
            )
        }
    }

    fun closeUnlockDialog() {
        _uiState.update {
            it.copy(
                unlockDialogRequest = null,
                unlockMessage = "",
                unlockError = null,
                isUnlocking = false,
            )
        }
    }

    fun setUnlockMessage(message: String) {
        _uiState.update { it.copy(unlockMessage = message, unlockError = null) }
    }

    fun confirmUnlock() {
        val request = _uiState.value.unlockDialogRequest ?: return
        val message = _uiState.value.unlockMessage
        viewModelScope.launch {
            _uiState.update { it.copy(isUnlocking = true, unlockError = null) }
            when (val result = exploreRepository.unlockLessonRequest(request.id, message)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isUnlocking = false,
                            unlockSuccessChatRoomId = result.data,
                            unlockDialogRequest = null,
                            unlockMessage = "",
                            unlockError = null,
                        )
                    }
                    refreshTokenBalance()
                    loadPage(1)
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isUnlocking = false, unlockError = result.message)
                    }
                }

                Resource.Loading -> Unit
            }
        }
    }

    fun consumeUnlockSuccess() {
        _uiState.update { it.copy(unlockSuccessChatRoomId = null) }
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }

    private suspend fun refreshTokenBalance() {
        when (val w = walletRepository.getWallet()) {
            is Resource.Success -> {
                _uiState.update { it.copy(tokenBalance = w.data.balance) }
            }

            is Resource.Error,
            Resource.Loading -> Unit
        }
    }
}

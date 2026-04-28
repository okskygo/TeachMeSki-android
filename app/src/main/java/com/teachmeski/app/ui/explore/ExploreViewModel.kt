package com.teachmeski.app.ui.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.ExploreLessonRequest
import com.teachmeski.app.domain.repository.ExploreRepository
import com.teachmeski.app.domain.repository.InstructorRepository
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
    val isRefreshing: Boolean = false,
    val disciplineFilter: String? = null,
    val tokenBalance: Int = 0,
    val unlockDialogRequest: ExploreLessonRequest? = null,
    val unlockMessage: String = "",
    val isUnlocking: Boolean = false,
    val unlockError: UiText? = null,
    val unlockSuccessChatRoomId: String? = null,
    /** F-108: when true, render IdentityRequiredDialog instead of unlock dialog. */
    val showIdentityRequired: Boolean = false,
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val walletRepository: WalletRepository,
    private val instructorRepository: InstructorRepository,
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
        viewModelScope.launch {
            // F-108 pre-check: refuse to even open the unlock dialog if
            // the instructor hasn't completed LINE identity binding. This
            // is the AC-LINE-UNLOCK-001 user-visible block; the backend
            // execute_unlock RPC also enforces it (AC-LINE-UNLOCK-002).
            val verified = when (val r = instructorRepository.getMyProfile()) {
                is Resource.Success -> r.data.lineUserId != null
                else -> false
            }
            if (!verified) {
                _uiState.update { it.copy(showIdentityRequired = true) }
                return@launch
            }
            _uiState.update {
                it.copy(
                    unlockDialogRequest = request,
                    unlockMessage = "",
                    unlockError = null,
                    isUnlocking = false,
                )
            }
        }
    }

    fun dismissIdentityRequired() {
        _uiState.update { it.copy(showIdentityRequired = false) }
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
                    // Backend may reject with identity_not_verified if the
                    // frontend pre-check was bypassed (e.g. profile state
                    // changed mid-flight). Surface the modal instead of an
                    // inline error in that case.
                    if (result.message is UiText.StringResource &&
                        result.message.resId == R.string.error_identity_not_verified
                    ) {
                        _uiState.update {
                            it.copy(
                                isUnlocking = false,
                                unlockDialogRequest = null,
                                unlockMessage = "",
                                unlockError = null,
                                showIdentityRequired = true,
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(isUnlocking = false, unlockError = result.message)
                        }
                    }
                }

                Resource.Loading -> Unit
            }
        }
    }

    fun consumeUnlockSuccess() {
        _uiState.update { it.copy(unlockSuccessChatRoomId = null) }
    }

    fun pullToRefresh() {
        viewModelScope.launch { refreshTokenBalance() }
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
            loadMutex.withLock {
                val disciplineList = _uiState.value.disciplineFilter?.let { listOf(it) }
                when (
                    val result = exploreRepository.getExploreLessonRequests(
                        page = 1,
                        disciplineFilter = disciplineList,
                        resortFilter = null,
                    )
                ) {
                    is Resource.Success -> {
                        val (list, total) = result.data
                        _uiState.update { s ->
                            s.copy(
                                requests = list,
                                isLoading = false,
                                isLoadingMore = false,
                                isRefreshing = false,
                                currentPage = 1,
                                totalCount = total,
                                hasMore = list.size < total,
                                error = null,
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(isRefreshing = false, error = result.message)
                        }
                    }
                    Resource.Loading -> Unit
                }
            }
        }
    }

    fun refreshOnResume() {
        if (loadMutex.isLocked) return
        viewModelScope.launch { refreshTokenBalance() }
        viewModelScope.launch {
            loadMutex.withLock {
                if (_uiState.value.requests.isEmpty()) {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }
                val disciplineList = _uiState.value.disciplineFilter?.let { listOf(it) }
                when (
                    val result = exploreRepository.getExploreLessonRequests(
                        page = 1,
                        disciplineFilter = disciplineList,
                        resortFilter = null,
                    )
                ) {
                    is Resource.Success -> {
                        val (list, total) = result.data
                        _uiState.update { s ->
                            s.copy(
                                requests = list,
                                isLoading = false,
                                isLoadingMore = false,
                                currentPage = 1,
                                totalCount = total,
                                hasMore = list.size < total,
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

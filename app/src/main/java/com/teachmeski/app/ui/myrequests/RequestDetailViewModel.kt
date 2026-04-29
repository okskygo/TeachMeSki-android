package com.teachmeski.app.ui.myrequests

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.InstructorPreview
import com.teachmeski.app.domain.model.InstructorSection
import com.teachmeski.app.domain.model.LessonRequest
import com.teachmeski.app.domain.repository.ChatRepository
import com.teachmeski.app.domain.repository.LessonRequestRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RequestDetailUiState(
    val detail: LessonRequest? = null,
    val unlockedInstructors: List<InstructorPreview> = emptyList(),
    val recommendedInstructors: List<InstructorPreview> = emptyList(),
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val isClosing: Boolean = false,
    val closeSuccess: Boolean = false,
    val firstMessageTarget: InstructorPreview? = null,
    val firstMessageDraft: String = "",
    val isSendingFirstMessage: Boolean = false,
    val firstMessageRoomId: String? = null,
    val isExpandingQuota: Boolean = false,
    val expandQuotaToast: UiText? = null,
) {
    val userInitiatedInstructors: List<InstructorPreview>
        get() = unlockedInstructors.filter { it.section == InstructorSection.UserInitiated }
    val expertInitiatedInstructors: List<InstructorPreview>
        get() = unlockedInstructors.filter { it.section == InstructorSection.ExpertInitiated }
}

@HiltViewModel
class RequestDetailViewModel @Inject constructor(
    private val lessonRequestRepository: LessonRequestRepository,
    private val chatRepository: ChatRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val requestId: String = savedStateHandle.get<String>("id") ?: ""

    private val _uiState = MutableStateFlow(RequestDetailUiState())
    val uiState: StateFlow<RequestDetailUiState> = _uiState.asStateFlow()

    init {
        Log.d("RequestDetailVM", "requestId=$requestId, keys=${savedStateHandle.keys()}")
        load()
    }

    fun load() {
        viewModelScope.launch {
            if (requestId.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        detail = null,
                        unlockedInstructors = emptyList(),
                        recommendedInstructors = emptyList(),
                        error = UiText.StringResource(R.string.error_load_request_detail),
                    )
                }
                return@launch
            }
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                )
            }
            coroutineScope {
                val detailDeferred = async { lessonRequestRepository.getLessonRequestDetail(requestId) }
                val unlockedDeferred = async { lessonRequestRepository.getUnlockedInstructors(requestId) }
                val recommendedDeferred = async { lessonRequestRepository.getRecommendedInstructors(requestId) }
                val detailResult = detailDeferred.await()
                val unlockedResult = unlockedDeferred.await()
                val recommendedResult = recommendedDeferred.await()
                when (detailResult) {
                    is Resource.Success -> {
                        val unlocked =
                            when (unlockedResult) {
                                is Resource.Success -> unlockedResult.data
                                else -> emptyList()
                            }
                        val recommended =
                            when (recommendedResult) {
                                is Resource.Success -> recommendedResult.data
                                else -> emptyList()
                            }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                detail = detailResult.data,
                                unlockedInstructors = unlocked,
                                recommendedInstructors = recommended,
                                error = null,
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                detail = null,
                                unlockedInstructors = emptyList(),
                                recommendedInstructors = emptyList(),
                                error = detailResult.message,
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            }
        }
    }

    fun close() {
        if (requestId.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isClosing = true, error = null) }
            when (val result = lessonRequestRepository.closeLessonRequest(requestId)) {
                is Resource.Success -> {
                    when (val refreshed = lessonRequestRepository.getLessonRequestDetail(requestId)) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    isClosing = false,
                                    detail = refreshed.data,
                                    closeSuccess = true,
                                )
                            }
                        }
                        else -> {
                            _uiState.update {
                                it.copy(
                                    isClosing = false,
                                    closeSuccess = true,
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isClosing = false,
                            error = result.message,
                        )
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isClosing = false) }
                }
            }
        }
    }

    fun consumeCloseSuccess() {
        _uiState.update { it.copy(closeSuccess = false) }
    }

    fun openFirstMessageDialog(instructor: InstructorPreview) {
        _uiState.update {
            it.copy(
                firstMessageTarget = instructor,
                firstMessageDraft = "",
                firstMessageRoomId = null,
            )
        }
    }

    fun dismissFirstMessageDialog() {
        if (_uiState.value.isSendingFirstMessage) return
        _uiState.update { it.copy(firstMessageTarget = null) }
    }

    fun updateFirstMessageDraft(text: String) {
        if (text.length > 2000) return
        _uiState.update { it.copy(firstMessageDraft = text) }
    }

    fun sendFirstMessage() {
        val s = _uiState.value
        val target = s.firstMessageTarget ?: return
        val body = s.firstMessageDraft.trim()
        if (body.isEmpty() || requestId.isEmpty()) return
        _uiState.update { it.copy(isSendingFirstMessage = true) }
        viewModelScope.launch {
            when (val res = chatRepository.createPathBChatRoom(target.instructorId, requestId, body)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSendingFirstMessage = false,
                            firstMessageTarget = null,
                            firstMessageDraft = "",
                            firstMessageRoomId = res.data,
                        )
                    }
                    load()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSendingFirstMessage = false,
                            error = res.message,
                        )
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isSendingFirstMessage = false) }
                }
            }
        }
    }

    fun consumeFirstMessageRoomId() {
        _uiState.update { it.copy(firstMessageRoomId = null) }
    }

    /**
     * F-109: trigger student-initiated quota expansion. On success, optimistically bumps
     * the in-memory quotaLimit so the CTA disappears. On failure, surfaces an error toast.
     */
    fun expandQuota() {
        val current = _uiState.value
        val detail = current.detail ?: return
        if (current.isExpandingQuota) return
        viewModelScope.launch {
            _uiState.update { it.copy(isExpandingQuota = true) }
            when (val result = lessonRequestRepository.expandQuota(detail.id)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isExpandingQuota = false,
                            detail = it.detail?.copy(quotaLimit = result.data),
                            expandQuotaToast = UiText.StringResource(
                                R.string.my_requests_find_more_success_toast,
                            ),
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isExpandingQuota = false,
                            expandQuotaToast = UiText.StringResource(
                                R.string.my_requests_find_more_error_toast,
                            ),
                        )
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isExpandingQuota = false) }
                }
            }
        }
    }

    fun consumeExpandQuotaToast() {
        _uiState.update { it.copy(expandQuotaToast = null) }
    }
}

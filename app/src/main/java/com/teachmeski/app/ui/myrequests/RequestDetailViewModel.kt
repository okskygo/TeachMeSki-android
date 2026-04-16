package com.teachmeski.app.ui.myrequests

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.InstructorPreview
import com.teachmeski.app.domain.model.LessonRequest
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
)

@HiltViewModel
class RequestDetailViewModel @Inject constructor(
    private val lessonRequestRepository: LessonRequestRepository,
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
}

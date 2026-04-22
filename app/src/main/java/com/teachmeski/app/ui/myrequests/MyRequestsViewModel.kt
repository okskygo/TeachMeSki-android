package com.teachmeski.app.ui.myrequests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.domain.model.LessonRequestListItem
import com.teachmeski.app.domain.repository.LessonRequestRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyRequestsUiState(
    val requests: List<LessonRequestListItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,
)

@HiltViewModel
class MyRequestsViewModel @Inject constructor(
    private val lessonRequestRepository: LessonRequestRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyRequestsUiState())
    val uiState: StateFlow<MyRequestsUiState> = _uiState.asStateFlow()

    init {
        load(isInitial = true)
    }

    fun refresh() {
        load(isInitial = false, isRefresh = true)
    }

    /**
     * Silently reload on lifecycle resume (e.g. returning from the wizard or
     * switching back to this tab) so newly-created requests appear without
     * requiring a manual pull-to-refresh. Does not toggle the refresh spinner.
     */
    fun refreshOnResume() {
        load(isInitial = false, isRefresh = false)
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun load(isInitial: Boolean, isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = isInitial,
                    isRefreshing = isRefresh,
                    error = null,
                )
            }
            when (val result = lessonRequestRepository.getMyLessonRequests()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            requests = result.data,
                            isLoading = false,
                            isRefreshing = false,
                            error = null,
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = result.message,
                        )
                    }
                }
                Resource.Loading -> {
                    _uiState.update {
                        it.copy(
                            isLoading = isInitial,
                            isRefreshing = isRefresh,
                        )
                    }
                }
            }
        }
    }
}

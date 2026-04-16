package com.teachmeski.app.ui.unlocked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.domain.model.UnlockedRoom
import com.teachmeski.app.domain.repository.ExploreRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UnlockedUiState(
    val rooms: List<UnlockedRoom> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: UiText? = null,
)

@HiltViewModel
class UnlockedViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UnlockedUiState())
    val uiState: StateFlow<UnlockedUiState> = _uiState.asStateFlow()

    init {
        load(isRefresh = false)
    }

    fun load(isRefresh: Boolean = false) {
        viewModelScope.launch {
            val initial = _uiState.value.rooms.isEmpty()
            _uiState.update { state ->
                state.copy(
                    isLoading = initial && !isRefresh,
                    isRefreshing = isRefresh,
                    error = null,
                )
            }
            when (val result = exploreRepository.getUnlockedRooms()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            rooms = result.data,
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
                            isLoading = initial && !isRefresh,
                            isRefreshing = isRefresh,
                        )
                    }
                }
            }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }
}

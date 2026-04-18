package com.teachmeski.app.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.InstructorRepository
import com.teachmeski.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InstructorAccountSettingsUiState(
    val email: String = "",
    val userId: String = "",
    val initialPhone: String? = null,
    val initialPhoneVerifiedAt: String? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class InstructorAccountSettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val instructorRepository: InstructorRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        InstructorAccountSettingsUiState(
            email = authRepository.currentUserEmail().orEmpty(),
            userId = authRepository.currentUserId().orEmpty(),
        ),
    )
    val uiState: StateFlow<InstructorAccountSettingsUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            when (val result = instructorRepository.getMyProfile()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            initialPhone = result.data.phone,
                            initialPhoneVerifiedAt = result.data.phoneVerifiedAt,
                            isLoading = false,
                        )
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false) }
                is Resource.Loading -> Unit
            }
        }
    }
}

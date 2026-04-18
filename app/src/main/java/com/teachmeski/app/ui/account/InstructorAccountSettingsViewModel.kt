package com.teachmeski.app.ui.account

import androidx.lifecycle.ViewModel
import com.teachmeski.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class InstructorAccountSettingsUiState(
    val email: String = "",
    val userId: String = "",
)

@HiltViewModel
class InstructorAccountSettingsViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        InstructorAccountSettingsUiState(
            email = authRepository.currentUserEmail().orEmpty(),
            userId = authRepository.currentUserId().orEmpty(),
        ),
    )
    val uiState: StateFlow<InstructorAccountSettingsUiState> = _uiState.asStateFlow()
}

package com.teachmeski.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.R
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val error: UiText? = null,
)

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun sendResetEmail() {
        val email = _uiState.value.email.trim()
        if (email.isBlank() || !email.contains("@")) {
            _uiState.update {
                it.copy(error = UiText.StringResource(R.string.auth_error_invalid_email))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.resetPasswordForEmail(email)
            _uiState.update { it.copy(isLoading = false, success = true) }
        }
    }
}

package com.teachmeski.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.R
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val needsEmailVerification: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun signIn() {
        val state = _uiState.value
        val email = state.email.trim()
        val password = state.password

        if (email.isBlank() || !email.contains("@")) {
            _uiState.update {
                it.copy(error = UiText.StringResource(R.string.auth_error_invalid_email))
            }
            return
        }
        if (password.isBlank()) {
            _uiState.update {
                it.copy(error = UiText.StringResource(R.string.auth_error_password_required))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.signIn(email, password)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
                is Resource.Error -> {
                    val isUnverified = result.message is UiText.StringResource &&
                        (result.message as UiText.StringResource).resId == R.string.auth_error_email_not_confirmed
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message,
                            needsEmailVerification = isUnverified,
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null, needsEmailVerification = false) }
    }
}

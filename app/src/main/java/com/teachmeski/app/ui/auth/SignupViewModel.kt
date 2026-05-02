package com.teachmeski.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.R
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.ui.component.PasswordRules
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignupUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsChecked: Boolean = false,
    val passwordRules: PasswordRules = PasswordRules(),
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val signupSuccess: Boolean = false,
) {
    val confirmMatch: Boolean
        get() = password == confirmPassword && confirmPassword.isNotEmpty()

    val canSubmit: Boolean
        get() = passwordRules.allPassed && confirmMatch && termsChecked &&
            displayName.isNotBlank() && email.contains("@")
}

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    fun onDisplayNameChange(value: String) {
        _uiState.update { it.copy(displayName = value, error = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordRules = PasswordRules.check(value),
                error = null,
            )
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, error = null) }
    }

    fun onTermsCheckedChange(checked: Boolean) {
        _uiState.update { it.copy(termsChecked = checked) }
    }

    fun signUp() {
        val state = _uiState.value
        val displayName = state.displayName.trim()
        val email = state.email.trim()

        if (displayName.isBlank()) {
            _uiState.update {
                it.copy(error = UiText.StringResource(R.string.auth_error_display_name_required))
            }
            return
        }
        if (email.isBlank() || !email.contains("@")) {
            _uiState.update {
                it.copy(error = UiText.StringResource(R.string.auth_error_invalid_email))
            }
            return
        }
        if (!state.passwordRules.allPassed) {
            val errorRes = when {
                !state.passwordRules.hasMinLength -> R.string.auth_error_password_too_short
                !state.passwordRules.hasMaxLength -> R.string.auth_error_password_too_long
                !state.passwordRules.hasUppercase -> R.string.auth_error_password_no_uppercase
                !state.passwordRules.hasLowercase -> R.string.auth_error_password_no_lowercase
                !state.passwordRules.hasDigit -> R.string.auth_error_password_no_digit
                else -> R.string.auth_error_password_too_short
            }
            _uiState.update {
                it.copy(error = UiText.StringResource(errorRes))
            }
            return
        }
        if (!state.confirmMatch) {
            _uiState.update {
                it.copy(error = UiText.StringResource(R.string.auth_error_password_confirm_mismatch))
            }
            return
        }
        if (!state.termsChecked) {
            _uiState.update {
                it.copy(error = UiText.StringResource(R.string.auth_error_terms_required))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            // Pre-check before signUp: Supabase silently returns HTTP 200
            // (no email sent) when the address is already registered, so
            // a duplicate signUp would otherwise leave the user staring
            // at "check your inbox" with no code in flight. Pre-check
            // failures fail-open to preserve transient-network tolerance.
            val precheck = authRepository.checkEmailRegistered(email)
            if (precheck is Resource.Success && precheck.data == true) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = UiText.StringResource(R.string.auth_error_email_already_registered),
                    )
                }
                return@launch
            }
            when (val result = authRepository.signUp(email, state.password, displayName)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, signupSuccess = true) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Resource.Loading -> Unit
            }
        }
    }
}

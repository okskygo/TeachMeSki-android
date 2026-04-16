package com.teachmeski.app.ui.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.R
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VerifyEmailUiState(
    val email: String = "",
    val otp: String = "",
    val isVerifying: Boolean = false,
    val isResending: Boolean = false,
    val error: UiText? = null,
    val resendCooldown: Int = 0,
    val resendMessage: UiText? = null,
)

@HiltViewModel
class VerifyEmailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        VerifyEmailUiState(email = savedStateHandle.get<String>("email") ?: ""),
    )
    val uiState: StateFlow<VerifyEmailUiState> = _uiState.asStateFlow()

    private var cooldownJob: Job? = null

    fun onOtpChange(value: String) {
        _uiState.update { it.copy(otp = value, error = null) }
    }

    fun verifyOtp() {
        val state = _uiState.value
        if (state.otp.length != 6) return

        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, error = null) }
            when (val result = authRepository.verifyEmailOtp(state.email, state.otp)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isVerifying = false) }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isVerifying = false, error = result.message, otp = "")
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun resendOtp() {
        val state = _uiState.value
        if (state.resendCooldown > 0 || state.isResending) return

        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true, error = null) }
            when (authRepository.resendEmailOtp(state.email)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isResending = false,
                            resendMessage = UiText.StringResource(R.string.auth_verify_email_resend_sent),
                        )
                    }
                    startCooldown(60)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isResending = false,
                            error = UiText.StringResource(R.string.auth_error_generic),
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun startCooldown(seconds: Int) {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            for (i in seconds downTo 1) {
                _uiState.update { it.copy(resendCooldown = i) }
                delay(1_000)
            }
            _uiState.update { it.copy(resendCooldown = 0, resendMessage = null) }
        }
    }
}

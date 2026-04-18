package com.teachmeski.app.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.domain.repository.CheckAndSendResult
import com.teachmeski.app.domain.repository.ConfirmPhoneResult
import com.teachmeski.app.domain.repository.PhoneVerificationRepository
import com.teachmeski.app.util.PhoneUtils
import com.teachmeski.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PhoneVerificationPhase {
    data class Verified(val phone: String) : PhoneVerificationPhase
    data object Idle : PhoneVerificationPhase
    data class OtpInput(val phone: String) : PhoneVerificationPhase
    data class Success(val phone: String) : PhoneVerificationPhase
}

data class PhoneVerificationUiState(
    val phase: PhoneVerificationPhase = PhoneVerificationPhase.Idle,
    val phoneInput: String = "",
    val isSending: Boolean = false,
    val isVerifying: Boolean = false,
    val cooldownSeconds: Int = 0,
    val error: String? = null,
)

@HiltViewModel
class PhoneVerificationViewModel @Inject constructor(
    private val repository: PhoneVerificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhoneVerificationUiState())
    val uiState: StateFlow<PhoneVerificationUiState> = _uiState.asStateFlow()

    private var cooldownJob: Job? = null

    fun initialize(phone: String?, phoneVerifiedAt: String?) {
        if (phone != null && phoneVerifiedAt != null) {
            _uiState.value = PhoneVerificationUiState(
                phase = PhoneVerificationPhase.Verified(phone),
            )
        }
    }

    fun onPhoneInputChange(value: String) {
        _uiState.update { it.copy(phoneInput = value, error = null) }
    }

    fun sendOtp() {
        if (_uiState.value.isSending) return
        _uiState.update { it.copy(isSending = true, error = null) }

        viewModelScope.launch {
            when (val result = repository.checkAndSend(_uiState.value.phoneInput)) {
                is CheckAndSendResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            phase = PhoneVerificationPhase.OtpInput(result.phone),
                        )
                    }
                    startCooldown(60)
                }
                is CheckAndSendResult.CooldownBlocked -> {
                    _uiState.update { it.copy(isSending = false) }
                    startCooldown(result.seconds)
                }
                is CheckAndSendResult.Error -> {
                    _uiState.update {
                        it.copy(isSending = false, error = result.type)
                    }
                }
            }
        }
    }

    fun resendOtp() {
        val phase = _uiState.value.phase
        if (phase !is PhoneVerificationPhase.OtpInput) return
        if (_uiState.value.isSending || _uiState.value.cooldownSeconds > 0) return

        _uiState.update { it.copy(isSending = true, error = null) }

        viewModelScope.launch {
            when (val result = repository.checkAndSend(_uiState.value.phoneInput)) {
                is CheckAndSendResult.Success -> {
                    _uiState.update { it.copy(isSending = false) }
                    startCooldown(60)
                }
                is CheckAndSendResult.CooldownBlocked -> {
                    _uiState.update { it.copy(isSending = false) }
                    startCooldown(result.seconds)
                }
                is CheckAndSendResult.Error -> {
                    _uiState.update {
                        it.copy(isSending = false, error = result.type)
                    }
                }
            }
        }
    }

    fun onOtpComplete(code: String) {
        val phase = _uiState.value.phase
        if (phase !is PhoneVerificationPhase.OtpInput) return
        if (_uiState.value.isVerifying) return

        _uiState.update { it.copy(isVerifying = true, error = null) }

        viewModelScope.launch {
            when (repository.verifyOtp(phase.phone, code)) {
                is Resource.Success -> confirmVerification(phase.phone)
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isVerifying = false, error = "otp")
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private suspend fun confirmVerification(phone: String) {
        when (val result = repository.confirm()) {
            is ConfirmPhoneResult.Success -> {
                _uiState.update {
                    it.copy(
                        isVerifying = false,
                        phase = PhoneVerificationPhase.Success(phone),
                    )
                }
            }
            is ConfirmPhoneResult.Error -> {
                _uiState.update {
                    it.copy(isVerifying = false, error = result.type)
                }
            }
        }
    }

    private fun startCooldown(seconds: Int) {
        cooldownJob?.cancel()
        _uiState.update { it.copy(cooldownSeconds = seconds) }
        cooldownJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _uiState.update { it.copy(cooldownSeconds = remaining) }
            }
        }
    }

    fun maskedPhone(e164: String): String = PhoneUtils.maskPhone(e164)
}

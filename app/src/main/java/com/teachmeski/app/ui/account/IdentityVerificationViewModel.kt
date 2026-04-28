package com.teachmeski.app.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.R
import com.teachmeski.app.auth.LineBindResultBus
import com.teachmeski.app.auth.LineBindResultUi
import com.teachmeski.app.auth.LineCallbackActivity
import com.teachmeski.app.domain.repository.InstructorRepository
import com.teachmeski.app.domain.repository.LineBindingRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IdentityVerificationState(
    val isVerified: Boolean = false,
    val isLoading: Boolean = true,
    /** Set when the section should launch a Custom Tab; cleared by [authorizeUrlConsumed]. */
    val authorizeUrl: String? = null,
    /** Set when a snackbar/toast should be shown; cleared by [toastConsumed]. */
    val toast: UiText? = null,
)

@HiltViewModel
class IdentityVerificationViewModel @Inject constructor(
    private val instructorRepository: InstructorRepository,
    private val lineBindingRepository: LineBindingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(IdentityVerificationState())
    val state: StateFlow<IdentityVerificationState> = _state.asStateFlow()

    init {
        loadProfile()
        viewModelScope.launch {
            LineBindResultBus.flow.collect { result ->
                handleResult(result)
                // Drop the replay cache so the next time the user
                // opens the account-settings screen we do not
                // re-show the same "Identity verified" toast.
                LineBindResultBus.consume()
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val verified = when (val r = instructorRepository.getMyProfile()) {
                is Resource.Success -> r.data.lineUserId != null
                else -> false
            }
            _state.update { it.copy(isVerified = verified, isLoading = false) }
        }
    }

    fun onBindClick() {
        _state.update { it.copy(authorizeUrl = lineBindingRepository.buildAuthorizeUrl()) }
    }

    fun authorizeUrlConsumed() {
        _state.update { it.copy(authorizeUrl = null) }
    }

    fun toastConsumed() {
        _state.update { it.copy(toast = null) }
    }

    private fun handleResult(result: LineBindResultUi) {
        val verifiedNow: Boolean
        val toastRes: Int
        when (result.kind) {
            LineCallbackActivity.EXTRA_KIND_SUCCESS -> {
                verifiedNow = true
                toastRes = R.string.identity_bind_success_toast
            }
            LineCallbackActivity.EXTRA_KIND_CANCELLED -> {
                verifiedNow = _state.value.isVerified
                toastRes = R.string.identity_bind_error_cancelled
            }
            else -> {
                verifiedNow = _state.value.isVerified
                toastRes = when (result.errorCode) {
                    LineCallbackActivity.ERROR_ALREADY_USED ->
                        R.string.identity_bind_error_already_used
                    LineCallbackActivity.ERROR_ALREADY_BOUND ->
                        R.string.identity_bind_error_already_bound
                    else -> R.string.identity_bind_error_generic
                }
            }
        }
        _state.update {
            it.copy(
                isVerified = verifiedNow,
                toast = UiText.StringResource(toastRes),
            )
        }
    }
}

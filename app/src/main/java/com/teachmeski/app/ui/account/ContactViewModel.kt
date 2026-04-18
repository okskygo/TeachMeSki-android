package com.teachmeski.app.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.R
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.ContactRepository
import com.teachmeski.app.domain.repository.UserRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private val EMAIL_REGEX = Regex("""^[^\s@]+@[^\s@]+\.[^\s@]+$""")
private const val MIN_MESSAGE_LENGTH = 10

data class ContactUiState(
    val name: String = "",
    val email: String = "",
    val message: String = "",
    val nameError: UiText? = null,
    val emailError: UiText? = null,
    val messageError: UiText? = null,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val error: UiText? = null,
)

@HiltViewModel
class ContactViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactUiState())
    val uiState: StateFlow<ContactUiState> = _uiState.asStateFlow()

    init {
        prefillUserInfo()
    }

    private fun prefillUserInfo() {
        viewModelScope.launch {
            val email = authRepository.currentUserEmail().orEmpty()
            _uiState.update { it.copy(email = email) }
            val userId = authRepository.currentUserId() ?: return@launch
            when (val result = userRepository.getUserById(userId)) {
                is Resource.Success -> {
                    val displayName = result.data.displayName.orEmpty()
                    _uiState.update { it.copy(name = displayName) }
                }
                else -> Unit
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update {
            it.copy(name = value, nameError = null, submitSuccess = false, error = null)
        }
    }

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(email = value, emailError = null, submitSuccess = false, error = null)
        }
    }

    fun onMessageChange(value: String) {
        _uiState.update {
            it.copy(message = value, messageError = null, submitSuccess = false, error = null)
        }
    }

    fun submit() {
        val state = _uiState.value
        val nameTrim = state.name.trim()
        val emailTrim = state.email.trim()
        val messageTrim = state.message.trim()

        val nameError = if (nameTrim.isEmpty()) {
            UiText.StringResource(R.string.contact_error_name_required)
        } else {
            null
        }
        val emailError = if (!EMAIL_REGEX.matches(emailTrim)) {
            UiText.StringResource(R.string.contact_error_email_invalid)
        } else {
            null
        }
        val messageError = if (messageTrim.length < MIN_MESSAGE_LENGTH) {
            UiText.StringResource(R.string.contact_error_message_too_short)
        } else {
            null
        }

        if (nameError != null || emailError != null || messageError != null) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    messageError = messageError,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    error = null,
                    nameError = null,
                    emailError = null,
                    messageError = null,
                )
            }
            when (val result = contactRepository.submitContact(nameTrim, emailTrim, messageTrim)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, submitSuccess = true)
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isSubmitting = false, submitSuccess = false, error = result.message)
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }
}

package com.teachmeski.app.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.R
import com.teachmeski.app.domain.repository.AuthRepository
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

private const val MAX_DISPLAY_NAME_LENGTH = 50

data class AccountUiState(
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: UiText? = null,
    val isLoading: Boolean = false,
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val userId = authRepository.currentUserId()
            if (userId == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = UiText.StringResource(R.string.auth_error_generic),
                    )
                }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null) }
            val email = authRepository.currentUserEmail().orEmpty()
            when (val result = userRepository.getUserById(userId)) {
                is Resource.Success -> {
                    val user = result.data
                    _uiState.update {
                        it.copy(
                            displayName = user.displayName.orEmpty(),
                            email = email,
                            avatarUrl = user.avatarUrl,
                            isLoading = false,
                            error = null,
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            email = email,
                            error = result.message,
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onDisplayNameChange(value: String) {
        _uiState.update {
            it.copy(
                displayName = value,
                saveSuccess = false,
                error = null,
            )
        }
    }

    fun updateDisplayName(name: String) {
        val userId = authRepository.currentUserId() ?: return
        val trimmed = name.trim()
        if (trimmed.length > MAX_DISPLAY_NAME_LENGTH) {
            _uiState.update {
                it.copy(error = UiText.StringResource(R.string.account_display_name_max_error))
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, saveSuccess = false) }
            when (val result = userRepository.updateDisplayName(userId, trimmed)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            displayName = trimmed,
                            saveSuccess = true,
                            error = null,
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isSaving = false, saveSuccess = false, error = result.message)
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun consumeSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    suspend fun signOut(): Resource<Unit> = authRepository.signOut()
}

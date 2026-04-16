package com.teachmeski.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.domain.model.UserRole
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.UserRepository
import com.teachmeski.app.ui.component.ActiveRole
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.RolePreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Authenticated(val activeRole: ActiveRole) : MainUiState
    data object Unauthenticated : MainUiState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val rolePreferences: RolePreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        observeSession()
    }

    private fun observeSession() {
        viewModelScope.launch {
            authRepository.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Authenticated -> resolveRole()
                    is SessionStatus.NotAuthenticated -> {
                        _uiState.value = MainUiState.Unauthenticated
                    }
                    SessionStatus.Initializing -> {
                        _uiState.value = MainUiState.Loading
                    }
                    is SessionStatus.RefreshFailure -> {
                        _uiState.value = MainUiState.Unauthenticated
                    }
                }
            }
        }
    }

    private suspend fun resolveRole() {
        val userId = authRepository.currentUserId() ?: run {
            _uiState.value = MainUiState.Unauthenticated
            return
        }

        when (val result = userRepository.getUserById(userId)) {
            is Resource.Success -> {
                val user = result.data
                if (user.deletedAt != null) {
                    authRepository.signOut()
                    return
                }
                val activeRole = when (user.role) {
                    UserRole.Student -> ActiveRole.Student
                    UserRole.Instructor -> ActiveRole.Instructor
                    UserRole.Both -> {
                        rolePreferences.getLastActiveRole(userId)
                            ?: ActiveRole.Instructor
                    }
                }
                rolePreferences.setLastActiveRole(userId, activeRole)
                _uiState.value = MainUiState.Authenticated(activeRole)
            }
            is Resource.Error -> {
                _uiState.value = MainUiState.Unauthenticated
            }
            is Resource.Loading -> Unit
        }
    }

    fun switchRole(newRole: ActiveRole) {
        val userId = authRepository.currentUserId() ?: return
        viewModelScope.launch {
            rolePreferences.setLastActiveRole(userId, newRole)
            _uiState.value = MainUiState.Authenticated(newRole)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}

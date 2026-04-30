package com.teachmeski.app.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.domain.model.PendingInstructorProfile
import com.teachmeski.app.domain.model.UserRole
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.ChatRepository
import com.teachmeski.app.domain.repository.InstructorRepository
import com.teachmeski.app.domain.repository.UserRepository
import com.teachmeski.app.notifications.NotificationDeepLinkBus
import com.teachmeski.app.notifications.PushTokenManager
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
    data class Authenticated(
        val activeRole: ActiveRole,
        val userRole: UserRole,
        val unreadCount: Int = 0,
    ) : MainUiState
    data object Unauthenticated : MainUiState
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val rolePreferences: RolePreferences,
    private val instructorRepository: InstructorRepository,
    private val pendingProfile: PendingInstructorProfile,
    private val chatRepository: ChatRepository,
    private val pushTokenManager: PushTokenManager,
    val notificationDeepLinkBus: NotificationDeepLinkBus,
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
                    is SessionStatus.Authenticated -> {
                        flushPendingInstructorProfileIfNeeded()
                        resolveRole()
                    }
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

    private suspend fun flushPendingInstructorProfileIfNeeded() {
        val data = pendingProfile.get() ?: return
        try {
            when (
                val result =
                    instructorRepository.createProfile(
                        discipline = data.discipline,
                        teachableLevels = data.teachableLevels,
                        resortIds = data.resortIds,
                        certifications = data.certifications,
                        certificationOther = data.certificationOther,
                        displayName = data.displayName,
                        bio = data.bio,
                        languages = data.languages,
                        priceHalfDay = data.priceHalfDay,
                        priceFullDay = data.priceFullDay,
                        offersTransport = data.offersTransport,
                        offersPhotography = data.offersPhotography,
                    )
            ) {
                is Resource.Success ->
                    Log.d("MainViewModel", "Created instructor profile from pending wizard data")
                is Resource.Error ->
                    Log.e("MainViewModel", "createProfile failed: ${result.message}")
                is Resource.Loading -> Unit
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "createProfile threw", e)
        } finally {
            pendingProfile.clear()
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
                _uiState.value = MainUiState.Authenticated(activeRole, user.role)
                refreshUnreadCount()
                pushTokenManager.registerCurrentDeviceToken(userId)
            }
            is Resource.Error -> {
                _uiState.value = MainUiState.Unauthenticated
            }
            is Resource.Loading -> Unit
        }
    }

    fun switchRole(newRole: ActiveRole) {
        val userId = authRepository.currentUserId() ?: return
        val current = _uiState.value as? MainUiState.Authenticated ?: return
        viewModelScope.launch {
            rolePreferences.setLastActiveRole(userId, newRole)
            _uiState.value = MainUiState.Authenticated(newRole, current.userRole, current.unreadCount)
        }
    }

    fun refreshUnreadCount() {
        viewModelScope.launch {
            val res = chatRepository.getUnreadCount()
            if (res is Resource.Success) {
                val current = _uiState.value as? MainUiState.Authenticated ?: return@launch
                _uiState.value = current.copy(unreadCount = res.data)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            val userId = authRepository.currentUserId()
            pushTokenManager.unregisterCurrentDeviceToken()
            if (userId != null) {
                rolePreferences.clearLastActiveRole(userId)
            }
            authRepository.signOut()
        }
    }
}

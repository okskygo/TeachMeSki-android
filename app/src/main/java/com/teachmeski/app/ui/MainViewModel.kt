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
        /** F-113 FR-113-007: current-panel unread (drives bottom-tab badge). */
        val unreadCount: Int = 0,
        /** F-113 FR-113-008: instructor-panel unread (used for icon-badge sum). */
        val instructorPanelUnread: Int = 0,
        /** F-113 FR-113-008: student-panel unread (used for icon-badge sum). */
        val studentPanelUnread: Int = 0,
    ) : MainUiState {
        /** F-113 FR-113-008: app-icon badge stays stable across panel switches. */
        val iconBadge: Int get() = instructorPanelUnread + studentPanelUnread
    }
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
                        // F-113 FR-113-019 / AC-113-014: drop any
                        // pending notification deep links so a push that
                        // arrived during the signed-out interval is not
                        // replayed after the next sign-in.
                        notificationDeepLinkBus.clearPending()
                        _uiState.value = MainUiState.Unauthenticated
                    }
                    SessionStatus.Initializing -> {
                        _uiState.value = MainUiState.Loading
                    }
                    is SessionStatus.RefreshFailure -> {
                        notificationDeepLinkBus.clearPending()
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
                        // For re-auth events (e.g. Supabase token refresh), a
                        // concurrent switchRole() may have already updated _uiState
                        // synchronously. Prefer that in-memory value over DataStore,
                        // which might still hold the previous role if the DataStore
                        // persist hasn't completed yet — avoiding a race where
                        // resolveRole() reads stale DataStore and reverts the role.
                        val inMemory = (_uiState.value as? MainUiState.Authenticated)?.activeRole
                        Log.d("TMS_NAV", "resolveRole: Both user inMemory=$inMemory")
                        inMemory
                            ?: rolePreferences.getLastActiveRole(userId)
                            ?: ActiveRole.Instructor
                    }
                }
                Log.d("TMS_NAV", "resolveRole: final activeRole=$activeRole")
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

    /**
     * Re-runs `resolveRole()` so a fresh `users.role` value (typically just
     * flipped to `Both` server-side by `InstructorRepository.createProfile`)
     * is reflected in `MainUiState.userRole`. Call this from the instructor
     * wizard's success path so the post-wizard root re-render and the
     * AccountScreen role-switch row both see the new role immediately
     * instead of waiting for the next process cold-start.
     */
    fun refreshRole() {
        viewModelScope.launch {
            resolveRole()
        }
    }

    // Set to true before a notification-triggered role switch so the
    // graph-re-root LaunchedEffect in AuthenticatedApp skips navigation
    // (the notification handler already sets up the correct back stack).
    @Volatile
    var suppressGraphNavOnRoleChange = false

    fun switchRole(newRole: ActiveRole) {
        val userId = authRepository.currentUserId() ?: return
        val current = _uiState.value as? MainUiState.Authenticated ?: return
        Log.d("TMS_NAV", "switchRole: $newRole started, suppressFlag=${suppressGraphNavOnRoleChange}")
        // F-113 FR-113-008: derive new tab-badge from cached per-panel
        // counts without re-fetching; icon badge (sum) is unchanged.
        val newTabBadge = when (newRole) {
            ActiveRole.Instructor -> current.instructorPanelUnread
            ActiveRole.Student -> current.studentPanelUnread
        }
        // Update _uiState synchronously so that any concurrent resolveRole()
        // (triggered by a Supabase token-refresh SessionStatus.Authenticated
        // re-emission) sees the new role immediately and does not overwrite it.
        _uiState.value = MainUiState.Authenticated(
            activeRole = newRole,
            userRole = current.userRole,
            unreadCount = newTabBadge,
            instructorPanelUnread = current.instructorPanelUnread,
            studentPanelUnread = current.studentPanelUnread,
        )
        Log.d("TMS_NAV", "switchRole: _uiState updated to $newRole synchronously, suppressFlag=${suppressGraphNavOnRoleChange}")
        viewModelScope.launch {
            rolePreferences.setLastActiveRole(userId, newRole)
            Log.d("TMS_NAV", "switchRole: DataStore persisted $newRole")
        }
    }

    fun refreshUnreadCount() {
        viewModelScope.launch {
            val current = _uiState.value as? MainUiState.Authenticated ?: return@launch
            // F-113 FR-113-008: fetch both panel counts in parallel; tab badge
            // = current panel; icon badge (computed property) = sum.
            val res = chatRepository.getUnreadCountForBothPanels()
            if (res is Resource.Success) {
                val (instructorCount, studentCount) = res.data
                val tabBadge = when (current.activeRole) {
                    ActiveRole.Instructor -> instructorCount
                    ActiveRole.Student -> studentCount
                }
                _uiState.value = current.copy(
                    unreadCount = tabBadge,
                    instructorPanelUnread = instructorCount,
                    studentPanelUnread = studentCount,
                )
                // TODO(F-113 FR-113-008): set system app-icon badge to
                // `iconBadge` once a launcher-badge mechanism is wired
                // (no ShortcutBadger / Notification Manager badge today).
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

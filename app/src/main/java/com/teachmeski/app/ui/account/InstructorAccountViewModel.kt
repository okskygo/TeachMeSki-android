package com.teachmeski.app.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.domain.model.InstructorProfile
import com.teachmeski.app.domain.model.TokenWallet
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.InstructorRepository
import com.teachmeski.app.domain.repository.WalletRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InstructorAccountUiState(
    val profile: InstructorProfile? = null,
    val wallet: TokenWallet? = null,
    val isLoading: Boolean = false,
    val isSigningOut: Boolean = false,
    val error: UiText? = null,
    val signOutSuccess: Boolean = false,
)

@HiltViewModel
class InstructorAccountViewModel @Inject constructor(
    private val instructorRepository: InstructorRepository,
    private val walletRepository: WalletRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InstructorAccountUiState())
    val uiState: StateFlow<InstructorAccountUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    signOutSuccess = false,
                )
            }
            coroutineScope {
                val profileDeferred = async { instructorRepository.getMyProfile() }
                val walletDeferred = async { walletRepository.getWallet() }
                val profileResult = profileDeferred.await()
                val walletResult = walletDeferred.await()

                val profile = (profileResult as? Resource.Success)?.data
                val wallet = (walletResult as? Resource.Success)?.data

                val error = when {
                    profileResult is Resource.Error -> profileResult.message
                    profileResult is Resource.Success && walletResult is Resource.Error ->
                        walletResult.message
                    else -> null
                }

                _uiState.update {
                    it.copy(
                        profile = profile,
                        wallet = wallet,
                        isLoading = false,
                        error = error,
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningOut = true, error = null) }
            when (val result = authRepository.signOut()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isSigningOut = false, signOutSuccess = true)
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSigningOut = false,
                            error = result.message,
                        )
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { s -> s.copy(isSigningOut = true) }
                }
            }
        }
    }

    fun consumeSignOutSuccess() {
        _uiState.update { it.copy(signOutSuccess = false) }
    }
}

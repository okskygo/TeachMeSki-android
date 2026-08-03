package com.teachmeski.app.ui.referral

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.ReferralInfo
import com.teachmeski.app.domain.repository.ReferralRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * F-116: UI state for `ReferralScreen`. Mirrors iOS `ReferralUiState`
 * field-for-field (see `teachmeski-ios/TeachMeSki/UI/Referral/ReferralViewModel.swift`).
 */
data class ReferralUiState(
    val info: ReferralInfo? = null,
    val isLoading: Boolean = false,
    val error: UiText? = null,
)

/**
 * Loads the signed-in instructor's referral code + live reward config for
 * `ReferralScreen`. Mirrors the single-load shape of `WalletViewModel.load()`
 * (one `Resource<T>` call, no snapshot-diffing needed since there's nothing
 * else to load on this screen).
 */
@HiltViewModel
class ReferralViewModel @Inject constructor(
    private val referralRepository: ReferralRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReferralUiState())
    val uiState: StateFlow<ReferralUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = referralRepository.getReferralInfo()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(info = result.data, isLoading = false, error = null)
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = UiText.StringResource(R.string.error_load_referral),
                        )
                    }
                }

                Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }
}

package com.teachmeski.app.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.domain.model.TokenTransaction
import com.teachmeski.app.domain.repository.WalletRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class CreditHistoryUiState(
    val transactions: List<TokenTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val totalCount: Int = 0,
    val hasMore: Boolean = false,
    val error: UiText? = null,
)

@HiltViewModel
class CreditHistoryViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreditHistoryUiState())
    val uiState: StateFlow<CreditHistoryUiState> = _uiState.asStateFlow()

    private val loadMutex = Mutex()

    init {
        loadPage(1)
    }

    fun loadPage(page: Int) {
        viewModelScope.launch {
            loadMutex.withLock {
                if (page == 1) {
                    _uiState.update {
                        it.copy(
                            isLoading = true,
                            isLoadingMore = false,
                            error = null,
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoadingMore = true,
                            error = null,
                        )
                    }
                }

                when (val result = walletRepository.getTransactions(page)) {
                    is Resource.Success -> {
                        val (list, total) = result.data
                        _uiState.update { s ->
                            val merged = if (page == 1) list else s.transactions + list
                            s.copy(
                                transactions = merged,
                                isLoading = false,
                                isLoadingMore = false,
                                currentPage = page,
                                totalCount = total,
                                hasMore = merged.size < total,
                                error = null,
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isLoadingMore = false,
                                error = result.message,
                            )
                        }
                    }

                    Resource.Loading -> {
                        _uiState.update {
                            it.copy(
                                isLoading = page == 1 && it.isLoading,
                                isLoadingMore = page > 1 && it.isLoadingMore,
                            )
                        }
                    }
                }
            }
        }
    }

    fun loadMore() {
        val s = _uiState.value
        if (s.isLoading || s.isLoadingMore || !s.hasMore) return
        loadPage(s.currentPage + 1)
    }

    fun consumeError() {
        _uiState.update { it.copy(error = null) }
    }
}

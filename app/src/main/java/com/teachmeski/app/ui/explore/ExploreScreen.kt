package com.teachmeski.app.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.EmptyState
import com.teachmeski.app.ui.theme.TmsColor
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = hiltViewModel(),
    onNavigateToChat: (String) -> Unit,
    onNavigateToWallet: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.refreshOnResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(listState, viewModel) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = info.totalItemsCount
            val nearEnd = total > 0 && last >= total - 2
            val s = viewModel.uiState.value
            nearEnd to (s.hasMore && !s.isLoadingMore && !s.isLoading)
        }
            .distinctUntilChanged()
            .collect { (nearEnd, canLoad) ->
                if (nearEnd && canLoad) {
                    viewModel.loadMore()
                }
            }
    }

    var wasRefreshing by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isRefreshing) {
        if (wasRefreshing && !uiState.isRefreshing) {
            listState.animateScrollToItem(0)
        }
        wasRefreshing = uiState.isRefreshing
    }

    LaunchedEffect(uiState.unlockSuccessChatRoomId) {
        val roomId = uiState.unlockSuccessChatRoomId ?: return@LaunchedEffect
        onNavigateToChat(roomId)
        viewModel.consumeUnlockSuccess()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TmsColor.Background),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.nav_explore),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = TmsColor.OnSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Surface(
                        color = TmsColor.PrimaryFixed,
                        shape = RoundedCornerShape(8.dp),
                        onClick = onNavigateToWallet,
                    ) {
                        Text(
                            text = stringResource(R.string.wallet_tokens_fmt, uiState.tokenBalance),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TmsColor.Primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }

                DisciplineFilterRow(
                    selected = uiState.disciplineFilter,
                    onSelect = viewModel::setDisciplineFilter,
                )

                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.pullToRefresh() },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    when {
                        uiState.isLoading && uiState.requests.isEmpty() -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = TmsColor.Primary)
                            }
                        }

                        uiState.requests.isEmpty() && uiState.error != null -> {
                            val err = uiState.error
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = err?.asString().orEmpty(),
                                    color = TmsColor.Error,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                TextButton(onClick = {
                                    viewModel.consumeError()
                                    viewModel.loadPage(1)
                                }) {
                                    Text(text = stringResource(R.string.common_retry))
                                }
                            }
                        }

                        uiState.requests.isEmpty() -> {
                            EmptyState(
                                title = stringResource(R.string.explore_empty_title),
                                description = stringResource(R.string.explore_empty_description),
                                modifier = Modifier.fillMaxSize(),
                            )
                        }

                        else -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                uiState.error?.let { err ->
                                    item(key = "error_banner") {
                                        Surface(
                                            color = TmsColor.ErrorContainer,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                            ) {
                                                Text(
                                                    text = err.asString(),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TmsColor.Error,
                                                    modifier = Modifier.weight(1f),
                                                )
                                                TextButton(onClick = {
                                                    viewModel.consumeError()
                                                    viewModel.loadPage(1)
                                                }) {
                                                    Text(stringResource(R.string.common_retry))
                                                }
                                            }
                                        }
                                    }
                                }
                                items(
                                    items = uiState.requests,
                                    key = { it.id },
                                ) { request ->
                                    ExploreRequestCard(
                                        request = request,
                                        onUnlockClick = { viewModel.openUnlockDialog(request) },
                                        onViewChatClick = { roomId -> onNavigateToChat(roomId) },
                                    )
                                }
                                if (uiState.isLoadingMore) {
                                    item(key = "loading_more") {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(32.dp),
                                                color = TmsColor.Primary,
                                                strokeWidth = 2.dp,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            uiState.unlockDialogRequest?.let { req ->
                UnlockDialog(
                    request = req,
                    tokenBalance = uiState.tokenBalance,
                    message = uiState.unlockMessage,
                    onMessageChange = viewModel::setUnlockMessage,
                    isUnlocking = uiState.isUnlocking,
                    error = uiState.unlockError,
                    onConfirm = { viewModel.confirmUnlock() },
                    onDismiss = { viewModel.closeUnlockDialog() },
                )
            }
        }
    }
}

@Composable
private fun DisciplineFilterRow(
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text(stringResource(R.string.explore_filter_all_disciplines)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = TmsColor.Primary,
                selectedLabelColor = TmsColor.OnPrimary,
            ),
        )
        FilterChip(
            selected = selected == "ski",
            onClick = { onSelect("ski") },
            label = { Text(stringResource(R.string.explore_filter_ski)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = TmsColor.Primary,
                selectedLabelColor = TmsColor.OnPrimary,
            ),
        )
        FilterChip(
            selected = selected == "snowboard",
            onClick = { onSelect("snowboard") },
            label = { Text(stringResource(R.string.explore_filter_snowboard)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = TmsColor.Primary,
                selectedLabelColor = TmsColor.OnPrimary,
            ),
        )
    }
}

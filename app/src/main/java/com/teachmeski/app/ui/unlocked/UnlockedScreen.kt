package com.teachmeski.app.ui.unlocked

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.EmptyState
import com.teachmeski.app.ui.theme.TmsColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnlockedScreen(
    viewModel: UnlockedViewModel = hiltViewModel(),
    onNavigateToChat: (String) -> Unit,
    onNavigateToExplore: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loadError = uiState.error

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Text(
                text = stringResource(R.string.nav_unlocked),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TmsColor.OnSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.load(isRefresh = true) },
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    uiState.isLoading && uiState.rooms.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(TmsColor.Background),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = TmsColor.Primary)
                    }
                }

                uiState.rooms.isEmpty() && loadError != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(TmsColor.Background)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = loadError.asString(),
                            color = TmsColor.Error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = {
                            viewModel.consumeError()
                            viewModel.load(isRefresh = false)
                        }) {
                            Text(text = stringResource(R.string.common_retry))
                        }
                    }
                }

                uiState.rooms.isEmpty() -> {
                    EmptyState(
                        title = stringResource(R.string.unlocked_empty_title),
                        description = stringResource(R.string.unlocked_empty_description),
                        modifier = Modifier
                            .fillMaxSize()
                            .background(TmsColor.Background),
                        action = {
                            Button(
                                onClick = onNavigateToExplore,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TmsColor.Primary,
                                    contentColor = TmsColor.OnPrimary,
                                ),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(text = stringResource(R.string.unlocked_empty_cta))
                            }
                        },
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(TmsColor.Background),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        loadError?.let { err ->
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
                                            viewModel.load(isRefresh = false)
                                        }) {
                                            Text(stringResource(R.string.common_retry))
                                        }
                                    }
                                }
                            }
                        }
                        items(
                            items = uiState.rooms,
                            key = { it.roomId },
                        ) { room ->
                            UnlockedStoryCard(
                                room = room,
                                onNavigateToChat = { onNavigateToChat(room.roomId) },
                            )
                        }
                    }
                }
                }
            }
        }
    }
}

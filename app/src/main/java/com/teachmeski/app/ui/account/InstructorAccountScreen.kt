package com.teachmeski.app.ui.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.UserAvatar
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun InstructorAccountScreen(
    viewModel: InstructorAccountViewModel = hiltViewModel(),
    onNavigateToWallet: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToContact: () -> Unit = {},
    onNavigateToLegal: () -> Unit = {},
    onSignedOut: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()

    LaunchedEffect(uiState.signOutSuccess) {
        if (uiState.signOutSuccess) {
            onSignedOut()
            viewModel.consumeSignOutSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TmsColor.Background)
            .verticalScroll(scroll)
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.instructor_account_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = TmsColor.OnSurface,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        when {
            uiState.isLoading && uiState.profile == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = TmsColor.Primary)
                }
            }

            uiState.profile == null && uiState.error != null -> {
                val err = uiState.error
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = err?.asString().orEmpty(),
                        color = TmsColor.Error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = { viewModel.load() }) {
                        Text(stringResource(R.string.common_retry))
                    }
                }
            }

            else -> {
                val profile = uiState.profile
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    UserAvatar(
                        displayName = profile?.displayName,
                        avatarUrl = profile?.avatarUrl,
                        size = 96.dp,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = profile?.displayName.orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TmsColor.OnSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = profile?.shortId?.let { stringResource(R.string.instructor_account_short_id_fmt, it) }
                            .orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TmsColor.OnSurfaceVariant,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val accepting = profile?.isAcceptingRequests == true
                Surface(
                    color = if (accepting) {
                        TmsColor.Success.copy(alpha = 0.15f)
                    } else {
                        TmsColor.SurfaceContainer
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text(
                        text = stringResource(
                            if (accepting) {
                                R.string.instructor_profile_accepting_label
                            } else {
                                R.string.instructor_profile_not_accepting_label
                            },
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (accepting) TmsColor.Success else TmsColor.OnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                val balance = uiState.wallet?.balance ?: 0
                Surface(
                    color = TmsColor.PrimaryFixed,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 2.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.wallet_balance_label),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = TmsColor.Primary,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.wallet_tokens_fmt, balance),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = TmsColor.OnSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.wallet_balance_tokens),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TmsColor.OnSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    InstructorAccountMenuRow(
                        title = stringResource(R.string.instructor_account_wallet_entry),
                        onClick = onNavigateToWallet,
                    )
                    InstructorAccountMenuRow(
                        title = stringResource(R.string.instructor_account_profile_entry),
                        onClick = onNavigateToProfile,
                    )
                    InstructorAccountMenuRow(
                        title = stringResource(R.string.contact_title),
                        onClick = onNavigateToContact,
                    )
                    InstructorAccountMenuRow(
                        title = stringResource(R.string.instructor_account_legal_entry),
                        onClick = onNavigateToLegal,
                    )
                }

                uiState.error?.let { err ->
                    if (uiState.profile != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = err.asString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = TmsColor.Warning,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    enabled = !uiState.isLoading && !uiState.isSigningOut,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TmsColor.Error),
                    border = BorderStroke(1.dp, TmsColor.Error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    if (uiState.isSigningOut) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = TmsColor.Error,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(stringResource(R.string.common_logout))
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.common_logout_confirm_title)) },
            text = { Text(stringResource(R.string.common_logout_confirm_desc)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.signOut()
                    },
                ) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }
}

@Composable
private fun InstructorAccountMenuRow(
    title: String,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(title, style = MaterialTheme.typography.bodyLarge)
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TmsColor.Outline,
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                TmsColor.SurfaceLowest,
                MaterialTheme.shapes.medium,
            ),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

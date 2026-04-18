package com.teachmeski.app.ui.account

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun InstructorAccountScreen(
    viewModel: InstructorAccountViewModel = hiltViewModel(),
    onSwitchToStudent: () -> Unit = {},
    onNavigateToWallet: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToContact: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
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
            text = stringResource(R.string.nav_instructor_account),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = TmsColor.OnSurface,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            InstructorAccountMenuRow(
                title = stringResource(R.string.instructor_account_switch_to_student),
                onClick = onSwitchToStudent,
            )
            InstructorAccountMenuRow(
                title = stringResource(R.string.instructor_account_wallet_entry),
                trailingText = stringResource(
                    R.string.wallet_tokens_fmt,
                    uiState.wallet?.balance ?: 0,
                ),
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
                title = stringResource(R.string.legal_terms_title),
                onClick = onNavigateToTerms,
            )
            InstructorAccountMenuRow(
                title = stringResource(R.string.legal_privacy_title),
                onClick = onNavigateToPrivacy,
            )
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
    trailingText: String? = null,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(title, style = MaterialTheme.typography.bodyLarge)
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (trailingText != null) {
                    Text(
                        text = trailingText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TmsColor.Primary,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TmsColor.Outline,
                )
            }
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

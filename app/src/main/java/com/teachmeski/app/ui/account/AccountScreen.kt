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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.TmsLoadingButton
import com.teachmeski.app.ui.component.TmsTextField
import com.teachmeski.app.ui.component.UserAvatar
import com.teachmeski.app.util.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(
    onContactClick: () -> Unit,
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            delay(2500)
            viewModel.consumeSaveSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.account_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                UserAvatar(
                    displayName = uiState.displayName.ifBlank { null },
                    avatarUrl = uiState.avatarUrl,
                    size = 96.dp,
                )
            }

            TmsTextField(
                value = uiState.displayName,
                onValueChange = viewModel::onDisplayNameChange,
                label = stringResource(R.string.account_display_name_label),
                placeholder = stringResource(R.string.account_display_name_placeholder),
            )
            TmsLoadingButton(
                text = stringResource(R.string.account_save_button),
                onClick = { viewModel.updateDisplayName(uiState.displayName) },
                isLoading = uiState.isSaving,
                enabled = !uiState.isLoading,
            )

            if (uiState.saveSuccess) {
                Text(
                    text = stringResource(R.string.account_save_success),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            uiState.error?.let { err ->
                Text(
                    text = err.asString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Text(
                text = stringResource(R.string.account_email_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = uiState.email,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerLow,
                        MaterialTheme.shapes.medium,
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            AccountMenuRow(
                title = stringResource(R.string.contact_title),
                onClick = onContactClick,
            )
            AccountMenuRow(
                title = stringResource(R.string.legal_terms_title),
                onClick = onTermsClick,
            )
            AccountMenuRow(
                title = stringResource(R.string.legal_privacy_title),
                onClick = onPrivacyClick,
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showLogoutDialog = true },
                enabled = !uiState.isLoading,
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Text(stringResource(R.string.common_logout))
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
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
                        scope.launch {
                            when (viewModel.signOut()) {
                                is Resource.Success -> onSignedOut()
                                is Resource.Error,
                                is Resource.Loading,
                                -> Unit
                            }
                        }
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
private fun AccountMenuRow(
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
                tint = MaterialTheme.colorScheme.outline,
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                MaterialTheme.colorScheme.surfaceContainerLowest,
                MaterialTheme.shapes.medium,
            ),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

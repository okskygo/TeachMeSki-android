package com.teachmeski.app.ui.account

import androidx.compose.foundation.background
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.TmsLoadingButton
import com.teachmeski.app.ui.component.TmsTextField
import com.teachmeski.app.ui.component.TmsTopBar
import com.teachmeski.app.ui.component.UserAvatar
import com.teachmeski.app.ui.theme.TmsColor
import kotlinx.coroutines.delay

@Composable
fun AccountSettingsScreen(
    onBack: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            delay(2500)
            viewModel.consumeSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TmsTopBar(
                title = stringResource(R.string.account_title),
                onBack = onBack,
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scroll)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                UserAvatar(
                    displayName = uiState.displayName.ifBlank { null },
                    avatarUrl = uiState.avatarUrl,
                    size = 96.dp,
                )

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
                        color = TmsColor.Primary,
                    )
                }

                uiState.error?.let { err ->
                    Text(
                        text = err.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = TmsColor.Error,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.account_email_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = TmsColor.OnSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = uiState.email,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TmsColor.OnSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            TmsColor.SurfaceLow,
                            MaterialTheme.shapes.medium,
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(TmsColor.Surface.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = TmsColor.Primary)
                }
            }
        }
    }
}

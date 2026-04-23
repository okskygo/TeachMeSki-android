package com.teachmeski.app.ui.account

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.TmsLoadingButton
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
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.uploadAvatar(context, uri)
        }
    }

    val onPickAvatar: () -> Unit = {
        imagePicker.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
        )
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            delay(2500)
            viewModel.consumeSaveSuccess()
        }
    }

    val nameLength = uiState.displayName.length
    val isNameTooLong = nameLength > MAX_DISPLAY_NAME_LENGTH

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
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            TmsColor.SurfaceLowest,
                            RoundedCornerShape(16.dp),
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .border(2.dp, TmsColor.OutlineVariant, CircleShape)
                                .clickable(onClick = onPickAvatar),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (uiState.isUploadingAvatar) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = TmsColor.Primary,
                                    strokeWidth = 3.dp,
                                )
                            } else {
                                UserAvatar(
                                    displayName = uiState.displayName.ifBlank { null },
                                    avatarUrl = uiState.avatarUrl,
                                    size = 96.dp,
                                )
                            }
                        }

                        Text(
                            text = stringResource(R.string.account_avatar_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = TmsColor.OnSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.account_display_name_label),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = TmsColor.OnSurface,
                            )
                            OutlinedTextField(
                                value = uiState.displayName,
                                onValueChange = viewModel::onDisplayNameChange,
                                placeholder = {
                                    Text(
                                        stringResource(R.string.account_display_name_placeholder),
                                        color = TmsColor.Outline.copy(alpha = 0.5f),
                                    )
                                },
                                isError = isNameTooLong,
                                singleLine = true,
                                shape = MaterialTheme.shapes.small,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = TmsColor.SurfaceLow,
                                    focusedContainerColor = TmsColor.SurfaceLow,
                                ),
                                suffix = {
                                    Text(
                                        text = "$nameLength/$MAX_DISPLAY_NAME_LENGTH",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isNameTooLong) TmsColor.Error else TmsColor.Outline,
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            if (isNameTooLong) {
                                Text(
                                    text = stringResource(R.string.account_display_name_max_error),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TmsColor.Error,
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.account_email_label),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium,
                                color = TmsColor.OnSurface,
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        TmsColor.SurfaceLow,
                                        MaterialTheme.shapes.small,
                                    )
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = uiState.email,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TmsColor.OnSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        if (uiState.saveSuccess) {
                            Text(
                                text = stringResource(R.string.account_save_success),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TmsColor.Success,
                            )
                        }

                        uiState.error?.let { err ->
                            Text(
                                text = err.asString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = TmsColor.Error,
                            )
                        }

                        TmsLoadingButton(
                            text = stringResource(R.string.account_save_button),
                            onClick = { viewModel.updateDisplayName(uiState.displayName) },
                            isLoading = uiState.isSaving,
                            enabled = !uiState.isLoading && !isNameTooLong && uiState.displayName.isNotBlank(),
                        )
                    }
                }
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

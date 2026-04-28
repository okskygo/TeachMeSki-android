package com.teachmeski.app.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.TmsTopBar
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun InstructorAccountSettingsScreen(
    onBack: () -> Unit,
    viewModel: InstructorAccountSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = TmsColor.Background,
        topBar = {
            TmsTopBar(
                title = stringResource(R.string.instructor_account_settings_entry),
                onBack = onBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TmsColor.SurfaceLowest, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                ReadonlyField(
                    label = stringResource(R.string.account_email_label),
                    value = uiState.email,
                )
                ReadonlyField(
                    label = stringResource(R.string.instructor_account_user_id_label),
                    value = uiState.userId,
                    isMono = true,
                )
            }

            if (!uiState.isLoading) {
                IdentityVerificationSection(
                    snackbarHostState = snackbarHostState,
                )
            }
        }
    }
}

@Composable
private fun ReadonlyField(
    label: String,
    value: String,
    isMono: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = TmsColor.OnSurface,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TmsColor.SurfaceLow, MaterialTheme.shapes.small)
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                text = value,
                style = if (isMono) {
                    MaterialTheme.typography.bodySmall
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                color = TmsColor.OnSurface.copy(alpha = 0.6f),
            )
        }
    }
}

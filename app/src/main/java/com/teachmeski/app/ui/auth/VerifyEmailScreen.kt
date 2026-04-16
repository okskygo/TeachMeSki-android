package com.teachmeski.app.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.OtpTextField
import com.teachmeski.app.ui.component.TmsLoadingButton

@Composable
fun VerifyEmailScreen(
    onBack: () -> Unit = {},
    viewModel: VerifyEmailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(80.dp))

            Text(
                text = stringResource(R.string.auth_verify_email_title),
                style = MaterialTheme.typography.headlineMedium,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.auth_verify_email_subtitle_fmt, state.email),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(32.dp))

            state.error?.let { error ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = error.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(12.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            OtpTextField(
                value = state.otp,
                onValueChange = viewModel::onOtpChange,
                isError = state.error != null,
            )

            Spacer(Modifier.height(24.dp))

            TmsLoadingButton(
                text = stringResource(R.string.auth_verify_email_verify),
                onClick = viewModel::verifyOtp,
                isLoading = state.isVerifying,
                enabled = state.otp.length == 6,
            )

            Spacer(Modifier.height(16.dp))

            state.resendMessage?.let {
                Text(
                    text = it.asString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(8.dp))
            }

            TextButton(
                onClick = viewModel::resendOtp,
                enabled = state.resendCooldown == 0 && !state.isResending,
            ) {
                val resendText = if (state.resendCooldown > 0) {
                    stringResource(R.string.auth_verify_email_resend_cooldown_fmt, state.resendCooldown)
                } else {
                    stringResource(R.string.auth_verify_email_resend)
                }
                Text(resendText)
            }

            Spacer(Modifier.weight(1f))

            TextButton(onClick = onBack) {
                Text(
                    text = stringResource(R.string.auth_verify_email_back),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

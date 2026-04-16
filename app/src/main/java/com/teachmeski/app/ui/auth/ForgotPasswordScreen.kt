package com.teachmeski.app.ui.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.TmsLoadingButton
import com.teachmeski.app.ui.component.TmsTextField

@Composable
fun ForgotPasswordScreen(
    onNavigateToLogin: () -> Unit = {},
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current

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
                text = stringResource(R.string.auth_forgot_password_title),
                style = MaterialTheme.typography.headlineMedium,
            )

            Spacer(Modifier.height(12.dp))

            if (state.success) {
                Text(
                    text = stringResource(R.string.auth_forgot_password_success),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )

                Spacer(Modifier.height(32.dp))

                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = stringResource(R.string.auth_forgot_password_back_to_login),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.auth_forgot_password_subtitle),
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

                TmsTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    label = stringResource(R.string.auth_forgot_password_email_label),
                    placeholder = stringResource(R.string.auth_forgot_password_email_placeholder),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done,
                    ),
                    enabled = !state.isLoading,
                )

                Spacer(Modifier.height(24.dp))

                TmsLoadingButton(
                    text = stringResource(R.string.auth_forgot_password_submit),
                    onClick = {
                        keyboard?.hide()
                        viewModel.sendResetEmail()
                    },
                    isLoading = state.isLoading,
                )

                Spacer(Modifier.height(24.dp))

                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = stringResource(R.string.auth_forgot_password_back_to_login),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

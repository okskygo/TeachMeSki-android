package com.teachmeski.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.PasswordRulesDisplay
import com.teachmeski.app.ui.component.TmsLoadingButton
import com.teachmeski.app.ui.component.TmsTextField

@Composable
fun SignupScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToVerifyEmail: (email: String) -> Unit = {},
    viewModel: SignupViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(state.signupSuccess) {
        if (state.signupSuccess) {
            onNavigateToVerifyEmail(state.email.trim())
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.auth_signup_title),
                style = MaterialTheme.typography.headlineMedium,
            )

            Spacer(Modifier.height(24.dp))

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
                value = state.displayName,
                onValueChange = viewModel::onDisplayNameChange,
                label = stringResource(R.string.auth_signup_display_name_label),
                placeholder = stringResource(R.string.auth_signup_display_name_placeholder),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                enabled = !state.isLoading,
            )
            Spacer(Modifier.height(12.dp))

            TmsTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = stringResource(R.string.auth_signup_email_label),
                placeholder = stringResource(R.string.auth_signup_email_placeholder),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                enabled = !state.isLoading,
            )
            Spacer(Modifier.height(12.dp))

            TmsTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = stringResource(R.string.auth_signup_password_label),
                placeholder = stringResource(R.string.auth_signup_password_placeholder),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                ),
                enabled = !state.isLoading,
            )
            if (state.password.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                PasswordRulesDisplay(
                    rules = state.passwordRules,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp),
                )
            }
            Spacer(Modifier.height(12.dp))

            TmsTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = stringResource(R.string.auth_signup_confirm_password_label),
                placeholder = stringResource(R.string.auth_signup_confirm_password_placeholder),
                visualTransformation = PasswordVisualTransformation(),
                isError = state.confirmPassword.isNotEmpty() && !state.confirmMatch,
                errorMessage = if (state.confirmPassword.isNotEmpty() && !state.confirmMatch) {
                    stringResource(R.string.auth_error_password_confirm_mismatch)
                } else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                enabled = !state.isLoading,
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Checkbox(
                    checked = state.termsChecked,
                    onCheckedChange = viewModel::onTermsCheckedChange,
                    enabled = !state.isLoading,
                )
                val termsText = buildAnnotatedString {
                    append(stringResource(R.string.auth_signup_terms_prefix))
                    append(" ")
                    pushStringAnnotation("terms", "https://teachmeski.com/terms")
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ) {
                        append(stringResource(R.string.auth_signup_terms_link))
                    }
                    pop()
                    append(" ")
                    append(stringResource(R.string.auth_signup_terms_and))
                    append(" ")
                    pushStringAnnotation("privacy", "https://teachmeski.com/privacy")
                    withStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ) {
                        append(stringResource(R.string.auth_signup_privacy_link))
                    }
                    pop()
                }
                Text(
                    text = termsText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            Spacer(Modifier.height(16.dp))

            TmsLoadingButton(
                text = stringResource(R.string.auth_signup_submit),
                onClick = {
                    keyboard?.hide()
                    viewModel.signUp()
                },
                isLoading = state.isLoading,
                enabled = state.canSubmit,
            )

            Spacer(Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.auth_signup_already_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        text = stringResource(R.string.auth_signup_login_link),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

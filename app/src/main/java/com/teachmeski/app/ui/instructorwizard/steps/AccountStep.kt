package com.teachmeski.app.ui.instructorwizard.steps

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.PasswordRulesDisplay
import com.teachmeski.app.ui.component.TmsTextField
import com.teachmeski.app.ui.instructorwizard.InstructorWizardUiState
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun AccountStep(
    state: InstructorWizardUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTermsCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val confirmMismatch = state.confirmPassword.isNotEmpty() && state.password != state.confirmPassword

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.instructor_wizard_account_title),
            style = MaterialTheme.typography.headlineSmall,
            color = TmsColor.OnSurface,
        )
        Text(
            text = stringResource(R.string.instructor_wizard_account_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = TmsColor.OnSurfaceVariant,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = TmsColor.SurfaceLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TmsTextField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    label = stringResource(R.string.instructor_wizard_account_email_label),
                    placeholder = stringResource(R.string.instructor_wizard_account_email_placeholder),
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                        ),
                    enabled = !state.isSigningUp,
                )

                TmsTextField(
                    value = state.password,
                    onValueChange = onPasswordChange,
                    label = stringResource(R.string.instructor_wizard_account_password_label),
                    placeholder = stringResource(R.string.instructor_wizard_account_password_placeholder),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next,
                        ),
                    enabled = !state.isSigningUp,
                )

                if (state.password.isNotEmpty()) {
                    PasswordRulesDisplay(
                        rules = state.passwordRules,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                TmsTextField(
                    value = state.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    label = stringResource(R.string.instructor_wizard_account_confirm_label),
                    placeholder = stringResource(R.string.instructor_wizard_account_confirm_placeholder),
                    visualTransformation = PasswordVisualTransformation(),
                    isError = confirmMismatch,
                    errorMessage =
                        if (confirmMismatch) {
                            stringResource(R.string.auth_error_password_confirm_mismatch)
                        } else {
                            null
                        },
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                    enabled = !state.isSigningUp,
                )
            }
        }

        TermsCheckbox(
            checked = state.termsChecked,
            onCheckedChange = onTermsCheckedChange,
            enabled = !state.isSigningUp,
        )
    }
}

@Composable
private fun TermsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        )
        val linkColor = TmsColor.Primary
        val textColor = TmsColor.OnSurfaceVariant
        val textStyle = MaterialTheme.typography.bodySmall
        val termsText =
            buildAnnotatedString {
                append(stringResource(R.string.instructor_wizard_account_terms_prefix))
                append(" ")
                pushStringAnnotation("url", "https://teachmeski.com/terms")
                withStyle(
                    SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline,
                    ),
                ) {
                    append(stringResource(R.string.instructor_wizard_account_terms_link))
                }
                pop()
                append(" ")
                append(stringResource(R.string.instructor_wizard_account_terms_and))
                append(" ")
                pushStringAnnotation("url", "https://teachmeski.com/privacy")
                withStyle(
                    SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline,
                    ),
                ) {
                    append(stringResource(R.string.instructor_wizard_account_privacy_link))
                }
                pop()
            }
        val context = LocalContext.current
        @Suppress("DEPRECATION")
        ClickableText(
            text = termsText,
            style = textStyle.copy(color = textColor),
            modifier = Modifier.padding(top = 12.dp),
            onClick = { offset ->
                termsText
                    .getStringAnnotations("url", offset, offset)
                    .firstOrNull()
                    ?.let { annotation ->
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item)),
                        )
                    }
            },
        )
    }
}

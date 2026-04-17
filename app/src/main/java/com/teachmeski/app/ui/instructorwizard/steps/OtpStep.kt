package com.teachmeski.app.ui.instructorwizard.steps

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.OtpTextField
import com.teachmeski.app.ui.component.TmsLoadingButton
import com.teachmeski.app.ui.instructorwizard.InstructorWizardUiState
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun OtpStep(
    state: InstructorWizardUiState,
    onOtpChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.instructor_wizard_otp_title),
            style = MaterialTheme.typography.headlineSmall,
            color = TmsColor.OnSurface,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.instructor_wizard_otp_subtitle_fmt, state.email.trim()),
            style = MaterialTheme.typography.bodyMedium,
            color = TmsColor.OnSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        state.otpError?.let { error ->
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
            Spacer(modifier = Modifier.height(16.dp))
        }

        OtpTextField(
            value = state.otp,
            onValueChange = onOtpChange,
            isError = state.otpError != null,
        )

        Spacer(modifier = Modifier.height(24.dp))

        TmsLoadingButton(
            text = stringResource(R.string.auth_verify_email_verify),
            onClick = onVerify,
            isLoading = state.isVerifyingOtp,
            enabled = state.otp.length == 6,
        )

        Spacer(modifier = Modifier.height(16.dp))

        state.resendMessage?.let {
            Text(
                text = it.asString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        TextButton(
            onClick = onResend,
            enabled = state.resendCooldown == 0 && !state.isResendingOtp,
        ) {
            val resendText =
                if (state.resendCooldown > 0) {
                    stringResource(R.string.auth_verify_email_resend_cooldown_fmt, state.resendCooldown)
                } else {
                    stringResource(R.string.auth_verify_email_resend)
                }
            Text(resendText)
        }
    }
}

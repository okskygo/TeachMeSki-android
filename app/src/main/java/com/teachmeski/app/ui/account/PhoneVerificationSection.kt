package com.teachmeski.app.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.PhoneVerificationBadge
import com.teachmeski.app.ui.component.TmsLoadingButton
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun PhoneVerificationSection(
    viewModel: PhoneVerificationViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TmsColor.SurfaceLowest, RoundedCornerShape(16.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.phone_section_title),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = TmsColor.OnSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.phone_section_desc),
            style = MaterialTheme.typography.bodySmall,
            color = TmsColor.Outline,
        )

        when (val phase = uiState.phase) {
            is PhoneVerificationPhase.Verified -> VerifiedContent(
                phone = viewModel.maskedPhone(phase.phone),
            )
            is PhoneVerificationPhase.Success -> SuccessContent(
                phone = viewModel.maskedPhone(phase.phone),
            )
            is PhoneVerificationPhase.Idle -> IdleContent(
                phoneInput = uiState.phoneInput,
                onPhoneChange = viewModel::onPhoneInputChange,
                onSend = viewModel::sendOtp,
                isSending = uiState.isSending,
                cooldownSeconds = uiState.cooldownSeconds,
                error = uiState.error?.let { mapErrorToString(it) },
            )
            is PhoneVerificationPhase.OtpInput -> OtpInputContent(
                phone = viewModel.maskedPhone(phase.phone),
                onOtpComplete = viewModel::onOtpComplete,
                onResend = viewModel::resendOtp,
                isSending = uiState.isSending,
                isVerifying = uiState.isVerifying,
                cooldownSeconds = uiState.cooldownSeconds,
                error = uiState.error?.let { mapErrorToString(it) },
            )
        }
    }
}

@Composable
private fun VerifiedContent(phone: String) {
    PhoneVerificationBadge(
        verified = true,
        verifiedLabel = stringResource(R.string.phone_verified_label),
        unverifiedLabel = stringResource(R.string.phone_unverified_label),
    )
    Text(
        text = phone,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = TmsColor.OnSurface,
    )
    Text(
        text = stringResource(R.string.phone_verified_locked),
        style = MaterialTheme.typography.bodySmall,
        color = TmsColor.Outline,
    )
}

@Composable
private fun SuccessContent(phone: String) {
    PhoneVerificationBadge(
        verified = true,
        verifiedLabel = stringResource(R.string.phone_verified_label),
        unverifiedLabel = stringResource(R.string.phone_unverified_label),
    )
    Text(
        text = phone,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = TmsColor.OnSurface,
    )
    Text(
        text = stringResource(R.string.phone_success),
        style = MaterialTheme.typography.bodySmall,
        color = TmsColor.Success,
    )
}

@Composable
private fun IdleContent(
    phoneInput: String,
    onPhoneChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
    cooldownSeconds: Int,
    error: String?,
) {
    PhoneVerificationBadge(
        verified = false,
        verifiedLabel = stringResource(R.string.phone_verified_label),
        unverifiedLabel = stringResource(R.string.phone_unverified_label),
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.phone_input_label),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = TmsColor.OnSurface,
        )
        OutlinedTextField(
            value = phoneInput,
            onValueChange = onPhoneChange,
            placeholder = {
                Text(
                    stringResource(R.string.phone_input_placeholder),
                    color = TmsColor.Outline.copy(alpha = 0.5f),
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = MaterialTheme.shapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = TmsColor.SurfaceLow,
                focusedContainerColor = TmsColor.SurfaceLow,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(R.string.phone_input_hint),
            style = MaterialTheme.typography.bodySmall,
            color = TmsColor.Outline,
        )
    }

    if (error != null) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodySmall,
            color = TmsColor.Error,
        )
    }

    val buttonText = when {
        cooldownSeconds > 0 -> stringResource(R.string.phone_resend_cooldown_fmt, cooldownSeconds)
        isSending -> stringResource(R.string.phone_sending)
        else -> stringResource(R.string.phone_send_button)
    }

    TmsLoadingButton(
        text = buttonText,
        onClick = onSend,
        isLoading = isSending,
        enabled = phoneInput.isNotBlank() && !isSending && cooldownSeconds <= 0,
    )
}

@Composable
private fun OtpInputContent(
    phone: String,
    onOtpComplete: (String) -> Unit,
    onResend: () -> Unit,
    isSending: Boolean,
    isVerifying: Boolean,
    cooldownSeconds: Int,
    error: String?,
) {
    PhoneVerificationBadge(
        verified = false,
        verifiedLabel = stringResource(R.string.phone_verified_label),
        unverifiedLabel = stringResource(R.string.phone_unverified_label),
    )

    Text(
        text = stringResource(R.string.phone_otp_title),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = TmsColor.OnSurface,
    )
    Text(
        text = stringResource(R.string.phone_otp_subtitle_fmt, phone),
        style = MaterialTheme.typography.bodySmall,
        color = TmsColor.Outline,
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        OtpFields(
            onComplete = onOtpComplete,
            enabled = !isVerifying,
        )
        if (isVerifying) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(TmsColor.SurfaceLowest.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = TmsColor.Primary,
                    strokeWidth = 3.dp,
                )
            }
        }
    }

    if (error != null) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodySmall,
            color = TmsColor.Error,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    val resendText = if (cooldownSeconds > 0) {
        stringResource(R.string.phone_resend_cooldown_fmt, cooldownSeconds)
    } else {
        stringResource(R.string.phone_resend)
    }

    TextButton(
        onClick = onResend,
        enabled = !isSending && cooldownSeconds <= 0,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = resendText,
            color = if (cooldownSeconds > 0 || isSending) {
                TmsColor.Outline.copy(alpha = 0.4f)
            } else {
                TmsColor.Primary
            },
        )
    }
}

@Composable
private fun OtpFields(
    onComplete: (String) -> Unit,
    enabled: Boolean,
    digitCount: Int = 6,
) {
    val digits = remember { List(digitCount) { mutableStateOf("") } }
    val focusRequesters = remember { List(digitCount) { FocusRequester() } }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 0 until digitCount) {
            OutlinedTextField(
                value = digits[i].value,
                onValueChange = { value ->
                    val filtered = value.filter { it.isDigit() }.take(1)
                    digits[i].value = filtered
                    if (filtered.isNotEmpty() && i < digitCount - 1) {
                        focusRequesters[i + 1].requestFocus()
                    }
                    if (digits.all { it.value.isNotEmpty() }) {
                        onComplete(digits.joinToString("") { it.value })
                    }
                },
                enabled = enabled,
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = MaterialTheme.shapes.small,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = TmsColor.SurfaceLow,
                    focusedContainerColor = TmsColor.SurfaceLow,
                ),
                modifier = Modifier
                    .width(44.dp)
                    .height(56.dp)
                    .focusRequester(focusRequesters[i]),
            )
        }
    }
}

@Composable
private fun mapErrorToString(errorType: String): String = when (errorType) {
    "invalid_format" -> stringResource(R.string.phone_error_invalid_format)
    "unsupported_country" -> stringResource(R.string.phone_error_unsupported_country)
    "phone_taken" -> stringResource(R.string.phone_error_phone_taken)
    "otp" -> stringResource(R.string.phone_error_otp)
    "not_verified" -> stringResource(R.string.phone_error_unknown)
    else -> stringResource(R.string.phone_error_unknown)
}

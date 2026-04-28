package com.teachmeski.app.ui.account

import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.IdentityVerifiedBadge
import com.teachmeski.app.ui.theme.TmsColor

/**
 * F-108 instructor "identity verification" card on the account
 * settings screen. Mirrors the web `IdentityVerificationSection`:
 *
 * - Verified state: title + green badge + "已綁定 LINE（聯絡客服可變更）"
 *   (with success-tinted check icon). No unbind / re-bind button.
 * - Unverified state: title + grey dashed badge + description + LINE
 *   green CTA "使用 LINE 綁定".
 *
 * Tapping the CTA sets `state.authorizeUrl`; this composable launches
 * the URL in a Custom Tab and clears the state. The OAuth round-trip
 * comes back via `LineCallbackActivity` → `MainActivity` → bus →
 * ViewModel, which surfaces a `state.toast` we render through a
 * `SnackbarHost`.
 */
@Composable
fun IdentityVerificationSection(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: IdentityVerificationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.authorizeUrl) {
        val url = state.authorizeUrl ?: return@LaunchedEffect
        CustomTabsIntent.Builder().build().launchUrl(context, url.toUri())
        viewModel.authorizeUrlConsumed()
    }

    val toast = state.toast
    val toastMessage = toast?.asString()
    LaunchedEffect(toast) {
        if (toast != null && toastMessage != null) {
            snackbarHostState.showSnackbar(toastMessage)
            viewModel.toastConsumed()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TmsColor.SurfaceLowest, RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.identity_verification_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = TmsColor.OnSurface,
                modifier = Modifier.weight(1f),
            )
            IdentityVerifiedBadge(
                verified = state.isVerified,
                verifiedLabel = stringResource(R.string.identity_verified_label),
                unverifiedLabel = stringResource(R.string.identity_unverified_label),
            )
        }

        if (state.isVerified) {
            VerifiedBody()
        } else if (!state.isLoading) {
            UnverifiedBody(onBindClick = viewModel::onBindClick)
        }
    }
}

@Composable
private fun VerifiedBody() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = TmsColor.Success,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = stringResource(R.string.identity_bound_locked_text),
            style = MaterialTheme.typography.bodyMedium,
            color = TmsColor.OnSurfaceVariant,
        )
    }
}

@Composable
private fun UnverifiedBody(onBindClick: () -> Unit) {
    Text(
        text = stringResource(R.string.identity_verification_description),
        style = MaterialTheme.typography.bodyMedium,
        color = TmsColor.OnSurfaceVariant,
    )
    Button(
        onClick = onBindClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TmsColor.LineGreen,
            contentColor = Color.White,
        ),
    ) {
        Text(
            text = stringResource(R.string.identity_bind_line_button),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

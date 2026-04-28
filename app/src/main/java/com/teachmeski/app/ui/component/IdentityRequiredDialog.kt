package com.teachmeski.app.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.teachmeski.app.R

/**
 * F-108 modal shown when an instructor tries to unlock (Path-A from
 * Explore or Path-B from Chat) without having completed LINE
 * identity verification. Tapping "立即綁定" should navigate to the
 * instructor account settings screen so the user lands on
 * `IdentityVerificationSection`.
 */
@Composable
fun IdentityRequiredDialog(
    onDismiss: () -> Unit,
    onBindNow: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.identity_bind_required_modal_title)) },
        text = { Text(stringResource(R.string.identity_bind_required_modal_body)) },
        confirmButton = {
            TextButton(onClick = onBindNow) {
                Text(stringResource(R.string.identity_bind_required_modal_cta))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.identity_bind_required_modal_cancel))
            }
        },
    )
}

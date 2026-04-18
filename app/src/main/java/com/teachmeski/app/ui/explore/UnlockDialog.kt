package com.teachmeski.app.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.ExploreLessonRequest
import com.teachmeski.app.ui.theme.TmsColor
import com.teachmeski.app.util.UiText

private const val MAX_LEN = 500

@Composable
fun UnlockDialog(
    request: ExploreLessonRequest,
    tokenBalance: Int,
    message: String,
    onMessageChange: (String) -> Unit,
    isUnlocking: Boolean,
    error: UiText?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val insufficient = tokenBalance < request.baseTokenCost
    val confirmDisabled =
        message.trim().isEmpty() || isUnlocking || insufficient || message.length > MAX_LEN

    Dialog(
        onDismissRequest = { if (!isUnlocking) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = TmsColor.SurfaceLowest,
            shadowElevation = 24.dp,
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                Header(isUnlocking = isUnlocking, onDismiss = onDismiss)

                CostSection(cost = request.baseTokenCost, balance = tokenBalance)

                if (insufficient) {
                    Spacer(modifier = Modifier.height(8.dp))
                    InsufficientWarning()
                }

                Spacer(modifier = Modifier.height(16.dp))
                MessageSection(
                    message = message,
                    onMessageChange = onMessageChange,
                    isUnlocking = isUnlocking,
                )

                if (error != null) {
                    Text(
                        text = error.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = TmsColor.Error,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                FooterButtons(
                    cost = request.baseTokenCost,
                    isUnlocking = isUnlocking,
                    confirmDisabled = confirmDisabled,
                    onConfirm = onConfirm,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun Header(isUnlocking: Boolean, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 12.dp, top = 20.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.explore_unlock_dialog_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = TmsColor.OnSurface,
        )
        IconButton(onClick = { if (!isUnlocking) onDismiss() }) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.explore_unlock_dialog_cancel),
                tint = TmsColor.OnSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CostSection(cost: Int, balance: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(TmsColor.Primary.copy(alpha = 0.05f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TmsColor.PrimaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = TmsColor.OnPrimary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column {
                Text(
                    text = stringResource(R.string.explore_unlock_dialog_cost_section_label),
                    style = MaterialTheme.typography.bodySmall,
                    color = TmsColor.OnSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.explore_unlock_dialog_cost_label, cost),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TmsColor.Primary,
                )
            }
        }

        HorizontalDivider(color = TmsColor.Primary.copy(alpha = 0.1f))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.explore_unlock_dialog_balance_label),
                style = MaterialTheme.typography.bodySmall,
                color = TmsColor.OnSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.explore_unlock_dialog_balance_value, balance),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TmsColor.OnSurface,
            )
        }
    }
}

@Composable
private fun InsufficientWarning() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, TmsColor.Warning.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .background(TmsColor.Warning.copy(alpha = 0.1f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Warning,
            contentDescription = null,
            tint = TmsColor.Warning,
            modifier = Modifier.size(20.dp),
        )
        Column {
            Text(
                text = stringResource(R.string.explore_unlock_dialog_insufficient_title),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = TmsColor.Warning,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.explore_unlock_dialog_insufficient_message),
                style = MaterialTheme.typography.bodySmall,
                color = TmsColor.OnSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MessageSection(
    message: String,
    onMessageChange: (String) -> Unit,
    isUnlocking: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = stringResource(R.string.explore_unlock_dialog_message_label),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TmsColor.OnSurface,
            )
            Text(
                text = stringResource(R.string.explore_unlock_dialog_char_count, message.length, MAX_LEN),
                style = MaterialTheme.typography.labelSmall,
                color = TmsColor.Outline,
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = message,
            onValueChange = { if (it.length <= MAX_LEN) onMessageChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            placeholder = {
                Text(
                    text = stringResource(R.string.explore_unlock_dialog_hint),
                    style = MaterialTheme.typography.bodySmall,
                )
            },
            enabled = !isUnlocking,
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodySmall,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = TmsColor.SurfaceLow,
                focusedContainerColor = TmsColor.SurfaceLowest,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = TmsColor.Primary,
            ),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.explore_unlock_dialog_message_tip),
            style = MaterialTheme.typography.labelSmall,
            fontStyle = FontStyle.Italic,
            color = TmsColor.Outline,
        )
    }
}

@Composable
private fun FooterButtons(
    cost: Int,
    isUnlocking: Boolean,
    confirmDisabled: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = onDismiss,
            enabled = !isUnlocking,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = TmsColor.SurfaceHighest,
                contentColor = TmsColor.OnSurface,
            ),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Text(
                text = stringResource(R.string.explore_unlock_dialog_cancel),
                fontWeight = FontWeight.Bold,
            )
        }

        Button(
            onClick = onConfirm,
            enabled = !confirmDisabled,
            modifier = Modifier.weight(2f),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = TmsColor.Primary,
                contentColor = TmsColor.OnPrimary,
                disabledContainerColor = TmsColor.Primary.copy(alpha = 0.5f),
                disabledContentColor = TmsColor.OnPrimary.copy(alpha = 0.5f),
            ),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            if (isUnlocking) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = TmsColor.OnPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.LockOpen,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    text = stringResource(R.string.explore_unlock_dialog_confirm, cost),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

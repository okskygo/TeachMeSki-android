package com.teachmeski.app.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.UnlockInfo
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun ChatUnlockBar(
    unlockInfo: UnlockInfo,
    isUnlocking: Boolean,
    onUnlockClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val insufficient = unlockInfo.balance < unlockInfo.cost
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = TmsColor.SurfaceLowest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = TmsColor.Primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.chat_unlock_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = TmsColor.OnSurface,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Text(
                text = stringResource(R.string.chat_unlock_description),
                style = MaterialTheme.typography.bodySmall,
                color = TmsColor.OnSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.chat_unlock_cost_fmt, unlockInfo.cost),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TmsColor.OnSurface,
                )
                Text(
                    text = stringResource(R.string.chat_unlock_balance_fmt, unlockInfo.balance),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (insufficient) TmsColor.Error else TmsColor.OnSurfaceVariant,
                )
            }
            if (insufficient) {
                Text(
                    text = stringResource(R.string.chat_unlock_insufficient),
                    style = MaterialTheme.typography.bodySmall,
                    color = TmsColor.Error,
                )
            }
            Button(
                onClick = onUnlockClick,
                enabled = !insufficient && !isUnlocking,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.chat_unlock_btn))
            }
        }
    }
}

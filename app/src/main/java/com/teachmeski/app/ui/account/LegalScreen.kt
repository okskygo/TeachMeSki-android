package com.teachmeski.app.ui.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.TmsTopBar

private const val TYPE_TERMS = "terms"
private const val TYPE_PRIVACY = "privacy"

@Composable
fun LegalScreen(
    type: String,
    onBack: () -> Unit,
) {
    val titleRes = when (type) {
        TYPE_TERMS -> R.string.legal_terms_title
        TYPE_PRIVACY -> R.string.legal_privacy_title
        else -> R.string.legal_privacy_title
    }
    val scroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        TmsTopBar(
            title = stringResource(titleRes),
            onBack = onBack,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.legal_website_placeholder),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.legal_back_home))
            }
        }
    }
}

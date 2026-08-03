package com.teachmeski.app.ui.referral

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.ReferralInfo
import com.teachmeski.app.ui.theme.TmsColor
import java.text.DateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * F-116: instructor's referral-code share screen. Mirrors web
 * `app/[locale]/(dashboard)/dashboard/referral/ReferralClient.tsx` and iOS
 * `UI/Referral/ReferralScreen.swift` 1:1.
 *
 * Section order (top-down): code card (with inline share button) → rules
 * card → campaign card (only while [ReferralInfo.isCampaignActive]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferralScreen(
    viewModel: ReferralViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = TmsColor.SurfaceLowest,
                    scrolledContainerColor = TmsColor.SurfaceLowest,
                ),
                title = { Text(stringResource(R.string.referral_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TmsColor.Background),
        ) {
            val info = uiState.info
            when {
                uiState.isLoading && info == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = TmsColor.Primary)
                    }
                }

                info == null && uiState.error != null -> {
                    val err = uiState.error
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = err?.asString().orEmpty(),
                            color = TmsColor.Error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = {
                            viewModel.consumeError()
                            viewModel.load()
                        }) {
                            Text(text = stringResource(R.string.common_retry))
                        }
                    }
                }

                info != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ReferralCodeCard(info = info)
                        ReferralRulesCard(info = info)
                        if (info.isCampaignActive()) {
                            ReferralCampaignCard(info = info)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReferralCodeCard(info: ReferralInfo) {
    val context = LocalContext.current
    val shareText = stringResource(R.string.referral_share_text_fmt, info.code)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TmsColor.SurfaceLowest, RoundedCornerShape(16.dp))
            .padding(20.dp),
    ) {
        Text(
            text = stringResource(R.string.referral_code_section_label),
            style = MaterialTheme.typography.bodyMedium,
            color = TmsColor.OnSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = info.code,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold,
            color = TmsColor.Primary,
            letterSpacing = 8.sp,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(intent, null))
            },
            colors = ButtonDefaults.buttonColors(containerColor = TmsColor.Primary),
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(R.string.referral_share_button))
        }
    }
}

@Composable
private fun ReferralRulesCard(info: ReferralInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TmsColor.SurfaceLowest, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.referral_rules_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TmsColor.OnSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.referral_rules_body_fmt, info.currentReward()),
            style = MaterialTheme.typography.bodyMedium,
            color = TmsColor.OnSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.referral_rules_note),
            style = MaterialTheme.typography.bodySmall,
            color = TmsColor.Outline,
        )
    }
}

@Composable
private fun ReferralCampaignCard(info: ReferralInfo) {
    val locale = LocalConfiguration.current.locales[0]
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TmsColor.PrimaryFixed, RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.referral_campaign_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = TmsColor.OnSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(
                R.string.referral_campaign_body_fmt,
                info.campaignReward ?: info.baseReward,
                info.baseReward,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = TmsColor.OnSurfaceVariant,
        )
        info.campaignEndsAt?.let { endsAt ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(
                    R.string.referral_campaign_deadline_fmt,
                    formatCampaignDeadline(endsAt, locale),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = TmsColor.Outline,
            )
        }
    }
}

private fun formatCampaignDeadline(endsAt: Instant, locale: Locale): String =
    DateFormat.getDateInstance(DateFormat.LONG, locale).apply {
        timeZone = TimeZone.getTimeZone("Asia/Taipei")
    }.format(Date.from(endsAt))

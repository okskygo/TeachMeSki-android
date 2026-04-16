package com.teachmeski.app.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.TmsTopBar
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun WalletScreen(
    viewModel: WalletViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToCreditHistory: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TmsTopBar(
                title = stringResource(R.string.wallet_title),
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TmsColor.Background),
        ) {
            when {
                uiState.isLoading && uiState.wallet == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = TmsColor.Primary)
                    }
                }

                uiState.wallet == null && uiState.error != null -> {
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

                else -> {
                    val balance = uiState.wallet?.balance ?: 0
                    val scroll = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scroll)
                            .padding(16.dp),
                    ) {
                        Surface(
                            color = TmsColor.PrimaryFixed,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            shadowElevation = 2.dp,
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = stringResource(R.string.wallet_balance_label),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = TmsColor.Primary,
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = stringResource(R.string.wallet_tokens_fmt, balance),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TmsColor.OnSurface,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.wallet_balance_tokens),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TmsColor.OnSurfaceVariant,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = stringResource(R.string.wallet_packages_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TmsColor.OnSurface,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        CreditPackagesGrid()
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onNavigateToCreditHistory,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TmsColor.Primary,
                                contentColor = TmsColor.OnPrimary,
                            ),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(stringResource(R.string.wallet_view_history))
                        }
                    }
                }
            }
        }
    }
}

private data class WalletCreditPackage(
    val nameRes: Int,
    val baseCredits: Int,
    val bonusCredits: Int,
    val priceYen: Int,
    val popular: Boolean,
) {
    val totalCredits: Int get() = baseCredits + bonusCredits
}

private val walletCreditPackages: List<WalletCreditPackage> = listOf(
    WalletCreditPackage(R.string.wallet_package_starter, 300, 0, 3_000, popular = false),
    WalletCreditPackage(R.string.wallet_package_standard, 600, 50, 5_500, popular = true),
    WalletCreditPackage(R.string.wallet_package_professional, 1_200, 150, 10_000, popular = false),
    WalletCreditPackage(R.string.wallet_package_enterprise, 2_500, 400, 18_000, popular = false),
)

@Composable
private fun CreditPackagesGrid() {
    val rows = walletCreditPackages.chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { rowPkgs ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowPkgs.forEach { pkg ->
                    CreditPackageCard(
                        pkg = pkg,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowPkgs.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CreditPackageCard(
    pkg: WalletCreditPackage,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]
    val numberFormat = remember(locale) { NumberFormat.getNumberInstance(locale) }
    val priceText = stringResource(
        R.string.wallet_package_price_fmt,
        numberFormat.format(pkg.priceYen),
    )
    val cardShape = RoundedCornerShape(16.dp)
    Box(modifier = modifier) {
        ElevatedCard(
            shape = cardShape,
            colors = CardDefaults.elevatedCardColors(containerColor = TmsColor.SurfaceLowest),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (pkg.popular) {
                        Modifier.border(2.dp, TmsColor.Primary.copy(alpha = 0.45f), cardShape)
                    } else {
                        Modifier
                    },
                ),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                if (pkg.popular) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = stringResource(pkg.nameRes),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = TmsColor.OnSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = numberFormat.format(pkg.totalCredits.toLong()),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TmsColor.OnSurface,
                )
                Text(
                    text = stringResource(R.string.wallet_package_credits_fmt, pkg.totalCredits),
                    style = MaterialTheme.typography.bodySmall,
                    color = TmsColor.OnSurfaceVariant,
                )
                if (pkg.bonusCredits > 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = TmsColor.PrimaryFixed,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CardGiftcard,
                                contentDescription = null,
                                tint = TmsColor.Primary,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = stringResource(R.string.wallet_package_bonus_fmt, pkg.bonusCredits),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = TmsColor.Primary,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = priceText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TmsColor.OnSurface,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = TmsColor.Primary.copy(alpha = 0.38f),
                        disabledContentColor = TmsColor.OnPrimary.copy(alpha = 0.85f),
                    ),
                ) {
                    Text(text = stringResource(R.string.wallet_package_coming_soon))
                }
                Text(
                    text = stringResource(R.string.wallet_package_coming_soon),
                    style = MaterialTheme.typography.bodySmall,
                    color = TmsColor.Outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }
        }
        if (pkg.popular) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-10).dp),
                shape = RoundedCornerShape(999.dp),
                color = TmsColor.Primary,
                shadowElevation = 2.dp,
            ) {
                Text(
                    text = stringResource(R.string.wallet_package_popular),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TmsColor.OnPrimary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

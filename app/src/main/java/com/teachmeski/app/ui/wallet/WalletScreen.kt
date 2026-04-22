package com.teachmeski.app.ui.wallet

import android.app.Activity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.iap.IapProduct
import com.teachmeski.app.ui.component.TmsTopBar
import com.teachmeski.app.ui.theme.TmsColor
import com.teachmeski.app.util.UiText

private const val PURCHASE_TERMS_URL = "https://teachmeski.com/terms#purchase-terms"

@Composable
fun WalletScreen(
    viewModel: WalletViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onNavigateToCreditHistory: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Recover pending purchases whenever the screen comes back to the foreground.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.recoverPendingPurchases()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Pipe one-shot snackbar signals from the VM. We resolve UiText -> String in a
    // composable scope by keeping the latest message in a Compose state and reading
    // it via @Composable asString() just before showing.
    val latestSnackbar = remember { mutableStateOf<UiText?>(null) }
    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { msg -> latestSnackbar.value = msg }
    }
    val pendingMsg = latestSnackbar.value
    if (pendingMsg != null) {
        val resolved = pendingMsg.asString()
        LaunchedEffect(pendingMsg) {
            snackbarHostState.showSnackbar(resolved)
            latestSnackbar.value = null
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TmsTopBar(
                title = stringResource(R.string.wallet_title),
                onBack = onBack,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                    val activity = context as? Activity
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scroll)
                            .padding(16.dp),
                    ) {
                        BalanceCard(balance)
                        Spacer(modifier = Modifier.height(20.dp))
                        HistoryButton(onClick = onNavigateToCreditHistory)
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = stringResource(R.string.wallet_packages_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TmsColor.OnSurface,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        IapPackagesSection(
                            products = uiState.products,
                            productsLoading = uiState.productsLoading,
                            productsError = uiState.productsError,
                            iapPhase = uiState.iapPhase,
                            onBuy = { productId ->
                                if (activity != null) viewModel.onPackageTapped(activity, productId)
                            },
                            onRetryProducts = { viewModel.loadProducts() },
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        PurchaseTermsLink(
                            onClick = { openCustomTab(context, PURCHASE_TERMS_URL) },
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            val phase = uiState.iapPhase
            if (phase is IapPhase.Success) {
                WalletSuccessOverlay(
                    delta = phase.delta,
                    newBalance = phase.newBalance,
                    onDismiss = { viewModel.dismissIapPhase() },
                )
            }
        }
    }
}

@Composable
private fun BalanceCard(balance: Int) {
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
}

@Composable
private fun HistoryButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
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

@Composable
private fun IapPackagesSection(
    products: List<IapProduct>,
    productsLoading: Boolean,
    productsError: UiText?,
    iapPhase: IapPhase,
    onBuy: (productId: String) -> Unit,
    onRetryProducts: () -> Unit,
) {
    when {
        productsLoading && products.isEmpty() -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    color = TmsColor.Primary,
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = stringResource(R.string.wallet_iap_products_loading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TmsColor.OnSurfaceVariant,
                )
            }
        }

        products.isEmpty() && productsError != null -> {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = productsError.asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TmsColor.OnSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onRetryProducts) {
                    Text(stringResource(R.string.common_retry))
                }
            }
        }

        else -> {
            val rows = products.chunked(2)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rows.forEach { rowPkgs ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        rowPkgs.forEach { product ->
                            IapProductCard(
                                product = product,
                                busy = iapPhase is IapPhase.Purchasing && iapPhase.productId == product.productId ||
                                    iapPhase is IapPhase.Verifying && iapPhase.productId == product.productId,
                                anyBusy = iapPhase !is IapPhase.Idle && iapPhase !is IapPhase.Success,
                                onBuy = { onBuy(product.productId) },
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
    }
}

@Composable
private fun IapProductCard(
    product: IapProduct,
    busy: Boolean,
    anyBusy: Boolean,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val popular = product.tierKey == "popular"
    val cardShape = RoundedCornerShape(16.dp)
    Box(modifier = modifier) {
        ElevatedCard(
            shape = cardShape,
            colors = CardDefaults.elevatedCardColors(containerColor = TmsColor.SurfaceLowest),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (popular) {
                        Modifier.border(2.dp, TmsColor.Primary.copy(alpha = 0.45f), cardShape)
                    } else {
                        Modifier
                    },
                ),
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                if (popular) Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = tierDisplayName(product.tierKey),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = TmsColor.OnSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = product.totalTokens.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TmsColor.OnSurface,
                )
                Text(
                    text = stringResource(R.string.wallet_package_credits_fmt, product.totalTokens),
                    style = MaterialTheme.typography.bodySmall,
                    color = TmsColor.OnSurfaceVariant,
                )
                if (product.bonusTokens > 0) {
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
                                text = stringResource(R.string.wallet_package_bonus_fmt, product.bonusTokens),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = TmsColor.Primary,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = product.formattedPrice,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TmsColor.OnSurface,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onBuy,
                    enabled = !anyBusy,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TmsColor.Primary,
                        contentColor = TmsColor.OnPrimary,
                        disabledContainerColor = TmsColor.Primary.copy(alpha = 0.38f),
                        disabledContentColor = TmsColor.OnPrimary.copy(alpha = 0.85f),
                    ),
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            color = TmsColor.OnPrimary,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(text = stringResource(R.string.wallet_iap_buy_cta_fmt, product.formattedPrice))
                    }
                }
            }
        }
        if (popular) {
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

@Composable
private fun tierDisplayName(tierKey: String): String = when (tierKey) {
    "starter" -> stringResource(R.string.wallet_package_starter)
    "popular" -> stringResource(R.string.wallet_package_standard)
    "pro" -> stringResource(R.string.wallet_package_professional)
    "premium" -> stringResource(R.string.wallet_package_enterprise)
    else -> tierKey
}

@Composable
private fun PurchaseTermsLink(onClick: () -> Unit) {
    Text(
        text = stringResource(R.string.wallet_purchase_terms_link),
        style = MaterialTheme.typography.bodyMedium,
        color = TmsColor.Primary,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
    )
}

private fun openCustomTab(context: android.content.Context, url: String) {
    val primary = TmsColor.Primary.toArgb()
    val intent = CustomTabsIntent.Builder()
        .setDefaultColorSchemeParams(
            CustomTabColorSchemeParams.Builder()
                .setToolbarColor(primary)
                .build(),
        )
        .build()
    runCatching { intent.launchUrl(context, url.toUri()) }
}

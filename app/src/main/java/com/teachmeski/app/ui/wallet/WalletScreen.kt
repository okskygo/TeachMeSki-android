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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import java.text.NumberFormat
import java.util.Locale

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

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.recoverPendingPurchases()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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
        containerColor = TmsColor.Background,
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
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp, bottom = 24.dp),
                    ) {
                        BalanceHero(
                            balance = balance,
                            onViewHistory = onNavigateToCreditHistory,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TermsBox(
                            onClick = { openCustomTab(context, PURCHASE_TERMS_URL) },
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        PackagesSectionHeader()
                        Spacer(modifier = Modifier.height(12.dp))
                        PackagesList(
                            products = uiState.products,
                            productsLoading = uiState.productsLoading,
                            productsError = uiState.productsError,
                            iapPhase = uiState.iapPhase,
                            onBuy = { productId ->
                                if (activity != null) viewModel.onPackageTapped(activity, productId)
                            },
                            onRetryProducts = { viewModel.loadProducts() },
                        )
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

// ---------- Balance Hero ----------

@Composable
private fun BalanceHero(balance: Int, onViewHistory: () -> Unit) {
    val heroGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF003D6B),
            TmsColor.Primary,
            Color(0xFF0866A8),
        ),
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(heroGradient)
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(TmsColor.SecondaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "$",
                            color = Color(0xFF003D6B),
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                        )
                    }
                    Text(
                        text = stringResource(R.string.wallet_balance_title),
                        color = TmsColor.OnPrimary.copy(alpha = 0.82f),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = formatNumber(balance),
                        color = TmsColor.OnPrimary,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = stringResource(R.string.wallet_balance_tokens),
                        color = TmsColor.OnPrimary.copy(alpha = 0.75f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(TmsColor.OnPrimary.copy(alpha = 0.12f))
                    .border(
                        width = 1.dp,
                        color = TmsColor.OnPrimary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp),
                    )
                    .clickable(onClick = onViewHistory)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(
                    text = stringResource(R.string.wallet_view_history),
                    color = TmsColor.OnPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

// ---------- Terms Box ----------

@Composable
private fun TermsBox(onClick: () -> Unit) {
    val title = stringResource(R.string.wallet_terms_title)
    val body = stringResource(R.string.wallet_terms_body)
    val annotated = androidx.compose.ui.text.buildAnnotatedString {
        withStyle(
            androidx.compose.ui.text.SpanStyle(
                color = TmsColor.OnSurface,
                fontWeight = FontWeight.Bold,
            ),
        ) { append(title) }
        withStyle(
            androidx.compose.ui.text.SpanStyle(color = TmsColor.OnSurfaceVariant),
        ) { append(" · ") }
        append(body)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TmsColor.SurfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = TmsColor.Primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = annotated,
            style = MaterialTheme.typography.bodySmall,
            color = TmsColor.OnSurfaceVariant,
            lineHeight = 18.sp,
        )
    }
}

// ---------- Packages list ----------

@Composable
private fun PackagesSectionHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.wallet_packages_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = TmsColor.OnSurface,
        )
        Text(
            text = stringResource(R.string.wallet_packages_hint),
            style = MaterialTheme.typography.bodySmall,
            color = TmsColor.Outline,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun PackagesList(
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
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onRetryProducts) {
                    Text(stringResource(R.string.common_retry))
                }
            }
        }

        else -> {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                products.forEach { product ->
                    PackageCard(
                        product = product,
                        busy = iapPhase is IapPhase.Purchasing && iapPhase.productId == product.productId ||
                            iapPhase is IapPhase.Verifying && iapPhase.productId == product.productId,
                        anyBusy = iapPhase !is IapPhase.Idle && iapPhase !is IapPhase.Success,
                        onBuy = { onBuy(product.productId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PackageCard(
    product: IapProduct,
    busy: Boolean,
    anyBusy: Boolean,
    onBuy: () -> Unit,
) {
    val featured = product.tierKey == "popular"
    val cardShape = RoundedCornerShape(16.dp)
    val cardBackground: Brush = if (featured) {
        Brush.linearGradient(
            colors = listOf(TmsColor.SurfaceLowest, TmsColor.PrimaryFixed.copy(alpha = 0.55f)),
        )
    } else {
        Brush.linearGradient(colors = listOf(TmsColor.SurfaceLowest, TmsColor.SurfaceLowest))
    }
    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(cardBackground)
                .then(
                    if (featured) {
                        Modifier.border(2.dp, TmsColor.Primary, cardShape)
                    } else {
                        Modifier.border(1.dp, TmsColor.OutlineVariant.copy(alpha = 0.6f), cardShape)
                    },
                )
                .padding(horizontal = 18.dp, vertical = 18.dp),
        ) {
            // Top row: name/desc | points/bonus
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tierDisplayName(product.tierKey),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TmsColor.OnSurface,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tierDescription(product.tierKey),
                        style = MaterialTheme.typography.bodySmall,
                        color = TmsColor.Outline,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = formatNumber(product.totalTokens),
                            color = if (featured) Color(0xFF003D6B) else TmsColor.OnSurface,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Text(
                            text = stringResource(R.string.wallet_balance_tokens),
                            color = TmsColor.Outline,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                    if (product.bonusTokens > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = TmsColor.SecondaryContainer.copy(alpha = 0.15f),
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.wallet_package_bonus_inline_fmt,
                                    product.bonusTokens,
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TmsColor.Warning,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(TmsColor.OutlineVariant.copy(alpha = 0.5f)),
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Bottom row: price | buy button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = product.formattedPrice,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TmsColor.OnSurface,
                )
                Button(
                    onClick = onBuy,
                    enabled = !anyBusy,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (featured) Color(0xFF003D6B) else TmsColor.Primary,
                        contentColor = TmsColor.OnPrimary,
                        disabledContainerColor = TmsColor.Primary.copy(alpha = 0.38f),
                        disabledContentColor = TmsColor.OnPrimary.copy(alpha = 0.85f),
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 28.dp,
                        vertical = 12.dp,
                    ),
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            color = TmsColor.OnPrimary,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.wallet_buy_cta),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                }
            }
        }

        if (featured) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-18).dp, y = (-10).dp),
                shape = RoundedCornerShape(999.dp),
                color = TmsColor.SecondaryContainer,
                shadowElevation = 4.dp,
            ) {
                Text(
                    text = stringResource(R.string.wallet_package_featured),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = TmsColor.OnSurface,
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
private fun tierDescription(tierKey: String): String = when (tierKey) {
    "starter" -> stringResource(R.string.wallet_package_desc_starter)
    "popular" -> stringResource(R.string.wallet_package_desc_popular)
    "pro" -> stringResource(R.string.wallet_package_desc_pro)
    "premium" -> stringResource(R.string.wallet_package_desc_premium)
    else -> ""
}

private fun formatNumber(value: Int): String =
    NumberFormat.getNumberInstance(Locale.US).format(value)

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

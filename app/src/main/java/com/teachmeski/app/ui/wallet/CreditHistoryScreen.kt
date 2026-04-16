package com.teachmeski.app.ui.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.TokenTransaction
import com.teachmeski.app.ui.component.TmsTopBar
import com.teachmeski.app.ui.theme.TmsColor
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun CreditHistoryScreen(
    viewModel: CreditHistoryViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(listState, viewModel) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = info.totalItemsCount
            val nearEnd = total > 0 && last >= total - 2
            val s = viewModel.uiState.value
            nearEnd to (s.hasMore && !s.isLoadingMore && !s.isLoading)
        }
            .distinctUntilChanged()
            .collect { (nearEnd, canLoad) ->
                if (nearEnd && canLoad) {
                    viewModel.loadMore()
                }
            }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TmsTopBar(
                title = stringResource(R.string.wallet_credit_history_title),
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
                uiState.isLoading && uiState.transactions.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = TmsColor.Primary)
                    }
                }

                uiState.transactions.isEmpty() && uiState.error != null -> {
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
                            viewModel.loadPage(1)
                        }) {
                            Text(text = stringResource(R.string.common_retry))
                        }
                    }
                }

                uiState.transactions.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.wallet_empty_transactions),
                            style = MaterialTheme.typography.titleMedium,
                            color = TmsColor.OnSurfaceVariant,
                            modifier = Modifier.padding(32.dp),
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        uiState.error?.let { err ->
                            item(key = "error_banner") {
                                Surface(
                                    color = TmsColor.ErrorContainer,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = err.asString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TmsColor.Error,
                                            modifier = Modifier.weight(1f),
                                        )
                                        TextButton(onClick = {
                                            viewModel.consumeError()
                                            viewModel.loadPage(1)
                                        }) {
                                            Text(stringResource(R.string.common_retry))
                                        }
                                    }
                                }
                            }
                        }
                        items(
                            items = uiState.transactions,
                            key = { it.id },
                        ) { tx ->
                            TransactionRow(transaction = tx)
                        }
                        if (uiState.isLoadingMore) {
                            item(key = "loading_more") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = TmsColor.Primary,
                                        strokeWidth = 2.dp,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: TokenTransaction,
) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]
    val dateLabel = formatTransactionDate(iso = transaction.createdAt, locale = locale)
    val typeLabel = transactionTypeLabel(transaction.type)
    val amountText = signedAmountText(transaction.amount)
    val amountColor = when {
        transaction.amount > 0 -> TmsColor.Success
        transaction.amount < 0 -> TmsColor.Error
        else -> TmsColor.OnSurfaceVariant
    }
    val balanceAfter = stringResource(
        R.string.wallet_transaction_balance_after_fmt,
        transaction.balanceAfter,
    )

    Surface(
        color = TmsColor.SurfaceLowest,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = typeLabel,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TmsColor.OnSurface,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodySmall,
                color = TmsColor.OnSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor,
                )
                Text(
                    text = balanceAfter,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TmsColor.OnSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun transactionTypeLabel(type: String): String {
    return when (type.lowercase(Locale.US)) {
        "unlock" -> stringResource(R.string.wallet_transaction_unlock)
        "purchase" -> stringResource(R.string.wallet_transaction_purchase)
        "bonus" -> stringResource(R.string.wallet_transaction_bonus)
        "compensation" -> stringResource(R.string.wallet_transaction_compensation)
        "refund" -> stringResource(R.string.wallet_transaction_refund)
        else -> stringResource(R.string.wallet_transaction_other)
    }
}

@Composable
private fun signedAmountText(amount: Int): String {
    return if (amount > 0) {
        stringResource(R.string.wallet_transaction_amount_positive_fmt, amount)
    } else {
        stringResource(R.string.wallet_transaction_amount_signed_fmt, amount)
    }
}

private fun formatTransactionDate(iso: String, locale: Locale): String {
    val millis = parseIsoTimestampToMillis(iso) ?: return iso.substringBefore('T').ifBlank { iso }
    return DateFormat.getDateTimeInstance(
        DateFormat.SHORT,
        DateFormat.SHORT,
        locale,
    ).format(Date(millis))
}

private fun parseIsoTimestampToMillis(iso: String): Long? {
    val candidates = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ssX",
    )
    for (pattern in candidates) {
        try {
            val fmt = SimpleDateFormat(pattern, Locale.US)
            fmt.isLenient = false
            return fmt.parse(iso)?.time
        } catch (_: ParseException) {
            continue
        }
    }
    return try {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        fmt.parse(iso)?.time
    } catch (_: ParseException) {
        null
    }
}

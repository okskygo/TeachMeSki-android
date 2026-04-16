package com.teachmeski.app.ui.account

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.TmsTopBar

private const val TYPE_TERMS = "terms"
private const val BASE_URL = "https://teachmeski.com"

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LegalScreen(
    type: String,
    onBack: () -> Unit,
) {
    val titleRes = when (type) {
        TYPE_TERMS -> R.string.legal_terms_title
        else -> R.string.legal_privacy_title
    }
    val url = "$BASE_URL/$type"

    Column(modifier = Modifier.fillMaxSize()) {
        TmsTopBar(
            title = stringResource(titleRes),
            onBack = onBack,
        )
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    loadUrl(url)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
        )
    }
}

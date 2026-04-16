package com.teachmeski.app.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText {
    data class StringResource(
        @param:StringRes val resId: Int,
        val args: List<Any> = emptyList(),
    ) : UiText()

    data class DynamicString(val value: String) : UiText()

    @Composable
    fun asString(): String = when (this) {
        is StringResource -> stringResource(resId, *args.toTypedArray())
        is DynamicString -> value
    }
}

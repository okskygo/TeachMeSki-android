package com.teachmeski.app.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.theme.TmsColor

data class PasswordRules(
    val hasMinLength: Boolean = false,
    val hasMaxLength: Boolean = true,
    val hasUppercase: Boolean = false,
    val hasLowercase: Boolean = false,
    val hasDigit: Boolean = false,
) {
    val allPassed: Boolean
        get() = hasMinLength && hasMaxLength && hasUppercase && hasLowercase && hasDigit

    companion object {
        fun check(password: String) = PasswordRules(
            hasMinLength = password.length >= 8,
            hasMaxLength = password.length <= 128,
            hasUppercase = password.contains(Regex("[A-Z]")),
            hasLowercase = password.contains(Regex("[a-z]")),
            hasDigit = password.contains(Regex("[0-9]")),
        )
    }
}

@Composable
fun PasswordRulesDisplay(rules: PasswordRules, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        RuleItem(rules.hasMinLength, stringResource(R.string.auth_signup_password_rule_min))
        RuleItem(rules.hasMaxLength, stringResource(R.string.auth_signup_password_rule_max))
        RuleItem(rules.hasUppercase, stringResource(R.string.auth_signup_password_rule_upper))
        RuleItem(rules.hasLowercase, stringResource(R.string.auth_signup_password_rule_lower))
        RuleItem(rules.hasDigit, stringResource(R.string.auth_signup_password_rule_digit))
    }
}

@Composable
private fun RuleItem(passed: Boolean, label: String) {
    val color = if (passed) TmsColor.Success else MaterialTheme.colorScheme.onSurfaceVariant
    val icon = if (passed) "✓" else "○"
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodySmall,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color,
        )
    }
}

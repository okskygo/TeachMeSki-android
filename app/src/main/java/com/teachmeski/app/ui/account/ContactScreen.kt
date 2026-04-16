package com.teachmeski.app.ui.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.TmsLoadingButton
import com.teachmeski.app.ui.component.TmsTextField
import com.teachmeski.app.ui.component.TmsTopBar

@Composable
fun ContactScreen(
    onBack: () -> Unit,
    viewModel: ContactViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        TmsTopBar(
            title = stringResource(R.string.contact_title),
            onBack = onBack,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scroll)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            if (uiState.submitSuccess) {
                Text(
                    text = stringResource(R.string.contact_success_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.contact_success_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Text(
                    text = stringResource(R.string.contact_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(20.dp))

                TmsTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = stringResource(R.string.contact_name_label),
                    placeholder = stringResource(R.string.contact_name_placeholder),
                    isError = uiState.nameError != null,
                    errorMessage = uiState.nameError?.asString(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                    ),
                )
                Spacer(modifier = Modifier.height(12.dp))
                TmsTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    label = stringResource(R.string.contact_email_label),
                    placeholder = stringResource(R.string.contact_email_placeholder),
                    isError = uiState.emailError != null,
                    errorMessage = uiState.emailError?.asString(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                Spacer(modifier = Modifier.height(12.dp))
                TmsTextField(
                    value = uiState.message,
                    onValueChange = viewModel::onMessageChange,
                    label = stringResource(R.string.contact_message_label),
                    placeholder = stringResource(R.string.contact_message_placeholder),
                    isError = uiState.messageError != null,
                    errorMessage = uiState.messageError?.asString(),
                    singleLine = false,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )

                uiState.error?.let { err ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = err.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                TmsLoadingButton(
                    text = stringResource(R.string.contact_submit),
                    onClick = { viewModel.submit() },
                    isLoading = uiState.isSubmitting,
                )
            }
        }
    }
}

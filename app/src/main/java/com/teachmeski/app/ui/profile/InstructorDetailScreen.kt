package com.teachmeski.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.TmsTopBar
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InstructorDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val shortId: String = savedStateHandle.get<String>("shortId") ?: ""
}

@Composable
fun InstructorDetailScreen(
    onBack: () -> Unit,
    viewModel: InstructorDetailViewModel = hiltViewModel(),
) {
    val shortId = viewModel.shortId
    Scaffold(
        topBar = {
            TmsTopBar(
                title = stringResource(R.string.placeholder_instructor_account),
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.placeholder_instructor_account),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = shortId.ifBlank { stringResource(R.string.common_empty_value) },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

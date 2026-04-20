package com.teachmeski.app.ui.profile.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.InstructorDetailBundle
import com.teachmeski.app.ui.component.TmsTopBar
import com.teachmeski.app.ui.profile.detail.components.DetailBioSection
import com.teachmeski.app.ui.profile.detail.components.DetailCertificatesSection
import com.teachmeski.app.ui.profile.detail.components.DetailCertificationsSection
import com.teachmeski.app.ui.profile.detail.components.DetailDisciplineSection
import com.teachmeski.app.ui.profile.detail.components.DetailHeaderSection
import com.teachmeski.app.ui.profile.detail.components.DetailLanguagesSection
import com.teachmeski.app.ui.profile.detail.components.DetailLevelsSection
import com.teachmeski.app.ui.profile.detail.components.DetailPricingSection
import com.teachmeski.app.ui.profile.detail.components.DetailResortsSection
import com.teachmeski.app.ui.profile.detail.components.DetailServicesSection
import com.teachmeski.app.ui.profile.detail.components.NotPublicBanner
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun InstructorDetailScreen(
    onBack: () -> Unit,
    viewModel: InstructorDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TmsTopBar(
                title = stringResource(R.string.instructor_detail_title),
                onBack = onBack,
            )
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            when (val s = state) {
                InstructorDetailUiState.Loading -> LoadingView()
                InstructorDetailUiState.NotFound -> NotFoundView()
                is InstructorDetailUiState.Error ->
                    ErrorView(
                        message = s.message.asString(),
                        onRetry = viewModel::load,
                    )
                is InstructorDetailUiState.Success -> SuccessContent(s.bundle)
            }
        }
    }
}

@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = TmsColor.Primary)
    }
}

@Composable
private fun NotFoundView() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Text(
            text = stringResource(R.string.instructor_detail_error_not_found),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        Button(onClick = onRetry) {
            Text(stringResource(R.string.instructor_detail_retry))
        }
    }
}

@Composable
private fun SuccessContent(bundle: InstructorDetailBundle) {
    val profile = bundle.profile
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        if (!profile.isAcceptingRequests) {
            item { NotPublicBanner() }
        }
        item { DetailHeaderSection(profile) }
        if (!profile.bio.isNullOrBlank()) {
            item { DetailBioSection(profile.bio!!) }
        }
        item { DetailDisciplineSection(profile.discipline) }
        if (profile.teachableLevels.isNotEmpty()) {
            item { DetailLevelsSection(profile.discipline, profile.teachableLevels) }
        }
        item { DetailResortsSection(bundle.resortsByRegion) }
        item { DetailCertificationsSection(profile.certifications, profile.certificationOther) }
        if (profile.languages.isNotEmpty()) {
            item { DetailLanguagesSection(profile.languages) }
        }
        item { DetailPricingSection(profile.priceHalfDay, profile.priceFullDay) }
        item { DetailServicesSection(profile.offersTransport, profile.offersPhotography) }
        if (profile.certificateUrls.isNotEmpty()) {
            item { DetailCertificatesSection(profile.certificateUrls) }
        }
    }
}

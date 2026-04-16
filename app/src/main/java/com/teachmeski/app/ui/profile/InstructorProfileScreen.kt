package com.teachmeski.app.ui.profile

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DownhillSkiing
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.Discipline
import com.teachmeski.app.domain.model.InstructorProfile
import com.teachmeski.app.domain.model.Region
import com.teachmeski.app.domain.model.SkiResort
import com.teachmeski.app.ui.account.MAX_DISPLAY_NAME_LENGTH
import com.teachmeski.app.ui.component.PhoneVerificationBadge
import com.teachmeski.app.ui.component.TmsTopBar
import com.teachmeski.app.ui.component.UserAvatar
import com.teachmeski.app.ui.theme.TmsColor
import com.teachmeski.app.ui.wizard.ResortSelector
import com.teachmeski.app.util.UiText
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val certificationOptionIds = listOf("CSIA", "CASI", "NZSIA", "PSIA", "SIA_Japan", "other")

@Composable
private fun certificationLabelRes(id: String): Int =
    when (id) {
        "CSIA" -> R.string.instructor_wizard_step4_cert_CSIA
        "CASI" -> R.string.instructor_wizard_step4_cert_CASI
        "NZSIA" -> R.string.instructor_wizard_step4_cert_NZSIA
        "PSIA" -> R.string.instructor_wizard_step4_cert_PSIA
        "SIA_Japan" -> R.string.instructor_wizard_step4_cert_SIA_Japan
        "other" -> R.string.instructor_wizard_step4_cert_other
        else -> R.string.instructor_wizard_step4_cert_other
    }

private fun levelDescriptionRes(level: Int): Int =
    when (level) {
        0 -> R.string.instructor_profile_level_0
        1 -> R.string.instructor_profile_level_1
        2 -> R.string.instructor_profile_level_2
        3 -> R.string.instructor_profile_level_3
        4 -> R.string.instructor_profile_level_4
        5 -> R.string.instructor_profile_level_5
        6 -> R.string.instructor_profile_level_6
        else -> R.string.instructor_profile_level_6
    }

private fun regionDisplayName(
    region: Region,
    locale: Locale,
): String =
    if (locale.language.startsWith("zh")) {
        region.nameZh
    } else {
        region.nameEn
    }

private fun resortDisplayName(
    resort: SkiResort,
    locale: Locale,
): String =
    if (locale.language.startsWith("zh")) {
        resort.nameZh
    } else {
        resort.nameEn
    }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InstructorProfileScreen(
    viewModel: InstructorProfileViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var certPendingDelete by remember { mutableStateOf<String?>(null) }
    val saveSuccessMessage = stringResource(R.string.account_save_success)

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            snackbarHostState.showSnackbar(saveSuccessMessage)
            viewModel.consumeSaveSuccess()
        }
    }

    LaunchedEffect(state.saveError, state.openDialog) {
        val err = state.saveError ?: return@LaunchedEffect
        if (state.openDialog == ProfileEditDialog.None) {
            snackbarHostState.showSnackbar(err.toMessage(context))
            viewModel.clearSaveError()
        }
    }

    LaunchedEffect(state.uploadCertError) {
        val err = state.uploadCertError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(err.toMessage(context))
        viewModel.clearUploadCertError()
    }

    val avatarPicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            if (uri != null) {
                viewModel.uploadAvatar(context, uri)
            }
        }

    val certPicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            if (uri == null) return@rememberLauncherForActivityResult
            scope.launch(Dispatchers.IO) {
                val bytes =
                    try {
                        context.contentResolver.openInputStream(uri)?.readBytes()
                    } catch (_: Exception) {
                        null
                    }
                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                if (bytes != null && bytes.isNotEmpty()) {
                    val payload = bytes
                    withContext(Dispatchers.Main) {
                        viewModel.uploadCertificate(payload, mime)
                    }
                }
            }
        }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TmsTopBar(
                title = stringResource(R.string.instructor_profile_title),
                onBack = onBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            state.isLoading && state.profile == null -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = TmsColor.Primary)
                }
            }
            state.profile == null -> {
                val err = state.error
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = err?.asString().orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TmsColor.Error,
                    )
                }
            }
            else -> {
                val profile = state.profile!!
                val regions = state.regions
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        OutlinedButton(
                            onClick = { /* preview placeholder */ },
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    contentColor = TmsColor.Primary,
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.instructor_profile_preview))
                        }
                        OutlinedButton(
                            onClick = { /* share placeholder */ },
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    contentColor = TmsColor.Primary,
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.instructor_profile_share))
                        }
                    }
                    HeaderCard(
                        profile = profile,
                        isSaving = state.isSaving,
                        onAvatarClick = { avatarPicker.launch("image/*") },
                        onEditName = { viewModel.openDialog(ProfileEditDialog.DisplayName) },
                        onEditDiscipline = { viewModel.openDialog(ProfileEditDialog.Discipline) },
                        onToggleAccepting = { viewModel.toggleAccepting(it) },
                    )
                    ProfileSectionCard(
                        title = stringResource(R.string.instructor_profile_levels_label),
                        onEdit = { viewModel.openDialog(ProfileEditDialog.Levels) },
                    ) {
                        InstructorLevelsList(levels = profile.teachableLevels.sorted())
                    }
                    ProfileSectionCard(
                        title = stringResource(R.string.instructor_profile_resorts_label),
                        onEdit = { viewModel.openDialog(ProfileEditDialog.Resorts) },
                    ) {
                        ResortsGroupedDisplay(
                            regions = regions,
                            selectedResortIds = profile.resortIds.toSet(),
                        )
                    }
                    ProfileSectionCard(
                        title = stringResource(R.string.instructor_profile_certs_label),
                        onEdit = { viewModel.openDialog(ProfileEditDialog.Certifications) },
                    ) {
                        val lines =
                            buildList {
                                profile.certifications.forEach { id ->
                                    add(
                                        stringResource(certificationLabelRes(id)) +
                                            if (id == "other" && !profile.certificationOther.isNullOrBlank()) {
                                                " — ${profile.certificationOther}"
                                            } else {
                                                ""
                                            },
                                    )
                                }
                                if ("other" !in profile.certifications &&
                                    !profile.certificationOther.isNullOrBlank()
                                ) {
                                    add(
                                        "${stringResource(R.string.instructor_wizard_step4_cert_other)} — ${profile.certificationOther}",
                                    )
                                }
                            }
                        Text(
                            text =
                                if (lines.isEmpty()) {
                                    stringResource(R.string.instructor_wizard_step7_not_set)
                                } else {
                                    lines.joinToString("\n")
                                },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TmsColor.OnSurfaceVariant,
                        )
                    }
                    ProfileSectionCard(
                        title = stringResource(R.string.instructor_profile_languages_label),
                        onEdit = { viewModel.openDialog(ProfileEditDialog.Languages) },
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            profile.languages.forEach { code ->
                                FilterChip(
                                    selected = true,
                                    onClick = {},
                                    enabled = false,
                                    label = { Text(languageLabel(code)) },
                                )
                            }
                        }
                    }
                    ProfileSectionCard(
                        title = stringResource(R.string.instructor_profile_pricing_label),
                        onEdit = { viewModel.openDialog(ProfileEditDialog.Pricing) },
                    ) {
                        Text(
                            text = pricingLineHalf(profile.priceHalfDay),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TmsColor.OnSurface,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = pricingLineFull(profile.priceFullDay),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TmsColor.OnSurface,
                        )
                    }
                    ProfileSectionCard(
                        title = stringResource(R.string.instructor_profile_services_label),
                        onEdit = { viewModel.openDialog(ProfileEditDialog.Services) },
                    ) {
                        InstructorServicesRows(
                            offersTransport = profile.offersTransport,
                            offersPhotography = profile.offersPhotography,
                        )
                    }
                    ProfileSectionCard(
                        title = stringResource(R.string.instructor_profile_bio_label),
                        onEdit = { viewModel.openDialog(ProfileEditDialog.Bio) },
                    ) {
                        Spacer(modifier = Modifier.height(0.dp))
                    }

                    CertificatesSection(
                        urls = profile.certificateUrls,
                        isBusy = state.isUploadingCert,
                        onAdd = { certPicker.launch("image/*") },
                        onRequestDelete = { certPendingDelete = it },
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    val profile = state.profile
    if (profile != null) {
        certPendingDelete?.let { url ->
            AlertDialog(
                onDismissRequest = { certPendingDelete = null },
                title = { Text(stringResource(R.string.instructor_profile_delete_cert_title)) },
                text = { Text(stringResource(R.string.instructor_profile_delete_cert_message)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteCertificate(url)
                            certPendingDelete = null
                        },
                        colors =
                            ButtonDefaults.textButtonColors(
                                contentColor = TmsColor.Error,
                            ),
                    ) {
                        Text(stringResource(R.string.common_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { certPendingDelete = null }) {
                        Text(stringResource(R.string.common_cancel))
                    }
                },
            )
        }
        ProfileEditDialogs(
            openDialog = state.openDialog,
            profile = profile,
            regions = state.regions,
            isSaving = state.isSaving,
            saveError = state.saveError,
            onDismiss = { viewModel.closeDialog() },
            onSaveDisplayName = { viewModel.saveUpdates(mapOf("display_name" to it)) },
            onSaveBio = { viewModel.saveUpdates(mapOf("bio" to it)) },
            onSaveDiscipline = { viewModel.saveUpdates(mapOf("discipline" to it.value)) },
            onSaveLevels = { viewModel.saveUpdates(mapOf("teachable_levels" to it.sorted())) },
            onSaveResorts = { viewModel.saveUpdates(mapOf("resort_ids" to it)) },
            onSaveCertifications = { certs, other ->
                val hasOther = "other" in certs
                val certList = certs.filter { it != "other" }
                viewModel.saveUpdates(
                    mapOf(
                        "certifications" to certList,
                        "certification_other" to
                            if (hasOther) {
                                other?.trim()?.takeIf { it.isNotEmpty() }
                            } else {
                                null
                            },
                    ),
                )
            },
            onSaveLanguages = { viewModel.saveUpdates(mapOf("languages" to it)) },
            onSavePricing = { half, full ->
                viewModel.saveUpdates(
                    mapOf(
                        "price_half_day" to half,
                        "price_full_day" to full,
                    ),
                )
            },
            onSaveServices = { transport, photo ->
                viewModel.saveUpdates(
                    mapOf(
                        "offers_transport" to transport,
                        "offers_photography" to photo,
                    ),
                )
            },
        )
    }
}

@Composable
private fun disciplineLabel(d: Discipline): String =
    when (d) {
        Discipline.Ski -> stringResource(R.string.common_discipline_ski)
        Discipline.Snowboard -> stringResource(R.string.common_discipline_snowboard)
        Discipline.Both -> stringResource(R.string.common_discipline_both)
    }

@Composable
private fun languageLabel(code: String): String =
    when (code) {
        "zh" -> stringResource(R.string.instructor_wizard_step6_lang_zh)
        "en" -> stringResource(R.string.instructor_wizard_step6_lang_en)
        "ja" -> stringResource(R.string.instructor_wizard_step6_lang_ja)
        else -> code
    }

@Composable
private fun pricingLineHalf(price: Int?): String =
    if (price != null) {
        stringResource(R.string.instructor_profile_half_day_fmt, price)
    } else {
        "${stringResource(R.string.instructor_wizard_step7_half_day_label)}: ${stringResource(R.string.instructor_wizard_step7_not_set)}"
    }

@Composable
private fun pricingLineFull(price: Int?): String =
    if (price != null) {
        stringResource(R.string.instructor_profile_full_day_fmt, price)
    } else {
        "${stringResource(R.string.instructor_wizard_step7_full_day_label)}: ${stringResource(R.string.instructor_wizard_step7_not_set)}"
    }

@Composable
private fun ProfileSectionCard(
    title: String,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = TmsColor.SurfaceLowest,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TmsColor.OnSurface,
                )
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.common_edit),
                        tint = TmsColor.Primary,
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun HeaderCard(
    profile: InstructorProfile,
    isSaving: Boolean,
    onAvatarClick: () -> Unit,
    onEditName: () -> Unit,
    onEditDiscipline: () -> Unit,
    onToggleAccepting: (Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = TmsColor.SurfaceLowest,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .clip(CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onAvatarClick,
                            ),
                ) {
                    UserAvatar(
                        displayName = profile.displayName,
                        avatarUrl = profile.avatarUrl,
                        size = 72.dp,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = profile.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = TmsColor.OnSurface,
                        )
                        IconButton(onClick = onEditName, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = stringResource(R.string.common_edit),
                                tint = TmsColor.Primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Text(
                        text = profile.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = TmsColor.OnSurfaceVariant,
                    )
                }
            }
            PhoneVerificationBadge(
                verified = profile.phoneVerifiedAt != null,
                verifiedLabel = stringResource(R.string.instructor_profile_phone_verified),
                unverifiedLabel = stringResource(R.string.instructor_profile_phone_not_verified),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.instructor_profile_toggle_accepting),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TmsColor.OnSurface,
                    )
                    Text(
                        text =
                            if (profile.isAcceptingRequests) {
                                stringResource(R.string.instructor_profile_accepting_label)
                            } else {
                                stringResource(R.string.instructor_profile_not_accepting_label)
                            },
                        style = MaterialTheme.typography.labelMedium,
                        color = TmsColor.OnSurfaceVariant,
                    )
                }
                Switch(
                    checked = profile.isAcceptingRequests,
                    onCheckedChange = onToggleAccepting,
                    enabled = !isSaving,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = disciplineLabel(profile.discipline),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TmsColor.OnSurface,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onEditDiscipline, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.common_edit),
                        tint = TmsColor.Primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Text(
                text =
                    profile.bio?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.instructor_profile_bio_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = TmsColor.OnSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InstructorLevelsList(levels: List<Int>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        levels.forEach { lv ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.DownhillSkiing,
                    contentDescription = null,
                    tint = TmsColor.Primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.explore_card_skill_level_fmt, lv.toString()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TmsColor.OnSurface,
                )
                Text(
                    text = " · ${stringResource(levelDescriptionRes(lv))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TmsColor.OnSurface,
                )
            }
        }
    }
}

@Composable
private fun InstructorServicesRows(
    offersTransport: Boolean,
    offersPhotography: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InstructorServiceRow(
            enabled = offersTransport,
            labelRes = R.string.instructor_profile_offers_transport,
        )
        InstructorServiceRow(
            enabled = offersPhotography,
            labelRes = R.string.instructor_profile_offers_photography,
        )
    }
}

@Composable
private fun InstructorServiceRow(
    enabled: Boolean,
    labelRes: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = if (enabled) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = null,
            tint = if (enabled) TmsColor.Success else TmsColor.Outline,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) TmsColor.OnSurface else TmsColor.Outline,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResortsGroupedDisplay(
    regions: List<Region>,
    selectedResortIds: Set<String>,
) {
    val locale = LocalConfiguration.current.locales[0]
    val blocks =
        remember(regions, selectedResortIds, locale.language) {
            regions.mapNotNull { region ->
                val names =
                    region.resorts
                        .filter { it.id in selectedResortIds }
                        .sortedBy { it.sortOrder }
                        .map { resortDisplayName(it, locale) }
                if (names.isEmpty()) null else region to names
            }
        }
    if (blocks.isEmpty()) {
        Text(
            text = stringResource(R.string.instructor_wizard_step7_not_set),
            style = MaterialTheme.typography.bodyMedium,
            color = TmsColor.OnSurfaceVariant,
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            blocks.forEach { (region, names) ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = regionDisplayName(region, locale),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TmsColor.OnSurface,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        names.forEach { name ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TmsColor.SurfaceLow,
                            ) {
                                Text(
                                    text = name,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TmsColor.OnSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CertificatesSection(
    urls: List<String>,
    isBusy: Boolean,
    onAdd: () -> Unit,
    onRequestDelete: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = TmsColor.SurfaceLowest,
        tonalElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.instructor_profile_certs_label),
                style = MaterialTheme.typography.titleMedium,
                color = TmsColor.OnSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.instructor_profile_cert_limit),
                style = MaterialTheme.typography.bodySmall,
                color = TmsColor.OnSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                urls.forEach { url ->
                    Box {
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier =
                                Modifier
                                    .width(96.dp)
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                        )
                        Box(
                            modifier =
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(TmsColor.Error)
                                    .clickable(onClick = { onRequestDelete(url) }),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.instructor_profile_cert_delete_cd),
                                tint = TmsColor.OnPrimary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
                Box(
                    modifier =
                        Modifier
                            .width(96.dp)
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(TmsColor.SurfaceLow)
                            .clickable(
                                enabled = !isBusy && urls.size < 8,
                                onClick = onAdd,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = TmsColor.Primary,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.instructor_profile_certs_upload),
                            tint = TmsColor.Primary,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ProfileEditDialogs(
    openDialog: ProfileEditDialog,
    profile: InstructorProfile,
    regions: List<Region>,
    isSaving: Boolean,
    saveError: UiText?,
    onDismiss: () -> Unit,
    onSaveDisplayName: (String) -> Unit,
    onSaveBio: (String?) -> Unit,
    onSaveDiscipline: (Discipline) -> Unit,
    onSaveLevels: (List<Int>) -> Unit,
    onSaveResorts: (List<String>) -> Unit,
    onSaveCertifications: (List<String>, String?) -> Unit,
    onSaveLanguages: (List<String>) -> Unit,
    onSavePricing: (Int?, Int?) -> Unit,
    onSaveServices: (Boolean, Boolean) -> Unit,
) {
    when (openDialog) {
        ProfileEditDialog.None -> Unit
        ProfileEditDialog.DisplayName -> {
            var text by remember(profile.displayName, openDialog) { mutableStateOf(profile.displayName) }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.instructor_wizard_step5_name_label)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        saveError?.let { err ->
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = err.asString(),
                                color = TmsColor.Error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val t = text.trim()
                            if (t.isNotEmpty() && t.length <= MAX_DISPLAY_NAME_LENGTH) {
                                onSaveDisplayName(t)
                            }
                        },
                        enabled = !isSaving && text.trim().isNotEmpty() && text.trim().length <= MAX_DISPLAY_NAME_LENGTH,
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.instructor_profile_edit_dialog_save))
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss, enabled = !isSaving) {
                        Text(stringResource(R.string.instructor_profile_edit_dialog_cancel))
                    }
                },
            )
        }
        ProfileEditDialog.Bio -> {
            var text by remember(profile.bio, openDialog) { mutableStateOf(profile.bio.orEmpty()) }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.instructor_profile_bio_label)) },
                text = {
                    Column(modifier = Modifier.heightIn(max = 360.dp)) {
                        OutlinedTextField(
                            value = text,
                            onValueChange = { if (it.length <= 2000) text = it },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                        )
                        saveError?.let { err ->
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = err.asString(),
                                color = TmsColor.Error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val t = text.trim()
                            onSaveBio(if (t.isEmpty()) null else t)
                        },
                        enabled = !isSaving,
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.instructor_profile_edit_dialog_save))
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss, enabled = !isSaving) {
                        Text(stringResource(R.string.instructor_profile_edit_dialog_cancel))
                    }
                },
            )
        }
        ProfileEditDialog.Discipline -> {
            var selected by remember(profile.discipline, openDialog) { mutableStateOf(profile.discipline) }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.instructor_profile_discipline_label)) },
                text = {
                    Column {
                        DisciplineRadioRow(
                            label = stringResource(R.string.common_discipline_ski),
                            selected = selected == Discipline.Ski,
                            onClick = { selected = Discipline.Ski },
                        )
                        DisciplineRadioRow(
                            label = stringResource(R.string.common_discipline_snowboard),
                            selected = selected == Discipline.Snowboard,
                            onClick = { selected = Discipline.Snowboard },
                        )
                        DisciplineRadioRow(
                            label = stringResource(R.string.common_discipline_both),
                            selected = selected == Discipline.Both,
                            onClick = { selected = Discipline.Both },
                        )
                        saveError?.let { err ->
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = err.asString(),
                                color = TmsColor.Error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { onSaveDiscipline(selected) },
                        enabled = !isSaving,
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.instructor_profile_edit_dialog_save))
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss, enabled = !isSaving) {
                        Text(stringResource(R.string.instructor_profile_edit_dialog_cancel))
                    }
                },
            )
        }
        ProfileEditDialog.Levels -> {
            var levels by remember(profile.teachableLevels, openDialog) {
                mutableStateOf(profile.teachableLevels.toSet())
            }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.instructor_profile_levels_label)) },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            (0..4).forEach { lv ->
                                val sel = lv in levels
                                FilterChip(
                                    selected = sel,
                                    onClick = {
                                        levels =
                                            if (sel) levels - lv else levels + lv
                                    },
                                    label = {
                                        Text(
                                            stringResource(
                                                R.string.explore_card_skill_level_fmt,
                                                lv.toString(),
                                            ),
                                        )
                                    },
                                )
                            }
                        }
                        saveError?.let { err ->
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = err.asString(),
                                color = TmsColor.Error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { onSaveLevels(levels.toList()) },
                        enabled = !isSaving && levels.isNotEmpty(),
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.instructor_profile_edit_dialog_save))
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss, enabled = !isSaving) {
                        Text(stringResource(R.string.instructor_profile_edit_dialog_cancel))
                    }
                },
            )
        }
        ProfileEditDialog.Resorts -> {
            var selectedIds by remember(profile.resortIds, openDialog) {
                mutableStateOf(profile.resortIds.toSet())
            }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.instructor_profile_resorts_label)) },
                text = {
                    Column(modifier = Modifier.heightIn(max = 400.dp)) {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            ResortSelector(
                                regions = regions,
                                selectedResortIds = selectedIds,
                                allRegionsSelected = false,
                                onResortToggle = { id ->
                                    selectedIds =
                                        if (id in selectedIds) selectedIds - id else selectedIds + id
                                },
                                onPrefectureToggle = { pref ->
                                    val idsInPref =
                                        regions
                                            .filter { (it.prefectureEn ?: "") == pref }
                                            .flatMap { r -> r.resorts.map { it.id } }
                                            .toSet()
                                    if (idsInPref.isNotEmpty()) {
                                        val allSel = idsInPref.all { it in selectedIds }
                                        selectedIds =
                                            if (allSel) selectedIds - idsInPref else selectedIds + idsInPref
                                    }
                                },
                            )
                        }
                        saveError?.let { err ->
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = err.asString(),
                                color = TmsColor.Error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { onSaveResorts(selectedIds.toList()) },
                        enabled = !isSaving && selectedIds.isNotEmpty(),
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.instructor_profile_edit_dialog_save))
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss, enabled = !isSaving) {
                        Text(stringResource(R.string.instructor_profile_edit_dialog_cancel))
                    }
                },
            )
        }
        ProfileEditDialog.Certifications -> {
            var certs by remember(profile.certifications, profile.certificationOther, openDialog) {
                mutableStateOf(
                    buildSet {
                        addAll(profile.certifications)
                        if (!profile.certificationOther.isNullOrBlank()) {
                            add("other")
                        }
                    },
                )
            }
            var other by remember(profile.certificationOther, openDialog) {
                mutableStateOf(profile.certificationOther.orEmpty())
            }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.instructor_profile_certs_label)) },
                text = {
                    Column(modifier = Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState())) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            certificationOptionIds.forEach { id ->
                                val sel = id in certs
                                FilterChip(
                                    selected = sel,
                                    onClick = {
                                        certs =
                                            if (sel) certs - id else certs + id
                                    },
                                    label = { Text(stringResource(certificationLabelRes(id))) },
                                )
                            }
                        }
                        if ("other" in certs) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = other,
                                onValueChange = { other = it },
                                label = { Text(stringResource(R.string.instructor_wizard_step4_other_placeholder)) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        saveError?.let { err ->
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = err.asString(),
                                color = TmsColor.Error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onSaveCertifications(certs.toList(), other)
                        },
                        enabled = !isSaving,
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.instructor_profile_edit_dialog_save))
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss, enabled = !isSaving) {
                        Text(stringResource(R.string.instructor_profile_edit_dialog_cancel))
                    }
                },
            )
        }
        ProfileEditDialog.Languages -> {
            var langs by remember(profile.languages, openDialog) {
                mutableStateOf(profile.languages.toSet())
            }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.instructor_profile_languages_label)) },
                text = {
                    Column {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf("zh", "en", "ja").forEach { code ->
                                val sel = code in langs
                                FilterChip(
                                    selected = sel,
                                    onClick = {
                                        langs =
                                            if (sel) langs - code else langs + code
                                    },
                                    label = { Text(languageLabel(code)) },
                                )
                            }
                        }
                        saveError?.let { err ->
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = err.asString(),
                                color = TmsColor.Error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { onSaveLanguages(langs.toList()) },
                        enabled = !isSaving && langs.isNotEmpty(),
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.instructor_profile_edit_dialog_save))
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss, enabled = !isSaving) {
                        Text(stringResource(R.string.instructor_profile_edit_dialog_cancel))
                    }
                },
            )
        }
        ProfileEditDialog.Pricing -> {
            var halfText by remember(profile.priceHalfDay, openDialog) {
                mutableStateOf(profile.priceHalfDay?.toString().orEmpty())
            }
            var fullText by remember(profile.priceFullDay, openDialog) {
                mutableStateOf(profile.priceFullDay?.toString().orEmpty())
            }
            val half = halfText.toIntOrNull()
            val full = fullText.toIntOrNull()
            val warn =
                half != null &&
                    full != null &&
                    full < half
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.instructor_profile_pricing_label)) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = halfText,
                            onValueChange = { halfText = it.filter { ch -> ch.isDigit() } },
                            label = { Text(stringResource(R.string.instructor_wizard_step7_half_day_label)) },
                            prefix = { Text(stringResource(R.string.instructor_wizard_step7_currency)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = fullText,
                            onValueChange = { fullText = it.filter { ch -> ch.isDigit() } },
                            label = { Text(stringResource(R.string.instructor_wizard_step7_full_day_label)) },
                            prefix = { Text(stringResource(R.string.instructor_wizard_step7_currency)) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        if (warn) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.instructor_wizard_step7_warning_full_lt_half),
                                color = TmsColor.Warning,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        saveError?.let { err ->
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = err.asString(),
                                color = TmsColor.Error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onSavePricing(
                                halfText.toIntOrNull(),
                                fullText.toIntOrNull(),
                            )
                        },
                        enabled = !isSaving,
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.instructor_profile_edit_dialog_save))
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss, enabled = !isSaving) {
                        Text(stringResource(R.string.instructor_profile_edit_dialog_cancel))
                    }
                },
            )
        }
        ProfileEditDialog.Services -> {
            var transport by remember(profile.offersTransport, openDialog) {
                mutableStateOf(profile.offersTransport)
            }
            var photo by remember(profile.offersPhotography, openDialog) {
                mutableStateOf(profile.offersPhotography)
            }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.instructor_profile_services_label)) },
                text = {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.instructor_profile_offers_transport),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TmsColor.OnSurface,
                                )
                                Text(
                                    text = stringResource(R.string.instructor_wizard_step8_transport_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TmsColor.OnSurfaceVariant,
                                )
                            }
                            Switch(checked = transport, onCheckedChange = { transport = it })
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.instructor_profile_offers_photography),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TmsColor.OnSurface,
                                )
                                Text(
                                    text = stringResource(R.string.instructor_wizard_step8_photography_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TmsColor.OnSurfaceVariant,
                                )
                            }
                            Switch(checked = photo, onCheckedChange = { photo = it })
                        }
                        saveError?.let { err ->
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = err.asString(),
                                color = TmsColor.Error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { onSaveServices(transport, photo) },
                        enabled = !isSaving,
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.instructor_profile_edit_dialog_save))
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss, enabled = !isSaving) {
                        Text(stringResource(R.string.instructor_profile_edit_dialog_cancel))
                    }
                },
            )
        }
    }
}

private fun UiText.toMessage(context: Context): String =
    when (this) {
        is UiText.StringResource -> context.getString(resId, *args.toTypedArray())
        is UiText.DynamicString -> value
    }

@Composable
private fun DisciplineRadioRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    role = Role.RadioButton,
                )
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = TmsColor.OnSurface)
    }
}

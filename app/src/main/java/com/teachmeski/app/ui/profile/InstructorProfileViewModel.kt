package com.teachmeski.app.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.InstructorProfile
import com.teachmeski.app.domain.model.Region
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.InstructorRepository
import com.teachmeski.app.domain.repository.ResortRepository
import com.teachmeski.app.domain.repository.UserRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.math.max
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MAX_ORIGINAL_BYTES = 10 * 1024 * 1024
private const val MAX_DIMENSION = 1024
private const val TARGET_UPLOAD_BYTES = 1024 * 1024
private const val MAX_CERTIFICATE_IMAGES = 8

enum class ProfileEditDialog {
    None,
    DisplayName,
    Bio,
    Discipline,
    Levels,
    Resorts,
    Certifications,
    Languages,
    Pricing,
    Services,
}

data class InstructorProfileUiState(
    val profile: InstructorProfile? = null,
    val regions: List<Region> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: UiText? = null,
    val saveError: UiText? = null,
    val saveSuccess: Boolean = false,
    val openDialog: ProfileEditDialog = ProfileEditDialog.None,
    val isUploadingCert: Boolean = false,
    val uploadCertError: UiText? = null,
)

@HiltViewModel
class InstructorProfileViewModel @Inject constructor(
    private val instructorRepository: InstructorRepository,
    private val resortRepository: ResortRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InstructorProfileUiState())
    val uiState: StateFlow<InstructorProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = it.profile == null,
                    error = null,
                    isSaving = false,
                )
            }
            when (val profileResult = instructorRepository.getMyProfile()) {
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = profileResult.message,
                            profile = null,
                        )
                    }
                }
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    val regions =
                        when (val r = resortRepository.getResortsWithRegions()) {
                            is Resource.Success -> r.data
                            else -> emptyList()
                        }
                    _uiState.update {
                        it.copy(
                            profile = profileResult.data,
                            regions = regions,
                            isLoading = false,
                            error = null,
                        )
                    }
                }
            }
        }
    }

    fun openDialog(dialog: ProfileEditDialog) {
        _uiState.update { it.copy(openDialog = dialog, saveError = null) }
    }

    fun closeDialog() {
        _uiState.update {
            it.copy(
                openDialog = ProfileEditDialog.None,
                saveError = null,
            )
        }
    }

    fun saveUpdates(updates: Map<String, Any?>) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    saveError = null,
                    saveSuccess = false,
                )
            }
            when (val result = instructorRepository.updateProfile(updates)) {
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveError = result.message,
                        )
                    }
                }
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            openDialog = ProfileEditDialog.None,
                            saveSuccess = true,
                        )
                    }
                    load()
                }
            }
        }
    }

    fun toggleAccepting(isAccepting: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }
            when (val result = instructorRepository.toggleAcceptingRequests(isAccepting)) {
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isSaving = false, saveError = result.message)
                    }
                }
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    load()
                }
            }
        }
    }

    fun uploadCertificate(bytes: ByteArray, contentType: String) {
        viewModelScope.launch {
            val count = _uiState.value.profile?.certificateUrls?.size ?: 0
            if (count >= MAX_CERTIFICATE_IMAGES) {
                _uiState.update {
                    it.copy(
                        uploadCertError = UiText.StringResource(R.string.instructor_profile_cert_limit),
                    )
                }
                return@launch
            }
            _uiState.update { it.copy(isUploadingCert = true, uploadCertError = null) }
            when (val result = instructorRepository.uploadCertificate(bytes, contentType)) {
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isUploadingCert = false, uploadCertError = result.message)
                    }
                }
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    _uiState.update { it.copy(isUploadingCert = false) }
                    load()
                }
            }
        }
    }

    fun deleteCertificate(url: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingCert = true, uploadCertError = null) }
            when (val result = instructorRepository.deleteCertificate(url)) {
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isUploadingCert = false, uploadCertError = result.message)
                    }
                }
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    _uiState.update { it.copy(isUploadingCert = false) }
                    load()
                }
            }
        }
    }

    fun clearUploadCertError() {
        _uiState.update { it.copy(uploadCertError = null) }
    }

    fun clearSaveError() {
        _uiState.update { it.copy(saveError = null) }
    }

    fun uploadAvatar(context: Context, uri: Uri) {
        val userId = authRepository.currentUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }

            val rawBytes =
                try {
                    context.contentResolver.openInputStream(uri)?.readBytes()
                } catch (_: Exception) {
                    null
                }

            if (rawBytes == null || rawBytes.isEmpty()) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveError = UiText.StringResource(R.string.account_avatar_upload_error),
                    )
                }
                return@launch
            }

            if (rawBytes.size > MAX_ORIGINAL_BYTES) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveError = UiText.StringResource(R.string.account_avatar_too_large),
                    )
                }
                return@launch
            }

            val compressed = compressImage(rawBytes)

            when (val upload = userRepository.uploadAvatar(userId, compressed, "jpg")) {
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isSaving = false, saveError = upload.message)
                    }
                }
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    when (
                        val sync =
                            instructorRepository.updateProfile(
                                mapOf("avatar_url" to upload.data),
                            )
                    ) {
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(isSaving = false, saveError = sync.message)
                            }
                        }
                        is Resource.Loading -> Unit
                        is Resource.Success -> {
                            _uiState.update { it.copy(isSaving = false) }
                            load()
                        }
                    }
                }
            }
        }
    }

    fun consumeSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    private fun compressImage(rawBytes: ByteArray): ByteArray {
        val original = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size) ?: return rawBytes

        val w = original.width
        val h = original.height
        val maxSide = max(w, h)
        val scaled =
            if (maxSide > MAX_DIMENSION) {
                val ratio = MAX_DIMENSION.toFloat() / maxSide
                val newW = (w * ratio).toInt()
                val newH = (h * ratio).toInt()
                Bitmap.createScaledBitmap(original, newW, newH, true)
            } else {
                original
            }

        var quality = 85
        while (quality >= 40) {
            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
            val bytes = out.toByteArray()
            if (bytes.size <= TARGET_UPLOAD_BYTES) return bytes
            quality -= 10
        }

        val fallback = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 40, fallback)
        return fallback.toByteArray()
    }
}

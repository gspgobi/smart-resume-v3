package com.nithra.nithraresume.ui.section.child

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.SectionChild4
import com.nithra.nithraresume.data.repository.SectionChildRepository
import com.nithra.nithraresume.utils.SrDir
import com.nithra.nithraresume.utils.SrImagePrefix
import com.nithra.nithraresume.utils.SrImageSuffix
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed interface Child4SignatureUiState {
    data object Loading : Child4SignatureUiState
    data object Ready : Child4SignatureUiState
    data class Error(val message: String) : Child4SignatureUiState
}

@HiltViewModel
class SectionChild4SignatureViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val sectionChildRepository: SectionChildRepository
) : ViewModel() {

    val sectionHeadAddedId: Int = checkNotNull(savedStateHandle["sectionHeadAddedId"])

    private val _uiState = MutableStateFlow<Child4SignatureUiState>(Child4SignatureUiState.Loading)
    val uiState: StateFlow<Child4SignatureUiState> = _uiState.asStateFlow()

    private val _child4 = MutableStateFlow<SectionChild4?>(null)
    val child4: StateFlow<SectionChild4?> = _child4.asStateFlow()

    init {
        viewModelScope.launch {
            _child4.value = sectionChildRepository.getChild4Once(sectionHeadAddedId)
            _uiState.value = Child4SignatureUiState.Ready
        }
    }

    fun saveSignatureImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val existing = ensureChild4Exists()
                val imageFile = signatureFile(existing.id)
                imageFile.parentFile?.mkdirs()
                context.contentResolver.openInputStream(uri)?.use { input ->
                    imageFile.outputStream().use { output -> input.copyTo(output) }
                }
                val updated = existing.copy(
                    signatureImagePath = imageFile.absolutePath,
                    isSignatureImageEnable = true
                )
                sectionChildRepository.updateChild4(updated)
                _child4.value = updated
            } catch (e: Exception) {
                _uiState.value = Child4SignatureUiState.Error("Failed to save signature")
            }
        }
    }

    fun saveDrawnSignature(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                val existing = ensureChild4Exists()
                val imageFile = signatureFile(existing.id)
                imageFile.parentFile?.mkdirs()
                imageFile.outputStream().use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                val updated = existing.copy(
                    signatureImagePath = imageFile.absolutePath,
                    isSignatureImageEnable = true
                )
                sectionChildRepository.updateChild4(updated)
                _child4.value = updated
            } catch (e: Exception) {
                _uiState.value = Child4SignatureUiState.Error("Failed to save signature")
            }
        }
    }

    fun toggleSignatureEnable() {
        viewModelScope.launch {
            val existing = _child4.value ?: return@launch
            val updated = existing.copy(isSignatureImageEnable = !existing.isSignatureImageEnable)
            sectionChildRepository.updateChild4(updated)
            _child4.value = updated
        }
    }

    fun deleteSignatureImage() {
        viewModelScope.launch {
            val existing = _child4.value ?: return@launch
            runCatching { File(existing.signatureImagePath).delete() }
            val updated = existing.copy(signatureImagePath = "", isSignatureImageEnable = false)
            sectionChildRepository.updateChild4(updated)
            _child4.value = updated
        }
    }

    private suspend fun ensureChild4Exists(): SectionChild4 {
        val existing = _child4.value
        if (existing != null && existing.id > 0) return existing
        sectionChildRepository.saveChild4(
            SectionChild4(
                id = 0, sectionHeadAddedId = sectionHeadAddedId,
                declarationContent = "", declarationContentBulletType = "",
                date = "", dateDateFormat = "", place = "",
                signatureImagePath = "", isSignatureImageEnable = false
            )
        )
        val loaded = sectionChildRepository.getChild4Once(sectionHeadAddedId)!!
        _child4.value = loaded
        return loaded
    }

    private fun signatureFile(sc4Id: Int): File {
        val dir = File(context.getExternalFilesDir(null), SrDir.SIGNATURE)
        return File(dir, "${SrImagePrefix.SIGNATURE}$sc4Id${SrImageSuffix.JPG}")
    }
}

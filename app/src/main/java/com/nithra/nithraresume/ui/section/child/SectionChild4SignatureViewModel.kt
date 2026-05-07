package com.nithra.nithraresume.ui.section.child

import android.content.Context
import android.graphics.Bitmap
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
    data object Saved : Child4SignatureUiState
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

    init {
        viewModelScope.launch {
            _child4.value = sectionChildRepository.getChild4Once(sectionHeadAddedId)
            _uiState.value = Child4SignatureUiState.Ready
        }
    }

    fun saveDrawnSignature(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                val existing = ensureChild4Exists()
                existing.signatureImagePath.takeIf { it.isNotEmpty() }
                    ?.let { runCatching { File(it).delete() } }
                val imageFile = signatureFile()
                imageFile.outputStream().use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                sectionChildRepository.updateChild4(
                    existing.copy(
                        signatureImagePath = imageFile.absolutePath,
                        isSignatureImageEnable = true
                    )
                )
                _uiState.value = Child4SignatureUiState.Saved
            } catch (e: Exception) {
                _uiState.value = Child4SignatureUiState.Error("Failed to save signature")
            }
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

    private fun signatureFile(): File {
        val dir = File(context.getExternalFilesDir(null), SrDir.SIGNATURE)
        dir.mkdirs()
        return File(dir, "${SrImagePrefix.SIGNATURE}${System.currentTimeMillis()}${SrImageSuffix.JPG}")
    }
}

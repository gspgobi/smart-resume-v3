package com.nithra.nithraresume.ui.section.child

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.SectionChild4
import com.nithra.nithraresume.data.model.SectionHeadAdded
import com.nithra.nithraresume.data.repository.SectionChildRepository
import com.nithra.nithraresume.data.repository.SectionHeadRepository
import com.nithra.nithraresume.utils.AnalyticsManager
import com.nithra.nithraresume.utils.SrDir
import com.nithra.nithraresume.utils.SrImagePrefix
import com.nithra.nithraresume.utils.SrImageSuffix
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed interface Child4UiState {
    data object Loading : Child4UiState
    data object Ready : Child4UiState
    data object Saved : Child4UiState
    data class Error(val message: String) : Child4UiState
}

@HiltViewModel
class SectionChild4ViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val sectionHeadRepository: SectionHeadRepository,
    private val sectionChildRepository: SectionChildRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    val sectionHeadAddedId: Int = checkNotNull(savedStateHandle["sectionHeadAddedId"])

    private val _uiState = MutableStateFlow<Child4UiState>(Child4UiState.Loading)
    val uiState: StateFlow<Child4UiState> = _uiState.asStateFlow()

    private val _sha = MutableStateFlow<SectionHeadAdded?>(null)
    val sha: StateFlow<SectionHeadAdded?> = _sha.asStateFlow()

    val child4: StateFlow<SectionChild4?> = sectionChildRepository
        .getChild4(sectionHeadAddedId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun resetState() { _uiState.value = Child4UiState.Ready }
    fun onClearAll() { analyticsManager.logSc4ClearAll() }

    init {
        viewModelScope.launch {
            _sha.value = sectionHeadRepository.getAddedById(sectionHeadAddedId)
            // Wait for the first Room DB emission so child4 stateIn is populated before Ready
            sectionChildRepository.getChild4(sectionHeadAddedId).first()
            _uiState.value = Child4UiState.Ready
        }
    }

    fun save(
        title: String,
        declarationContent: String,
        bulletType: String,
        date: String,
        dateDateFormat: String,
        place: String
    ) {
        viewModelScope.launch {
            try {
                sectionHeadRepository.updateAddedTitle(sectionHeadAddedId, title)
                val existing = child4.value
                if (existing != null && existing.id > 0) {
                    sectionChildRepository.updateChild4(
                        existing.copy(
                            declarationContent = declarationContent,
                            declarationContentBulletType = bulletType,
                            date = date,
                            dateDateFormat = dateDateFormat,
                            place = place
                        )
                    )
                } else {
                    sectionChildRepository.saveChild4(
                        SectionChild4(
                            id = 0,
                            sectionHeadAddedId = sectionHeadAddedId,
                            declarationContent = declarationContent,
                            declarationContentBulletType = bulletType,
                            date = date,
                            dateDateFormat = dateDateFormat,
                            place = place,
                            signatureImagePath = "",
                            isSignatureImageEnable = false
                        )
                    )
                }
                _sha.value = sectionHeadRepository.getAddedById(sectionHeadAddedId)
                analyticsManager.logSc4Save()
                _uiState.value = Child4UiState.Saved
            } catch (e: Exception) {
                _uiState.value = Child4UiState.Error(e.message ?: "Save failed")
            }
        }
    }

    fun saveSignatureFromUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val existing = ensureChild4Exists()
                existing.signatureImagePath.takeIf { it.isNotEmpty() }
                    ?.let { runCatching { File(it).delete() } }
                val imageFile = signatureFile()
                context.contentResolver.openInputStream(uri)?.use { input ->
                    imageFile.outputStream().use { output -> input.copyTo(output) }
                }
                sectionChildRepository.updateChild4(
                    existing.copy(
                        signatureImagePath = imageFile.absolutePath,
                        isSignatureImageEnable = true
                    )
                )
                analyticsManager.logSc4NewSignature()
            } catch (e: Exception) {
                _uiState.value = Child4UiState.Error("Failed to save signature")
            }
        }
    }

    fun deleteSignatureImage() {
        viewModelScope.launch {
            val existing = child4.value ?: return@launch
            runCatching { File(existing.signatureImagePath).delete() }
            sectionChildRepository.updateChild4(
                existing.copy(signatureImagePath = "", isSignatureImageEnable = false)
            )
            analyticsManager.logSc4DeleteSignature()
        }
    }

    private suspend fun ensureChild4Exists(): SectionChild4 {
        val existing = child4.value
        if (existing != null && existing.id > 0) return existing
        sectionChildRepository.saveChild4(
            SectionChild4(
                id = 0, sectionHeadAddedId = sectionHeadAddedId,
                declarationContent = "", declarationContentBulletType = "",
                date = "", dateDateFormat = "", place = "",
                signatureImagePath = "", isSignatureImageEnable = false
            )
        )
        return sectionChildRepository.getChild4Once(sectionHeadAddedId)!!
    }

    private fun signatureFile(): File {
        val dir = File(context.getExternalFilesDir(null), SrDir.SIGNATURE)
        dir.mkdirs()
        return File(dir, "${SrImagePrefix.SIGNATURE}${System.currentTimeMillis()}${SrImageSuffix.JPG}")
    }
}

package com.nithra.nithraresume.ui.section.child


import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.SectionChild1
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed interface Child1UiState {
    data object Loading : Child1UiState
    data object Ready : Child1UiState
    data object Saved : Child1UiState
    data class Error(val message: String) : Child1UiState
}

@HiltViewModel
class SectionChild1ViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val sectionHeadRepository: SectionHeadRepository,
    private val sectionChildRepository: SectionChildRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    val sectionHeadAddedId: Int = checkNotNull(savedStateHandle["sectionHeadAddedId"])

    private val _uiState = MutableStateFlow<Child1UiState>(Child1UiState.Loading)
    val uiState: StateFlow<Child1UiState> = _uiState.asStateFlow()

    private val _sha = MutableStateFlow<SectionHeadAdded?>(null)
    val sha: StateFlow<SectionHeadAdded?> = _sha.asStateFlow()

    private val _child1 = MutableStateFlow<SectionChild1?>(null)
    val child1: StateFlow<SectionChild1?> = _child1.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _sha.value = sectionHeadRepository.getAddedById(sectionHeadAddedId)
            _child1.value = sectionChildRepository.getChild1Once(sectionHeadAddedId)
            _uiState.value = Child1UiState.Ready
        }
    }

    // ── Save ──────────────────────────────────────────────────────────────────

    fun save(
        title: String,
        name: String,
        address: String,
        email: String,
        phone: String,
        gender: String,
        dob: String,
        dobDateFormat: String,
        nationality: String
    ) {
        viewModelScope.launch {
            try {
                // Update section head title
                sectionHeadRepository.updateAddedTitle(sectionHeadAddedId, title)

                val existing = _child1.value
                if (existing != null && existing.id > 0) {
                    sectionChildRepository.updateChild1(
                        existing.copy(
                            name = name, address = address, email = email,
                            phone = phone, gender = gender, dob = dob,
                            dobDateFormat = dobDateFormat, nationality = nationality
                        )
                    )
                } else {
                    val newId = sectionChildRepository.saveChild1(
                        SectionChild1(
                            id = 0,
                            sectionHeadAddedId = sectionHeadAddedId,
                            name = name, address = address, email = email,
                            phone = phone, gender = gender, dob = dob,
                            dobDateFormat = dobDateFormat, nationality = nationality,
                            userImagePath = "", isUserImageEnable = false
                        )
                    )
                    _child1.value = sectionChildRepository.getChild1Once(sectionHeadAddedId)
                }

                // Reload sha to reflect updated title
                _sha.value = sectionHeadRepository.getAddedById(sectionHeadAddedId)
                analyticsManager.logSc1Save()
                _uiState.value = Child1UiState.Saved
            } catch (e: Exception) {
                _uiState.value = Child1UiState.Error(e.message ?: "Save failed")
            }
        }
    }

    fun resetState() { _uiState.value = Child1UiState.Ready }
    fun onClearAll() { analyticsManager.logSc1ClearAll() }

    // ── Image ─────────────────────────────────────────────────────────────────

    fun saveImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val existing = ensureChild1Exists()
                val imageFile = imageFile(existing.id)
                imageFile.parentFile?.mkdirs()
                context.contentResolver.openInputStream(uri)?.use { input ->
                    imageFile.outputStream().use { output -> input.copyTo(output) }
                }
                val updated = existing.copy(
                    userImagePath = imageFile.absolutePath,
                    isUserImageEnable = true
                )
                sectionChildRepository.updateChild1(updated)
                _child1.value = updated
                analyticsManager.logSc1BrowseImage()
            } catch (e: Exception) {
                _uiState.value = Child1UiState.Error("Failed to save image")
            }
        }
    }

    fun deleteImage() {
        viewModelScope.launch {
            val existing = _child1.value ?: return@launch
            runCatching { File(existing.userImagePath).delete() }
            val updated = existing.copy(userImagePath = "", isUserImageEnable = false)
            sectionChildRepository.updateChild1(updated)
            _child1.value = updated
            analyticsManager.logSc1DeleteImage()
        }
    }

    private suspend fun ensureChild1Exists(): SectionChild1 {
        val existing = _child1.value
        if (existing != null && existing.id > 0) return existing
        sectionChildRepository.saveChild1(
            SectionChild1(
                id = 0, sectionHeadAddedId = sectionHeadAddedId,
                name = "", address = "", email = "", phone = "",
                gender = "", dob = "", dobDateFormat = "",
                nationality = "", userImagePath = "", isUserImageEnable = false
            )
        )
        val loaded = sectionChildRepository.getChild1Once(sectionHeadAddedId)!!
        _child1.value = loaded
        return loaded
    }

    private fun imageFile(sc1Id: Int): File {
        val dir = File(context.getExternalFilesDir(null), SrDir.USER_IMAGE)
        return File(dir, "${SrImagePrefix.USER_IMAGE}$sc1Id${SrImageSuffix.JPG}")
    }
}

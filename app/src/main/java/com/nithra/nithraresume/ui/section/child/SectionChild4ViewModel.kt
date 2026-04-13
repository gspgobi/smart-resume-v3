package com.nithra.nithraresume.ui.section.child

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.SectionChild4
import com.nithra.nithraresume.data.model.SectionHeadAdded
import com.nithra.nithraresume.data.repository.SectionChildRepository
import com.nithra.nithraresume.data.repository.SectionHeadRepository
import com.nithra.nithraresume.utils.BULLET_NONE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    private val sectionHeadRepository: SectionHeadRepository,
    private val sectionChildRepository: SectionChildRepository
) : ViewModel() {

    val sectionHeadAddedId: Int = checkNotNull(savedStateHandle["sectionHeadAddedId"])

    private val _uiState = MutableStateFlow<Child4UiState>(Child4UiState.Loading)
    val uiState: StateFlow<Child4UiState> = _uiState.asStateFlow()

    private val _sha = MutableStateFlow<SectionHeadAdded?>(null)
    val sha: StateFlow<SectionHeadAdded?> = _sha.asStateFlow()

    private val _child4 = MutableStateFlow<SectionChild4?>(null)
    val child4: StateFlow<SectionChild4?> = _child4.asStateFlow()

    fun resetState() { _uiState.value = Child4UiState.Ready }

    init {
        viewModelScope.launch {
            _sha.value = sectionHeadRepository.getAddedById(sectionHeadAddedId)
            _child4.value = sectionChildRepository.getChild4Once(sectionHeadAddedId)
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
                val existing = _child4.value
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
                    _child4.value = sectionChildRepository.getChild4Once(sectionHeadAddedId)
                }
                _sha.value = sectionHeadRepository.getAddedById(sectionHeadAddedId)
                _uiState.value = Child4UiState.Saved
            } catch (e: Exception) {
                _uiState.value = Child4UiState.Error(e.message ?: "Save failed")
            }
        }
    }
}

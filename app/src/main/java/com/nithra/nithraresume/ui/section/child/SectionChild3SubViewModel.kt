package com.nithra.nithraresume.ui.section.child

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.SectionChild3
import com.nithra.nithraresume.data.repository.SectionChildRepository
import com.nithra.nithraresume.utils.BULLET_NONE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface Child3SubUiState {
    data object Loading : Child3SubUiState
    data object Ready : Child3SubUiState
    data object Saved : Child3SubUiState
    data class Error(val message: String) : Child3SubUiState
}

@HiltViewModel
class SectionChild3SubViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sectionChildRepository: SectionChildRepository
) : ViewModel() {

    val sectionHeadAddedId: Int = checkNotNull(savedStateHandle["sectionHeadAddedId"])
    private val itemId: Int = savedStateHandle["itemId"] ?: -1

    private val _uiState = MutableStateFlow<Child3SubUiState>(Child3SubUiState.Loading)
    val uiState: StateFlow<Child3SubUiState> = _uiState.asStateFlow()

    private val _item = MutableStateFlow<SectionChild3?>(null)
    val item: StateFlow<SectionChild3?> = _item.asStateFlow()

    fun resetState() { _uiState.value = Child3SubUiState.Ready }

    init {
        viewModelScope.launch {
            if (itemId > 0) _item.value = sectionChildRepository.getChild3ById(itemId)
            _uiState.value = Child3SubUiState.Ready
        }
    }

    fun save(
        studyDegree: String,
        schoolName: String,
        subtitle: String,
        studyPeriod: String,
        concentrates: String,
        bulletType: String
    ) {
        viewModelScope.launch {
            try {
                val existing = _item.value
                if (existing != null && existing.id > 0) {
                    sectionChildRepository.updateChild3(
                        existing.copy(
                            studyDegree = studyDegree, schoolName = schoolName,
                            subtitle = subtitle, studyPeriod = studyPeriod,
                            concentrates = concentrates, concentratesBulletType = bulletType
                        )
                    )
                } else {
                    val nextPos = sectionChildRepository.getChild3Count(sectionHeadAddedId)
                    sectionChildRepository.insertChild3(
                        SectionChild3(
                            id = 0,
                            sectionHeadAddedId = sectionHeadAddedId,
                            indexPosition = nextPos,
                            studyDegree = studyDegree, schoolName = schoolName,
                            subtitle = subtitle, studyPeriod = studyPeriod,
                            concentrates = concentrates, concentratesBulletType = bulletType
                        )
                    )
                }
                _uiState.value = Child3SubUiState.Saved
            } catch (e: Exception) {
                _uiState.value = Child3SubUiState.Error(e.message ?: "Save failed")
            }
        }
    }
}
// update 141

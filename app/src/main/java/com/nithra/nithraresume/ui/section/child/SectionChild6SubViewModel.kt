package com.nithra.nithraresume.ui.section.child

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.SectionChild6
import com.nithra.nithraresume.data.repository.SectionChildRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface Child6SubUiState {
    data object Loading : Child6SubUiState
    data object Ready : Child6SubUiState
    data object Saved : Child6SubUiState
    data class Error(val message: String) : Child6SubUiState
}

@HiltViewModel
class SectionChild6SubViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sectionChildRepository: SectionChildRepository
) : ViewModel() {

    val sectionHeadAddedId: Int = checkNotNull(savedStateHandle["sectionHeadAddedId"])
    private val itemId: Int = savedStateHandle["itemId"] ?: -1

    private val _uiState = MutableStateFlow<Child6SubUiState>(Child6SubUiState.Loading)
    val uiState: StateFlow<Child6SubUiState> = _uiState.asStateFlow()

    private val _item = MutableStateFlow<SectionChild6?>(null)
    val item: StateFlow<SectionChild6?> = _item.asStateFlow()

    fun resetState() { _uiState.value = Child6SubUiState.Ready }

    init {
        viewModelScope.launch {
            if (itemId > 0) _item.value = sectionChildRepository.getChild6ById(itemId)
            _uiState.value = Child6SubUiState.Ready
        }
    }

    fun save(contentTitle: String, contentDetail: String) {
        viewModelScope.launch {
            try {
                val existing = _item.value
                if (existing != null && existing.id > 0) {
                    sectionChildRepository.updateChild6(
                        existing.copy(contentTitle = contentTitle, contentDetail = contentDetail)
                    )
                } else {
                    val nextPos = sectionChildRepository.getChild6Count(sectionHeadAddedId)
                    sectionChildRepository.insertChild6(
                        SectionChild6(
                            id = 0,
                            sectionHeadAddedId = sectionHeadAddedId,
                            indexPosition = nextPos,
                            contentTitle = contentTitle,
                            contentDetail = contentDetail
                        )
                    )
                }
                _uiState.value = Child6SubUiState.Saved
            } catch (e: Exception) {
                _uiState.value = Child6SubUiState.Error(e.message ?: "Save failed")
            }
        }
    }
}

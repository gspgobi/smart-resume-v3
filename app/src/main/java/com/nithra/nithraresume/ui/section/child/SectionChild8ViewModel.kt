package com.nithra.nithraresume.ui.section.child

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.SectionChild8
import com.nithra.nithraresume.data.model.SectionHeadAdded
import com.nithra.nithraresume.data.repository.SectionChildRepository
import com.nithra.nithraresume.data.repository.SectionHeadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface Child8UiState {
    data object Loading : Child8UiState
    data object Ready : Child8UiState
    data object Saved : Child8UiState
    data class Error(val message: String) : Child8UiState
}

@HiltViewModel
class SectionChild8ViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sectionHeadRepository: SectionHeadRepository,
    private val sectionChildRepository: SectionChildRepository
) : ViewModel() {

    val sectionHeadAddedId: Int = checkNotNull(savedStateHandle["sectionHeadAddedId"])

    private val _uiState = MutableStateFlow<Child8UiState>(Child8UiState.Loading)
    val uiState: StateFlow<Child8UiState> = _uiState.asStateFlow()

    private val _sha = MutableStateFlow<SectionHeadAdded?>(null)
    val sha: StateFlow<SectionHeadAdded?> = _sha.asStateFlow()

    private val _child8 = MutableStateFlow<SectionChild8?>(null)
    val child8: StateFlow<SectionChild8?> = _child8.asStateFlow()

    fun resetState() { _uiState.value = Child8UiState.Ready }

    init {
        viewModelScope.launch {
            _sha.value = sectionHeadRepository.getAddedById(sectionHeadAddedId)
            _child8.value = sectionChildRepository.getChild8Once(sectionHeadAddedId)
            _uiState.value = Child8UiState.Ready
        }
    }

    fun save(
        title: String,
        date: String,
        dateDateFormat: String,
        address: String,
        content: String
    ) {
        viewModelScope.launch {
            try {
                sectionHeadRepository.updateAddedTitle(sectionHeadAddedId, title)
                val existing = _child8.value
                if (existing != null && existing.id > 0) {
                    sectionChildRepository.updateChild8(
                        existing.copy(
                            date = date, dateDateFormat = dateDateFormat,
                            address = address, content = content
                        )
                    )
                } else {
                    sectionChildRepository.saveChild8(
                        SectionChild8(
                            id = 0,
                            sectionHeadAddedId = sectionHeadAddedId,
                            date = date, dateDateFormat = dateDateFormat,
                            address = address, content = content
                        )
                    )
                }
                _sha.value = sectionHeadRepository.getAddedById(sectionHeadAddedId)
                _uiState.value = Child8UiState.Saved
            } catch (e: Exception) {
                _uiState.value = Child8UiState.Error(e.message ?: "Save failed")
            }
        }
    }
}

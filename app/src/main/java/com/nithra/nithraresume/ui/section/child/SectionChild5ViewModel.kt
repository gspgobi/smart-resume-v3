package com.nithra.nithraresume.ui.section.child

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.SectionChild5
import com.nithra.nithraresume.data.model.SectionHeadAdded
import com.nithra.nithraresume.data.repository.SectionChildRepository
import com.nithra.nithraresume.data.repository.SectionHeadRepository
import com.nithra.nithraresume.utils.AnalyticsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface Child5UiState {
    data object Loading : Child5UiState
    data object Ready : Child5UiState
    data object Saved : Child5UiState
    data class Error(val message: String) : Child5UiState
}

@HiltViewModel
class SectionChild5ViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sectionHeadRepository: SectionHeadRepository,
    private val sectionChildRepository: SectionChildRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    val sectionHeadAddedId: Int = checkNotNull(savedStateHandle["sectionHeadAddedId"])

    private val _uiState = MutableStateFlow<Child5UiState>(Child5UiState.Loading)
    val uiState: StateFlow<Child5UiState> = _uiState.asStateFlow()

    private val _sha = MutableStateFlow<SectionHeadAdded?>(null)
    val sha: StateFlow<SectionHeadAdded?> = _sha.asStateFlow()

    private val _child5 = MutableStateFlow<SectionChild5?>(null)
    val child5: StateFlow<SectionChild5?> = _child5.asStateFlow()

    fun resetState() { _uiState.value = Child5UiState.Ready }
    fun onClearAll() { analyticsManager.logSc5ClearAll() }

    init {
        viewModelScope.launch {
            _sha.value = sectionHeadRepository.getAddedById(sectionHeadAddedId)
            _child5.value = sectionChildRepository.getChild5Once(sectionHeadAddedId)
            _uiState.value = Child5UiState.Ready
        }
    }

    fun save(title: String, content: String, bulletType: String) {
        viewModelScope.launch {
            try {
                sectionHeadRepository.updateAddedTitle(sectionHeadAddedId, title)
                val existing = _child5.value
                if (existing != null && existing.id > 0) {
                    sectionChildRepository.updateChild5(
                        existing.copy(content = content, contentBulletType = bulletType)
                    )
                } else {
                    sectionChildRepository.saveChild5(
                        SectionChild5(
                            id = 0,
                            sectionHeadAddedId = sectionHeadAddedId,
                            content = content,
                            contentBulletType = bulletType
                        )
                    )
                }
                _sha.value = sectionHeadRepository.getAddedById(sectionHeadAddedId)
                analyticsManager.logSc5Save()
                _uiState.value = Child5UiState.Saved
            } catch (e: Exception) {
                _uiState.value = Child5UiState.Error(e.message ?: "Save failed")
            }
        }
    }
}

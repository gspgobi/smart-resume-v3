package com.nithra.nithraresume.ui.section.child


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.SectionChild7
import com.nithra.nithraresume.data.repository.SectionChildRepository
import com.nithra.nithraresume.utils.AnalyticsManager
import com.nithra.nithraresume.utils.BULLET_NONE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface Child7SubUiState {
    data object Loading : Child7SubUiState
    data object Ready : Child7SubUiState
    data object Saved : Child7SubUiState
    data class Error(val message: String) : Child7SubUiState
}

@HiltViewModel
class SectionChild7SubViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sectionChildRepository: SectionChildRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    val sectionHeadAddedId: Int = checkNotNull(savedStateHandle["sectionHeadAddedId"])
    private val itemId: Int = savedStateHandle["itemId"] ?: -1

    private val _uiState = MutableStateFlow<Child7SubUiState>(Child7SubUiState.Loading)
    val uiState: StateFlow<Child7SubUiState> = _uiState.asStateFlow()

    private val _item = MutableStateFlow<SectionChild7?>(null)
    val item: StateFlow<SectionChild7?> = _item.asStateFlow()

    fun resetState() { _uiState.value = Child7SubUiState.Ready }
    fun onClearAll() { analyticsManager.logSc7SubClearAll() }

    init {
        viewModelScope.launch {
            if (itemId > 0) _item.value = sectionChildRepository.getChild7ById(itemId)
            _uiState.value = Child7SubUiState.Ready
        }
    }

    fun save(
        contentTitle: String,
        contentSubtitle: String,
        contentDetail: String,
        bulletType: String
    ) {
        viewModelScope.launch {
            try {
                val existing = _item.value
                if (existing != null && existing.id > 0) {
                    sectionChildRepository.updateChild7(
                        existing.copy(
                            contentTitle = contentTitle,
                            contentSubtitle = contentSubtitle,
                            contentDetail = contentDetail,
                            contentDetailBulletType = bulletType
                        )
                    )
                } else {
                    val nextPos = sectionChildRepository.getChild7Count(sectionHeadAddedId)
                    sectionChildRepository.insertChild7(
                        SectionChild7(
                            id = 0,
                            sectionHeadAddedId = sectionHeadAddedId,
                            indexPosition = nextPos,
                            contentTitle = contentTitle,
                            contentSubtitle = contentSubtitle,
                            contentDetail = contentDetail,
                            contentDetailBulletType = bulletType
                        )
                    )
                }
                analyticsManager.logSc7SubSave()
                _uiState.value = Child7SubUiState.Saved
            } catch (e: Exception) {
                _uiState.value = Child7SubUiState.Error(e.message ?: "Save failed")
            }
        }
    }
}

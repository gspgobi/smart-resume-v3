package com.nithra.nithraresume.ui.section.child

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.SectionChild2
import com.nithra.nithraresume.data.repository.SectionChildRepository
import com.nithra.nithraresume.utils.AnalyticsManager
import com.nithra.nithraresume.utils.BULLET_NONE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface Child2SubUiState {
    data object Loading : Child2SubUiState
    data object Ready : Child2SubUiState
    data object Saved : Child2SubUiState
    data class Error(val message: String) : Child2SubUiState
}

@HiltViewModel
class SectionChild2SubViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sectionChildRepository: SectionChildRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    val sectionHeadAddedId: Int = checkNotNull(savedStateHandle["sectionHeadAddedId"])
    private val itemId: Int = savedStateHandle["itemId"] ?: -1

    private val _uiState = MutableStateFlow<Child2SubUiState>(Child2SubUiState.Loading)
    val uiState: StateFlow<Child2SubUiState> = _uiState.asStateFlow()

    private val _item = MutableStateFlow<SectionChild2?>(null)
    val item: StateFlow<SectionChild2?> = _item.asStateFlow()

    fun resetState() { _uiState.value = Child2SubUiState.Ready }

    init {
        viewModelScope.launch {
            if (itemId > 0) {
                _item.value = sectionChildRepository.getChild2ById(itemId)
            }
            _uiState.value = Child2SubUiState.Ready
        }
    }

    fun save(
        workRole: String,
        companyName: String,
        subtitle: String,
        workPeriod: String,
        accomplishments: String,
        bulletType: String
    ) {
        viewModelScope.launch {
            try {
                val existing = _item.value
                if (existing != null && existing.id > 0) {
                    sectionChildRepository.updateChild2(
                        existing.copy(
                            workRole = workRole, companyName = companyName,
                            subtitle = subtitle, workPeriod = workPeriod,
                            accomplishments = accomplishments,
                            accomplishmentsBulletType = bulletType
                        )
                    )
                } else {
                    val nextPos = sectionChildRepository.getChild2Count(sectionHeadAddedId)
                    sectionChildRepository.insertChild2(
                        SectionChild2(
                            id = 0,
                            sectionHeadAddedId = sectionHeadAddedId,
                            indexPosition = nextPos,
                            workRole = workRole, companyName = companyName,
                            subtitle = subtitle, workPeriod = workPeriod,
                            accomplishments = accomplishments,
                            accomplishmentsBulletType = bulletType
                        )
                    )
                }
                analyticsManager.logSc2SubSave()
                _uiState.value = Child2SubUiState.Saved
            } catch (e: Exception) {
                _uiState.value = Child2SubUiState.Error(e.message ?: "Save failed")
            }
        }
    }
}

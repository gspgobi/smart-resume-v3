package com.nithra.nithraresume.ui.section.head

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.ResumeFormat
import com.nithra.nithraresume.data.model.SectionHeadAdded
import com.nithra.nithraresume.data.model.SectionHeadSampleData
import com.nithra.nithraresume.data.model.UserProfile
import com.nithra.nithraresume.data.repository.ResumeFormatRepository
import com.nithra.nithraresume.data.repository.SectionChildRepository
import com.nithra.nithraresume.data.repository.SectionHeadRepository
import com.nithra.nithraresume.data.repository.UserProfileRepository
import com.nithra.nithraresume.utils.GROUP_ID_ADDONS
import com.nithra.nithraresume.utils.GROUP_ID_SECTIONS
import com.nithra.nithraresume.utils.SHSD_GROUP_CUSTOM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SectionHeadUiEvent {
    data object Idle : SectionHeadUiEvent
    data class Error(val message: String) : SectionHeadUiEvent
}

@HiltViewModel
class SectionHeadViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userProfileRepository: UserProfileRepository,
    private val resumeFormatRepository: ResumeFormatRepository,
    private val sectionHeadRepository: SectionHeadRepository,
    private val sectionChildRepository: SectionChildRepository
) : ViewModel() {

    val profileId: Int = checkNotNull(savedStateHandle["profileId"])

    // ── Profile & format ──────────────────────────────────────────────────────

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _currentFormat = MutableStateFlow<ResumeFormat?>(null)
    val currentFormat: StateFlow<ResumeFormat?> = _currentFormat.asStateFlow()

    // ── Section lists ─────────────────────────────────────────────────────────

    private val allAdded: StateFlow<List<SectionHeadAdded>> = sectionHeadRepository
        .getAddedByProfileId(profileId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val sections: StateFlow<List<SectionHeadAdded>> = allAdded
        .map { list -> list.filter { it.groupBaseId == GROUP_ID_SECTIONS }
                            .sortedBy { it.indexPosition } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val addons: StateFlow<List<SectionHeadAdded>> = allAdded
        .map { list -> list.filter { it.groupBaseId == GROUP_ID_ADDONS }
                            .sortedBy { it.indexPosition } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Add-section dialog data ───────────────────────────────────────────────

    private val _availableSections = MutableStateFlow<List<SectionHeadSampleData>>(emptyList())
    val availableSections: StateFlow<List<SectionHeadSampleData>> = _availableSections.asStateFlow()

    private val _availableAddons = MutableStateFlow<List<SectionHeadSampleData>>(emptyList())
    val availableAddons: StateFlow<List<SectionHeadSampleData>> = _availableAddons.asStateFlow()

    // ── UI events ─────────────────────────────────────────────────────────────

    private val _uiEvent = MutableStateFlow<SectionHeadUiEvent>(SectionHeadUiEvent.Idle)
    val uiEvent: StateFlow<SectionHeadUiEvent> = _uiEvent.asStateFlow()

    fun resetUiEvent() { _uiEvent.value = SectionHeadUiEvent.Idle }

    init {
        loadProfileAndFormat()
    }

    private fun loadProfileAndFormat() {
        viewModelScope.launch {
            _profile.value = userProfileRepository.getById(profileId)
            _profile.value?.let { p ->
                _currentFormat.value = resumeFormatRepository.getById(p.resumeFormatBaseId)
            }
        }
    }

    fun reloadFormat() {
        viewModelScope.launch {
            _profile.value = userProfileRepository.getById(profileId)
            _profile.value?.let { p ->
                _currentFormat.value = resumeFormatRepository.getById(p.resumeFormatBaseId)
            }
        }
    }

    // ── Add section dialog ────────────────────────────────────────────────────

    fun loadAvailableSections(currentSections: List<SectionHeadAdded>) {
        viewModelScope.launch {
            val allSamples = sectionHeadRepository.getSampleDataByGroupId(GROUP_ID_SECTIONS)
            _availableSections.value = filterAvailable(allSamples, currentSections)
        }
    }

    fun loadAvailableAddons(currentAddons: List<SectionHeadAdded>) {
        viewModelScope.launch {
            val allSamples = sectionHeadRepository.getSampleDataByGroupId(GROUP_ID_ADDONS)
            _availableAddons.value = filterAvailable(allSamples, currentAddons)
        }
    }

    /**
     * Returns SHSD items not yet added, plus all Custom items (can be added multiple times).
     * Group-header sentinel rows (id = -1) are inserted before each group name change.
     */
    private fun filterAvailable(
        allSamples: List<SectionHeadSampleData>,
        currentAdded: List<SectionHeadAdded>
    ): List<SectionHeadSampleData> {
        val addedSampleIds = currentAdded.map { it.sampleDataId }.toSet()
        val result = mutableListOf<SectionHeadSampleData>()
        var lastGroupName = ""
        for (sample in allSamples) {
            val isAlreadyAdded = sample.id in addedSampleIds
            val isCustom = sample.groupName.equals(SHSD_GROUP_CUSTOM, ignoreCase = true)
            if (isAlreadyAdded && !isCustom) continue
            if (sample.groupName != lastGroupName) {
                // Insert group header sentinel (id = -1)
                result.add(sample.copy(id = -1, title = sample.groupName))
                lastGroupName = sample.groupName
            }
            result.add(sample)
        }
        return result
    }

    fun addSection(sample: SectionHeadSampleData, currentList: List<SectionHeadAdded>) {
        viewModelScope.launch {
            try {
                val nextPosition = if (currentList.isEmpty()) 0
                                   else currentList.maxOf { it.indexPosition } + 1
                sectionHeadRepository.insertAdded(
                    SectionHeadAdded(
                        id = 0,
                        profileId = profileId,
                        groupBaseId = sample.sectionHeadGroupBaseId,
                        headBaseId = sample.sectionHeadBaseId,
                        sampleDataId = sample.id,
                        title = sample.title,
                        isEnable = true,
                        indexPosition = nextPosition
                    )
                )
            } catch (e: Exception) {
                _uiEvent.value = SectionHeadUiEvent.Error(e.message ?: "Failed to add section")
            }
        }
    }

    // ── Toggle enable / disable ───────────────────────────────────────────────

    fun toggleEnable(sha: SectionHeadAdded) {
        viewModelScope.launch {
            sectionHeadRepository.updateAddedIsEnable(sha.id, !sha.isEnable)
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    fun deleteSection(sha: SectionHeadAdded, groupList: List<SectionHeadAdded>) {
        viewModelScope.launch {
            try {
                // Delete child data
                deleteChildData(sha.id)
                // Shift index positions for items after the deleted one
                groupList
                    .filter { it.indexPosition > sha.indexPosition }
                    .forEach { item ->
                        sectionHeadRepository.updateAddedPosition(item.id, item.indexPosition - 1)
                    }
                sectionHeadRepository.deleteAdded(sha)
            } catch (e: Exception) {
                _uiEvent.value = SectionHeadUiEvent.Error(e.message ?: "Failed to delete section")
            }
        }
    }

    private suspend fun deleteChildData(sectionHeadAddedId: Int) {
        sectionChildRepository.deleteChild1(sectionHeadAddedId)
        sectionChildRepository.deleteChild2ByHeadId(sectionHeadAddedId)
        sectionChildRepository.deleteChild3ByHeadId(sectionHeadAddedId)
        sectionChildRepository.deleteChild4(sectionHeadAddedId)
        sectionChildRepository.deleteChild5(sectionHeadAddedId)
        sectionChildRepository.deleteChild6ByHeadId(sectionHeadAddedId)
        sectionChildRepository.deleteChild7ByHeadId(sectionHeadAddedId)
        sectionChildRepository.deleteChild8(sectionHeadAddedId)
    }
}

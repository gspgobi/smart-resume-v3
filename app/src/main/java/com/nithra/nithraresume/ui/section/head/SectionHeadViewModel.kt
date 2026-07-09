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
import com.nithra.nithraresume.utils.AnalyticsManager
import com.nithra.nithraresume.utils.GROUP_ID_ADDONS
import com.nithra.nithraresume.utils.GROUP_ID_SECTIONS
import com.nithra.nithraresume.utils.SHSD_GROUP_CUSTOM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.util.Log
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
    private val sectionChildRepository: SectionChildRepository,
    private val analyticsManager: AnalyticsManager
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
        viewModelScope.launch {
            userProfileRepository.getByIdFlow(profileId).collect { profile ->
                _profile.value = profile
                _currentFormat.value = profile?.let {
                    resumeFormatRepository.getById(it.resumeFormatBaseId)
                }
            }
        }
        viewModelScope.launch {
            sections.collectLatest { current ->
                val all = sectionHeadRepository.getSampleDataByGroupId(GROUP_ID_SECTIONS)
                _availableSections.value = filterAvailable(all, current)
            }
        }
        viewModelScope.launch {
            addons.collectLatest { current ->
                val all = sectionHeadRepository.getSampleDataByGroupId(GROUP_ID_ADDONS)
                _availableAddons.value = filterAvailable(all, current)
            }
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
                if (sample.sectionHeadGroupBaseId == GROUP_ID_SECTIONS) analyticsManager.logShaAddNewSection()
                else analyticsManager.logShaAddNewAddon()
            } catch (e: Exception) {
                Log.e(TAG, "addSection", e)
                _uiEvent.value = SectionHeadUiEvent.Error(e.message ?: "Failed to add section")
            }
        }
    }

    // ── Toggle enable / disable ───────────────────────────────────────────────

    fun toggleEnable(sha: SectionHeadAdded) {
        viewModelScope.launch {
            val newEnabled = !sha.isEnable
            sectionHeadRepository.updateAddedIsEnable(sha.id, newEnabled)
            if (sha.groupBaseId == GROUP_ID_SECTIONS) analyticsManager.logShaEnableDisableSection(newEnabled)
            else analyticsManager.logShaEnableDisableAddon(newEnabled)
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
                if (sha.groupBaseId == GROUP_ID_SECTIONS) analyticsManager.logShaDeleteSection(sha.headBaseId)
                else analyticsManager.logShaDeleteAddon(sha.headBaseId)
            } catch (e: Exception) {
                Log.e(TAG, "deleteSection", e)
                _uiEvent.value = SectionHeadUiEvent.Error(e.message ?: "Failed to delete section")
            }
        }
    }

    fun onResumeFormatClicked()   { analyticsManager.logShaResumeFormat() }
    fun onGenerateResumeClicked() { analyticsManager.logShaGenerateResume() }
    fun onViewShareClicked()      { analyticsManager.logShaViewShare() }

    private suspend fun deleteChildData(sectionHeadAddedId: Int) = coroutineScope {
        launch { sectionChildRepository.deleteChild1(sectionHeadAddedId) }
        launch { sectionChildRepository.deleteChild2ByHeadId(sectionHeadAddedId) }
        launch { sectionChildRepository.deleteChild3ByHeadId(sectionHeadAddedId) }
        launch { sectionChildRepository.deleteChild4(sectionHeadAddedId) }
        launch { sectionChildRepository.deleteChild5(sectionHeadAddedId) }
        launch { sectionChildRepository.deleteChild6ByHeadId(sectionHeadAddedId) }
        launch { sectionChildRepository.deleteChild7ByHeadId(sectionHeadAddedId) }
        launch { sectionChildRepository.deleteChild8(sectionHeadAddedId) }
    }

    private companion object { const val TAG = "SectionHeadVM" }
}

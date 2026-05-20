package com.nithra.nithraresume.ui.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.SectionHeadAdded
import com.nithra.nithraresume.data.model.UserProfile
import com.nithra.nithraresume.data.repository.ResumeFormatRepository
import com.nithra.nithraresume.data.repository.SectionChildRepository
import com.nithra.nithraresume.data.repository.SectionHeadRepository
import com.nithra.nithraresume.data.repository.UserProfileRepository
import com.nithra.nithraresume.utils.MAX_PROFILES
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface UserProfileUiState {
    data object Idle : UserProfileUiState
    data object Loading : UserProfileUiState
    data class Error(val message: String) : UserProfileUiState
    data object ProfileCreated : UserProfileUiState
    data object ProfileRenamed : UserProfileUiState
    data object ProfileDeleted : UserProfileUiState
}

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userProfileRepository: UserProfileRepository,
    private val resumeFormatRepository: ResumeFormatRepository,
    private val sectionHeadRepository: SectionHeadRepository,
    private val sectionChildRepository: SectionChildRepository
) : ViewModel() {

    val dummyCreated: Boolean = savedStateHandle.get<Boolean>("dummyCreated") ?: false

    val profiles: StateFlow<List<UserProfile>> = userProfileRepository
        .getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow<UserProfileUiState>(UserProfileUiState.Idle)
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    fun resetUiState() {
        _uiState.value = UserProfileUiState.Idle
    }

    // ── Name helpers ──────────────────────────────────────────────────────────

    fun suggestNewProfileName(existingProfiles: List<UserProfile>): String {
        val base = "My Profile"
        val names = existingProfiles.map { it.name.trim() }
        if (!names.contains(base)) return base
        for (i in 2..MAX_PROFILES) {
            val candidate = "$base ($i)"
            if (!names.contains(candidate)) return candidate
        }
        return base
    }

    fun isNameDuplicate(name: String, existingProfiles: List<UserProfile>): Boolean =
        existingProfiles.any { it.name == name }

    // ── Create ────────────────────────────────────────────────────────────────

    fun createProfile(name: String, existingProfiles: List<UserProfile>) {
        viewModelScope.launch {
            _uiState.value = UserProfileUiState.Loading
            try {
                val defaultFormat = resumeFormatRepository.getDefault()
                    ?: resumeFormatRepository.getAll().first().firstOrNull()
                    ?: return@launch

                val nextPosition = if (existingProfiles.isEmpty()) 0
                                   else existingProfiles.maxOf { it.indexPosition } + 1

                val newProfile = UserProfile(
                    id = 0,
                    name = name,
                    indexPosition = nextPosition,
                    isSampleProfile = false,
                    sampleProfileId = -1,
                    resumeFormatBaseId = defaultFormat.id,
                    fontStyle = defaultFormat.fontStyle,
                    fontSize = defaultFormat.fontSize,
                    backgroundColor = defaultFormat.backgroundColor,
                    resumeFileName = name
                )
                val profileId = userProfileRepository.insert(newProfile)

                // Seed default sections for the new profile
                val defaults = sectionHeadRepository.getDefaultSampleData()
                defaults.forEach { sample ->
                    sectionHeadRepository.insertAdded(
                        SectionHeadAdded(
                            id = 0,
                            profileId = profileId.toInt(),
                            groupBaseId = sample.sectionHeadGroupBaseId,
                            headBaseId = sample.sectionHeadBaseId,
                            sampleDataId = sample.id,
                            title = sample.title,
                            isEnable = sample.isEnable,
                            indexPosition = sample.indexPosition
                        )
                    )
                }
                _uiState.value = UserProfileUiState.ProfileCreated
            } catch (e: Exception) {
                _uiState.value = UserProfileUiState.Error(e.message ?: "Failed to create profile")
            }
        }
    }

    // ── Rename ────────────────────────────────────────────────────────────────

    fun renameProfile(profile: UserProfile, newName: String) {
        viewModelScope.launch {
            _uiState.value = UserProfileUiState.Loading
            try {
                userProfileRepository.update(profile.copy(name = newName, resumeFileName = newName))
                _uiState.value = UserProfileUiState.ProfileRenamed
            } catch (e: Exception) {
                _uiState.value = UserProfileUiState.Error(e.message ?: "Failed to rename profile")
            }
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    fun deleteProfile(profile: UserProfile, allProfiles: List<UserProfile>) {
        viewModelScope.launch {
            _uiState.value = UserProfileUiState.Loading
            try {
                // Cascade-delete all section children then heads
                val headList = sectionHeadRepository.getAddedByProfileIdOnce(profile.id)
                headList.forEach { sha -> deleteAllChildrenForHead(sha.id) }
                sectionHeadRepository.deleteAddedByProfileId(profile.id)

                // Shift index positions down for profiles that followed the deleted one
                allProfiles
                    .filter { it.indexPosition > profile.indexPosition }
                    .forEach { p ->
                        userProfileRepository.update(p.copy(indexPosition = p.indexPosition - 1))
                    }

                userProfileRepository.delete(profile)
                _uiState.value = UserProfileUiState.ProfileDeleted
            } catch (e: Exception) {
                _uiState.value = UserProfileUiState.Error(e.message ?: "Failed to delete profile")
            }
        }
    }

    private suspend fun deleteAllChildrenForHead(sectionHeadAddedId: Int) {
        // Call all 8 delete-by-head methods; no-op for tables that don't hold this ID
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

package com.nithra.nithraresume.ui.generate

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.UserProfile
import com.nithra.nithraresume.data.repository.ResumeFormatRepository
import com.nithra.nithraresume.data.repository.SectionChildRepository
import com.nithra.nithraresume.data.repository.SectionHeadRepository
import com.nithra.nithraresume.data.repository.UserProfileRepository
import com.nithra.nithraresume.pdf.ResumePdfBuilder
import com.nithra.nithraresume.pdf.ResumePdfData
import com.nithra.nithraresume.utils.GROUP_ID_ADDONS
import com.nithra.nithraresume.utils.GROUP_ID_SECTIONS
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

sealed interface GenerateResumeUiState {
    data object Idle : GenerateResumeUiState
    data object Loading : GenerateResumeUiState
    data object Generating : GenerateResumeUiState
    data class Done(val pdfFile: File) : GenerateResumeUiState
    data class Error(val message: String) : GenerateResumeUiState
}

@HiltViewModel
class GenerateResumeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val userProfileRepository: UserProfileRepository,
    private val resumeFormatRepository: ResumeFormatRepository,
    private val sectionHeadRepository: SectionHeadRepository,
    private val sectionChildRepository: SectionChildRepository
) : ViewModel() {

    val profileId: Int = checkNotNull(savedStateHandle["profileId"])

    private val _uiState = MutableStateFlow<GenerateResumeUiState>(GenerateResumeUiState.Loading)
    val uiState: StateFlow<GenerateResumeUiState> = _uiState.asStateFlow()

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    fun resetState() { _uiState.value = GenerateResumeUiState.Idle }

    init {
        viewModelScope.launch {
            _profile.value = userProfileRepository.getById(profileId)
            _uiState.value = GenerateResumeUiState.Idle
        }
    }

    fun generate(fileName: String) {
        val currentProfile = _profile.value ?: return
        _uiState.value = GenerateResumeUiState.Generating

        viewModelScope.launch {
            try {
                val pdfFile = withContext(Dispatchers.IO) {
                    buildPdf(currentProfile, fileName)
                }
                // Persist the file name back to the profile
                userProfileRepository.updateResumeFileName(profileId, fileName)
                _profile.value = userProfileRepository.getById(profileId)
                _uiState.value = GenerateResumeUiState.Done(pdfFile)
            } catch (e: Exception) {
                _uiState.value = GenerateResumeUiState.Error(e.message ?: "Failed to generate resume")
            }
        }
    }

    private suspend fun buildPdf(profile: UserProfile, fileName: String): File {
        val format = resumeFormatRepository.getById(profile.resumeFormatBaseId)
            ?: resumeFormatRepository.getDefault()
            ?: error("No resume format found")

        val allEnabled = sectionHeadRepository.getEnabledByProfileId(profileId)
        val sections = allEnabled.filter { it.groupBaseId == GROUP_ID_SECTIONS }
            .sortedBy { it.indexPosition }
        val addons = allEnabled.filter { it.groupBaseId == GROUP_ID_ADDONS }
            .sortedBy { it.indexPosition }

        val sc1Map = sections.filter { it.headBaseId == 1 }.mapNotNull { sha ->
            sectionChildRepository.getChild1Once(sha.id)
                ?.let { sha.id to it }
        }.toMap()

        val sc2Map = sections.filter { it.headBaseId == 2 }.associate { sha ->
            sha.id to sectionChildRepository.getChild2ListOnce(sha.id)
        }

        val sc3Map = sections.filter { it.headBaseId == 3 }.associate { sha ->
            sha.id to sectionChildRepository.getChild3ListOnce(sha.id)
        }

        val sc4Map = sections.filter { it.headBaseId == 4 }.mapNotNull { sha ->
            sectionChildRepository.getChild4Once(sha.id)
                ?.let { sha.id to it }
        }.toMap()

        val sc5Map = sections.filter { it.headBaseId == 5 }.mapNotNull { sha ->
            sectionChildRepository.getChild5Once(sha.id)
                ?.let { sha.id to it }
        }.toMap()

        val sc6Map = sections.filter { it.headBaseId == 6 }.associate { sha ->
            sha.id to sectionChildRepository.getChild6ListOnce(sha.id)
        }

        val sc7Map = sections.filter { it.headBaseId == 7 }.associate { sha ->
            sha.id to sectionChildRepository.getChild7ListOnce(sha.id)
        }

        val sc8Map = addons.filter { it.headBaseId == 8 }.mapNotNull { sha ->
            sectionChildRepository.getChild8Once(sha.id)
                ?.let { sha.id to it }
        }.toMap()

        val data = ResumePdfData(
            profile = profile,
            format = format,
            sections = sections,
            addons = addons,
            sc1ByHeadId = sc1Map,
            sc2sByHeadId = sc2Map,
            sc3sByHeadId = sc3Map,
            sc4ByHeadId = sc4Map,
            sc5ByHeadId = sc5Map,
            sc6sByHeadId = sc6Map,
            sc7sByHeadId = sc7Map,
            sc8ByHeadId = sc8Map
        )

        return ResumePdfBuilder(context).build(data, fileName)
    }
}
// update 120

package com.nithra.nithraresume.ui.generate

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nithra.nithraresume.data.model.ResumeFormat
import com.nithra.nithraresume.data.model.SectionChild1
import com.nithra.nithraresume.data.model.SectionChild4
import com.nithra.nithraresume.data.model.UserProfile
import com.nithra.nithraresume.data.repository.ResumeFormatRepository
import com.nithra.nithraresume.data.repository.SectionChildRepository
import com.nithra.nithraresume.data.repository.SectionHeadRepository
import com.nithra.nithraresume.data.repository.UserProfileRepository
import com.nithra.nithraresume.pdf.ResumePdfBuilder
import com.nithra.nithraresume.pdf.ResumePdfData
import com.nithra.nithraresume.utils.DOT_PDF
import com.nithra.nithraresume.utils.GROUP_ID_ADDONS
import com.nithra.nithraresume.utils.GROUP_ID_SECTIONS
import com.nithra.nithraresume.utils.SrDir
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

    private val _currentFormat = MutableStateFlow<ResumeFormat?>(null)
    val currentFormat: StateFlow<ResumeFormat?> = _currentFormat.asStateFlow()

    private val _sc1 = MutableStateFlow<SectionChild1?>(null)
    val sc1: StateFlow<SectionChild1?> = _sc1.asStateFlow()

    private val _sc4 = MutableStateFlow<SectionChild4?>(null)
    val sc4: StateFlow<SectionChild4?> = _sc4.asStateFlow()

    fun resetState() { _uiState.value = GenerateResumeUiState.Idle }

    fun fileExists(fileName: String): Boolean {
        val file = java.io.File(
            context.getExternalFilesDir(null),
            "${SrDir.GENERATED_RESUME}/$fileName$DOT_PDF"
        )
        return file.exists()
    }

    init {
        viewModelScope.launch {
            val sections = sectionHeadRepository.getEnabledByProfileId(profileId)
                .filter { it.groupBaseId == GROUP_ID_SECTIONS }
            sections.firstOrNull { it.headBaseId == 1 }?.let { sha ->
                _sc1.value = sectionChildRepository.getChild1Once(sha.id)
            }
            sections.firstOrNull { it.headBaseId == 4 }?.let { sha ->
                _sc4.value = sectionChildRepository.getChild4Once(sha.id)
            }
            _uiState.value = GenerateResumeUiState.Idle
        }

        viewModelScope.launch {
            userProfileRepository.getByIdFlow(profileId).collect { profile ->
                _profile.value = profile
                _currentFormat.value = profile?.let {
                    resumeFormatRepository.getById(it.resumeFormatBaseId)
                }
            }
        }
    }

    fun generate(fileName: String, includeUserImage: Boolean, includeSignature: Boolean) {
        val currentProfile = _profile.value ?: return
        _uiState.value = GenerateResumeUiState.Generating

        viewModelScope.launch {
            try {
                // Persist flags to DB first — buildPdf reads fresh values from DB
                _sc1.value?.let { c1 ->
                    val updated = c1.copy(isUserImageEnable = includeUserImage)
                    sectionChildRepository.updateChild1(updated)
                    _sc1.value = updated
                }
                _sc4.value?.let { c4 ->
                    val updated = c4.copy(isSignatureImageEnable = includeSignature)
                    sectionChildRepository.updateChild4(updated)
                    _sc4.value = updated
                }

                val pdfFile = withContext(Dispatchers.IO) {
                    buildPdf(currentProfile, fileName)
                }
                userProfileRepository.updateResumeFileName(profileId, fileName)
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
            sectionChildRepository.getChild1Once(sha.id)?.let { sha.id to it }
        }.toMap()

        val sc2Map = sections.filter { it.headBaseId == 2 }.associate { sha ->
            sha.id to sectionChildRepository.getChild2ListOnce(sha.id)
        }

        val sc3Map = sections.filter { it.headBaseId == 3 }.associate { sha ->
            sha.id to sectionChildRepository.getChild3ListOnce(sha.id)
        }

        val sc4Map = sections.filter { it.headBaseId == 4 }.mapNotNull { sha ->
            sectionChildRepository.getChild4Once(sha.id)?.let { sha.id to it }
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

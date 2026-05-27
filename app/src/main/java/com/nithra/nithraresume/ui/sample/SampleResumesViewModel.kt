package com.nithra.nithraresume.ui.sample

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.nithra.nithraresume.data.model.SectionChild1
import com.nithra.nithraresume.data.model.SectionChild2
import com.nithra.nithraresume.data.model.SectionChild3
import com.nithra.nithraresume.data.model.SectionChild4
import com.nithra.nithraresume.data.model.SectionChild5
import com.nithra.nithraresume.data.model.SectionChild6
import com.nithra.nithraresume.data.model.SectionChild7
import com.nithra.nithraresume.data.model.SectionChild8
import com.nithra.nithraresume.data.model.SectionHeadAdded
import com.nithra.nithraresume.data.model.UserProfile
import com.nithra.nithraresume.data.repository.SectionChildRepository
import com.nithra.nithraresume.data.repository.SectionHeadRepository
import com.nithra.nithraresume.data.repository.UserProfileRepository
import com.nithra.nithraresume.utils.AnalyticsManager
import com.nithra.nithraresume.utils.AssetDir
import com.nithra.nithraresume.utils.AssetFile
import com.nithra.nithraresume.utils.DOT_PDF
import com.nithra.nithraresume.utils.SAMPLE_RESUME_PREVIEW_ASSET_PREFIX
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import javax.inject.Inject

// ── UI state ──────────────────────────────────────────────────────────────────

data class SampleGroup(val title: String, val items: List<SampleItem>)
data class SampleItem(val name: String, val sampleProfileId: Int)

sealed interface SampleResumesUiState {
    data object Loading : SampleResumesUiState
    data class Ready(val groups: List<SampleGroup>) : SampleResumesUiState
    data object Adding : SampleResumesUiState
    data object Added : SampleResumesUiState
    data class PreviewReady(val file: File, val groups: List<SampleGroup>) : SampleResumesUiState
    data class Error(val message: String, val groups: List<SampleGroup>) : SampleResumesUiState
}

// ── JSON data classes ─────────────────────────────────────────────────────────

private data class SampleResumesJson(val userProfileArrayList: List<SampleProfileJson>?)
private data class SampleProfileJson(
    val upName: String?,
    val upIsSampleProfile: Boolean = true,
    val sampleProfileId: Int = 0,
    val sampleProfileType: Int = 1,
    val resumeFormatBaseId: Int = 1,
    val upFontStyle: String?,
    val upFontSize: Int = 12,
    val upBackgroundColor: String?,
    val upResumeFileName: String?,
    val sectionHeadAddedArrayList: List<SampleShaJson>?
)
private data class SampleShaJson(
    val sectionHeadGroupBaseId: Int = 1,
    val sectionHeadBaseId: Int = 1,
    val sectionHeadSampleDataId: Int?,
    val shaTitle: String?,
    val shaIsEnable: Boolean = true,
    val shaIndexPosition: Int = 0,
    val sectionChild1: SampleSc1Json?,
    val sectionChild2ArrayList: List<SampleSc2Json>?,
    val sectionChild3ArrayList: List<SampleSc3Json>?,
    val sectionChild4: SampleSc4Json?,
    val sectionChild5: SampleSc5Json?,
    val sectionChild6ArrayList: List<SampleSc6Json>?,
    val sectionChild7ArrayList: List<SampleSc7Json>?,
    val sectionChild8: SampleSc8Json?
)
private data class SampleSc1Json(
    val sc1Name: String?, val sc1Address: String?, val sc1Email: String?, val sc1Phone: String?,
    val sc1Gender: String?, val sc1Dob: String?, val sc1DobDateFormat: String?,
    val sc1Nationality: String?, val sc1UserImagePath: String?, val sc1IsUserImageEnable: Boolean = false
)
private data class SampleSc2Json(
    val sc2IndexPosition: Int = 0, val sc2WorkRole: String?, val sc2CompanyName: String?,
    val sc2Subtitle: String?, val sc2WorkPeriod: String?,
    val sc2Accomplishments: String?, val sc2AccomplishmentsBulletType: String?
)
private data class SampleSc3Json(
    val sc3IndexPosition: Int = 0, val sc3StudyDegree: String?, val sc3SchoolName: String?,
    val sc3Subtitle: String?, val sc3StudyPeriod: String?,
    val sc3Concentrates: String?, val sc3ConcentratesBulletType: String?
)
private data class SampleSc4Json(
    val sc4DeclarationContent: String?, val sc4DeclarationContentBulletType: String?,
    val sc4Date: String?, val sc4DateDateFormat: String?, val sc4Place: String?,
    val sc4SignatureImagePath: String?, val sc4IsSignatureImageEnable: Boolean = false
)
private data class SampleSc5Json(val sc5Content: String?, val sc5ContentBulletType: String?)
private data class SampleSc6Json(
    val sc6IndexPosition: Int = 0, val sc6ContentTitle: String?, val sc6ContentDetail: String?
)
private data class SampleSc7Json(
    val sc7IndexPosition: Int = 0, val sc7ContentTitle: String?, val sc7ContentSubtitle: String?,
    val sc7ContentDetail: String?, val sc7ContentDetailBulletType: String?
)
private data class SampleSc8Json(
    val sc8Date: String?, val sc8DateDateFormat: String?,
    val sc8Address: String?, val sc8Content: String?
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

private val CATEGORY_TITLES = listOf(
    "Freshers (0-1 yrs)",
    "Entry Level (1-3 yrs)",
    "Mid Level (3-8 yrs)",
    "Senior Level (8+ yrs)"
)

@HiltViewModel
class SampleResumesViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userProfileRepository: UserProfileRepository,
    private val sectionHeadRepository: SectionHeadRepository,
    private val sectionChildRepository: SectionChildRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SampleResumesUiState>(SampleResumesUiState.Loading)
    val uiState: StateFlow<SampleResumesUiState> = _uiState.asStateFlow()

    private var cachedGroups: List<SampleGroup> = emptyList()
    private var cachedProfiles: List<SampleProfileJson> = emptyList()

    init {
        viewModelScope.launch(Dispatchers.IO) { loadGroups() }
    }

    private fun loadGroups() {
        val json = context.assets.open("${AssetDir.JSON}/${AssetFile.SAMPLE_RESUMES_JSON}")
            .bufferedReader().use { it.readText() }
        val data = Gson().fromJson(json, SampleResumesJson::class.java)
        cachedProfiles = data.userProfileArrayList.orEmpty()
        cachedGroups = CATEGORY_TITLES.mapIndexed { idx, title ->
            val type = idx + 1
            SampleGroup(
                title = title,
                items = cachedProfiles
                    .filter { it.sampleProfileType == type }
                    .map { SampleItem(it.upName.orEmpty(), it.sampleProfileId) }
            )
        }
        _uiState.value = SampleResumesUiState.Ready(cachedGroups)
    }

    fun addSampleProfile(sampleProfileId: Int) {
        val profile = cachedProfiles.firstOrNull { it.sampleProfileId == sampleProfileId } ?: return
        viewModelScope.launch {
            _uiState.value = SampleResumesUiState.Adding
            withContext(Dispatchers.IO) {
                val maxPos = userProfileRepository.getAllOnce().maxOfOrNull { it.indexPosition } ?: -1
                val newProfileId = userProfileRepository.insert(
                    UserProfile(
                        name = profile.upName.orEmpty(),
                        indexPosition = maxPos + 1,
                        isSampleProfile = profile.upIsSampleProfile,
                        sampleProfileId = profile.sampleProfileId,
                        resumeFormatBaseId = profile.resumeFormatBaseId,
                        fontStyle = profile.upFontStyle.orEmpty(),
                        fontSize = profile.upFontSize,
                        backgroundColor = profile.upBackgroundColor.orEmpty(),
                        resumeFileName = profile.upResumeFileName
                    )
                ).toInt()

                for (sha in profile.sectionHeadAddedArrayList.orEmpty()) {
                    val newShaId = sectionHeadRepository.insertAdded(
                        SectionHeadAdded(
                            profileId = newProfileId,
                            groupBaseId = sha.sectionHeadGroupBaseId,
                            headBaseId = sha.sectionHeadBaseId,
                            sampleDataId = sha.sectionHeadSampleDataId,
                            title = sha.shaTitle.orEmpty(),
                            isEnable = sha.shaIsEnable,
                            indexPosition = sha.shaIndexPosition
                        )
                    ).toInt()

                    when (sha.sectionHeadBaseId) {
                        1 -> sha.sectionChild1?.let { sc ->
                            sectionChildRepository.saveChild1(SectionChild1(
                                sectionHeadAddedId = newShaId,
                                name = sc.sc1Name.orEmpty(), address = sc.sc1Address.orEmpty(),
                                email = sc.sc1Email.orEmpty(), phone = sc.sc1Phone.orEmpty(),
                                gender = sc.sc1Gender.orEmpty(), dob = sc.sc1Dob.orEmpty(),
                                dobDateFormat = sc.sc1DobDateFormat.orEmpty(),
                                nationality = sc.sc1Nationality.orEmpty(),
                                userImagePath = sc.sc1UserImagePath.orEmpty(),
                                isUserImageEnable = sc.sc1IsUserImageEnable
                            ))
                        }
                        2 -> sha.sectionChild2ArrayList?.forEach { sc ->
                            sectionChildRepository.insertChild2(SectionChild2(
                                sectionHeadAddedId = newShaId,
                                indexPosition = sc.sc2IndexPosition,
                                workRole = sc.sc2WorkRole.orEmpty(),
                                companyName = sc.sc2CompanyName.orEmpty(),
                                subtitle = sc.sc2Subtitle.orEmpty(),
                                workPeriod = sc.sc2WorkPeriod.orEmpty(),
                                accomplishments = sc.sc2Accomplishments.orEmpty(),
                                accomplishmentsBulletType = sc.sc2AccomplishmentsBulletType.orEmpty()
                            ))
                        }
                        3 -> sha.sectionChild3ArrayList?.forEach { sc ->
                            sectionChildRepository.insertChild3(SectionChild3(
                                sectionHeadAddedId = newShaId,
                                indexPosition = sc.sc3IndexPosition,
                                studyDegree = sc.sc3StudyDegree.orEmpty(),
                                schoolName = sc.sc3SchoolName.orEmpty(),
                                subtitle = sc.sc3Subtitle.orEmpty(),
                                studyPeriod = sc.sc3StudyPeriod.orEmpty(),
                                concentrates = sc.sc3Concentrates.orEmpty(),
                                concentratesBulletType = sc.sc3ConcentratesBulletType.orEmpty()
                            ))
                        }
                        4 -> sha.sectionChild4?.let { sc ->
                            sectionChildRepository.saveChild4(SectionChild4(
                                sectionHeadAddedId = newShaId,
                                declarationContent = sc.sc4DeclarationContent.orEmpty(),
                                declarationContentBulletType = sc.sc4DeclarationContentBulletType.orEmpty(),
                                date = sc.sc4Date.orEmpty(),
                                dateDateFormat = sc.sc4DateDateFormat.orEmpty(),
                                place = sc.sc4Place.orEmpty(),
                                signatureImagePath = sc.sc4SignatureImagePath.orEmpty(),
                                isSignatureImageEnable = sc.sc4IsSignatureImageEnable
                            ))
                        }
                        5 -> sha.sectionChild5?.let { sc ->
                            sectionChildRepository.saveChild5(SectionChild5(
                                sectionHeadAddedId = newShaId,
                                content = sc.sc5Content.orEmpty(),
                                contentBulletType = sc.sc5ContentBulletType.orEmpty()
                            ))
                        }
                        6 -> sha.sectionChild6ArrayList?.forEach { sc ->
                            sectionChildRepository.insertChild6(SectionChild6(
                                sectionHeadAddedId = newShaId,
                                indexPosition = sc.sc6IndexPosition,
                                contentTitle = sc.sc6ContentTitle.orEmpty(),
                                contentDetail = sc.sc6ContentDetail.orEmpty()
                            ))
                        }
                        7 -> sha.sectionChild7ArrayList?.forEach { sc ->
                            sectionChildRepository.insertChild7(SectionChild7(
                                sectionHeadAddedId = newShaId,
                                indexPosition = sc.sc7IndexPosition,
                                contentTitle = sc.sc7ContentTitle.orEmpty(),
                                contentSubtitle = sc.sc7ContentSubtitle.orEmpty(),
                                contentDetail = sc.sc7ContentDetail.orEmpty(),
                                contentDetailBulletType = sc.sc7ContentDetailBulletType.orEmpty()
                            ))
                        }
                        8 -> sha.sectionChild8?.let { sc ->
                            sectionChildRepository.saveChild8(SectionChild8(
                                sectionHeadAddedId = newShaId,
                                date = sc.sc8Date.orEmpty(),
                                dateDateFormat = sc.sc8DateDateFormat.orEmpty(),
                                address = sc.sc8Address.orEmpty(),
                                content = sc.sc8Content.orEmpty()
                            ))
                        }
                    }
                }
            }
            analyticsManager.logProfileCreated(isFromSample = true)
            _uiState.value = SampleResumesUiState.Added
        }
    }

    fun openPreview(sampleProfileId: Int) {
        analyticsManager.logSrSamplePreview(sampleProfileId)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val assetName = "$SAMPLE_RESUME_PREVIEW_ASSET_PREFIX$sampleProfileId$DOT_PDF"
                val destFile = File(context.filesDir, assetName)
                if (!destFile.exists()) {
                    context.assets.open("${AssetDir.SAMPLE_RESUMES}/$assetName").use { input ->
                        destFile.outputStream().use { output -> input.copyTo(output) }
                    }
                }
                destFile
            }.onSuccess { file ->
                _uiState.value = SampleResumesUiState.PreviewReady(file, cachedGroups)
            }.onFailure {
                _uiState.value = SampleResumesUiState.Error(
                    "Could not open preview", cachedGroups
                )
            }
        }
    }

    fun onPreviewHandled() {
        _uiState.value = SampleResumesUiState.Ready(cachedGroups)
    }

    fun onErrorHandled() {
        _uiState.value = SampleResumesUiState.Ready(cachedGroups)
    }
}

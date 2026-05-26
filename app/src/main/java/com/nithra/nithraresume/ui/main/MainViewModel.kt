package com.nithra.nithraresume.ui.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.nithra.nithraresume.data.api.ApiRepository
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
import com.nithra.nithraresume.data.repository.FcmRepository
import com.nithra.nithraresume.data.repository.SectionChildRepository
import com.nithra.nithraresume.data.repository.SectionHeadRepository
import com.nithra.nithraresume.data.repository.UserProfileRepository
import com.nithra.nithraresume.utils.AssetDir
import com.nithra.nithraresume.utils.AssetFile
import com.nithra.nithraresume.utils.GENERATE_COUNT_SHOW_RATE_US
import com.nithra.nithraresume.utils.PrefsManager
import com.nithra.nithraresume.utils.SrDir
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

// ── JSON data classes for dummyResumes.json ───────────────────────────────────

private data class DummyResumesJson(val userProfileArrayList: List<DummyProfileJson>?)
private data class DummyProfileJson(
    val upName: String?,
    val upIndexPosition: Int = -2,
    val upIsSampleProfile: Boolean = false,
    val sampleProfileId: Int? = null,
    val resumeFormatBaseId: Int = 1,
    val upFontStyle: String?,
    val upFontSize: Int = 12,
    val upBackgroundColor: String?,
    val upResumeFileName: String?,
    val sectionHeadAddedArrayList: List<DummyShaJson>?
)
private data class DummyShaJson(
    val sectionHeadGroupBaseId: Int = 1,
    val sectionHeadBaseId: Int = 1,
    val sectionHeadSampleDataId: Int?,
    val shaTitle: String?,
    val shaIsEnable: Boolean = true,
    val shaIndexPosition: Int = 0,
    val sectionChild1: DummySc1Json?,
    val sectionChild2ArrayList: List<DummySc2Json>?,
    val sectionChild3ArrayList: List<DummySc3Json>?,
    val sectionChild4: DummySc4Json?,
    val sectionChild5: DummySc5Json?,
    val sectionChild6ArrayList: List<DummySc6Json>?,
    val sectionChild7ArrayList: List<DummySc7Json>?,
    val sectionChild8: DummySc8Json?
)
private data class DummySc1Json(
    val sc1Name: String?, val sc1Address: String?, val sc1Email: String?, val sc1Phone: String?,
    val sc1Gender: String?, val sc1Dob: String?, val sc1DobDateFormat: String?,
    val sc1Nationality: String?, val sc1UserImagePath: String?, val sc1IsUserImageEnable: Boolean = false
)
private data class DummySc2Json(
    val sc2IndexPosition: Int = 0, val sc2WorkRole: String?, val sc2CompanyName: String?,
    val sc2Subtitle: String?, val sc2WorkPeriod: String?,
    val sc2Accomplishments: String?, val sc2AccomplishmentsBulletType: String?
)
private data class DummySc3Json(
    val sc3IndexPosition: Int = 0, val sc3StudyDegree: String?, val sc3SchoolName: String?,
    val sc3Subtitle: String?, val sc3StudyPeriod: String?,
    val sc3Concentrates: String?, val sc3ConcentratesBulletType: String?
)
private data class DummySc4Json(
    val sc4DeclarationContent: String?, val sc4DeclarationContentBulletType: String?,
    val sc4Date: String?, val sc4DateDateFormat: String?, val sc4Place: String?,
    val sc4SignatureImagePath: String?, val sc4IsSignatureImageEnable: Boolean = false
)
private data class DummySc5Json(val sc5Content: String?, val sc5ContentBulletType: String?)
private data class DummySc6Json(
    val sc6IndexPosition: Int = 0, val sc6ContentTitle: String?, val sc6ContentDetail: String?
)
private data class DummySc7Json(
    val sc7IndexPosition: Int = 0, val sc7ContentTitle: String?, val sc7ContentSubtitle: String?,
    val sc7ContentDetail: String?, val sc7ContentDetailBulletType: String?
)
private data class DummySc8Json(
    val sc8Date: String?, val sc8DateDateFormat: String?,
    val sc8Address: String?, val sc8Content: String?
)

// ── Rate us exit event ─────────────────────────────────────────────────────────

sealed class RateUsEvent {
    data object TriggerExit          : RateUsEvent()
    data object OpenPlayStore        : RateUsEvent()
    data object ShowFeedbackThenExit : RateUsEvent()
}

// ── Migration state ────────────────────────────────────────────────────────────

sealed class MigrationUiState {
    data object Idle : MigrationUiState()
    data object ShowRationale : MigrationUiState()
    data class Running(val done: Int, val total: Int) : MigrationUiState()
    data object PermissionDenied : MigrationUiState()
    data object Finished : MigrationUiState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    fcmRepository: FcmRepository,
    private val apiRepository: ApiRepository,
    private val userProfileRepository: UserProfileRepository,
    private val sectionHeadRepository: SectionHeadRepository,
    private val sectionChildRepository: SectionChildRepository,
    private val prefsManager: PrefsManager
) : ViewModel() {

    /** Unread notification count — drives the bell badge in the TopAppBar. */
    val unreadNotificationCount: StateFlow<Int> = fcmRepository
        .getUnreadCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    private val rateUsDone: StateFlow<Boolean> = prefsManager.v1RateUsDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val resumeGeneratedCount: StateFlow<Int> = prefsManager.v2ResumeGeneratedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private fun markRateUsDone() {
        viewModelScope.launch { prefsManager.setV1RateUsDone() }
    }

    private val _showDoYouLoveAppDialog = MutableStateFlow(false)
    val showDoYouLoveAppDialog: StateFlow<Boolean> = _showDoYouLoveAppDialog

    private val _showRateUs5StarsDialog = MutableStateFlow(false)
    val showRateUs5StarsDialog: StateFlow<Boolean> = _showRateUs5StarsDialog

    private val _rateUsEvent = MutableSharedFlow<RateUsEvent>(extraBufferCapacity = 1)
    val rateUsEvent: SharedFlow<RateUsEvent> = _rateUsEvent.asSharedFlow()

    fun onExitRequested() {
        val count = resumeGeneratedCount.value
        if (!rateUsDone.value && count >= GENERATE_COUNT_SHOW_RATE_US && count % 2 == 0) {
            _showDoYouLoveAppDialog.value = true
        } else {
            viewModelScope.launch { _rateUsEvent.emit(RateUsEvent.TriggerExit) }
        }
    }

    fun onLoveItClicked() {
        _showDoYouLoveAppDialog.value = false
        _showRateUs5StarsDialog.value = true
    }

    fun onCouldBeBetterClicked() {
        _showDoYouLoveAppDialog.value = false
        viewModelScope.launch { _rateUsEvent.emit(RateUsEvent.ShowFeedbackThenExit) }
    }

    fun onDoYouLoveAppDismissed() {
        _showDoYouLoveAppDialog.value = false
        viewModelScope.launch { _rateUsEvent.emit(RateUsEvent.TriggerExit) }
    }

    fun onSureTakeMeThere() {
        _showRateUs5StarsDialog.value = false
        markRateUsDone()
        viewModelScope.launch { _rateUsEvent.emit(RateUsEvent.OpenPlayStore) }
    }

    fun onNoThanks() {
        _showRateUs5StarsDialog.value = false
        markRateUsDone()
        viewModelScope.launch { _rateUsEvent.emit(RateUsEvent.TriggerExit) }
    }

    fun onMaybeLater() {
        _showRateUs5StarsDialog.value = false
        viewModelScope.launch { _rateUsEvent.emit(RateUsEvent.TriggerExit) }
    }

    private val _dummyProfileCreated = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val dummyProfileCreated: SharedFlow<Unit> = _dummyProfileCreated.asSharedFlow()

    private val _migrationState = MutableStateFlow<MigrationUiState>(MigrationUiState.Idle)
    val migrationState: StateFlow<MigrationUiState> = _migrationState

    private var pendingPhotoCount = 0
    private var pendingSigCount   = 0

    init {
        viewModelScope.launch {
            val migrated = prefsManager.v3AllV2FilesMigratedToV3FilesStructure.first()
            if (migrated) return@launch

            val (photoCount, sigCount) = withContext(Dispatchers.IO) {
                coroutineScope {
                    val pc = async { sectionChildRepository.countOldV2UserImagePaths() }
                    val sc = async { sectionChildRepository.countOldV2SignaturePaths() }
                    pc.await() to sc.await()
                }
            }
            if (photoCount == 0 && sigCount == 0) {
                prefsManager.setV3AllV2FilesMigratedToV3FilesStructure()
                return@launch
            }
            pendingPhotoCount = photoCount
            pendingSigCount   = sigCount

            val permName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
            val granted = ContextCompat.checkSelfPermission(context, permName) == PackageManager.PERMISSION_GRANTED
            if (granted) runMigration()
            else _migrationState.value = MigrationUiState.ShowRationale
        }
    }

    fun acknowledgeMigrationDenied() {
        _migrationState.value = MigrationUiState.Finished
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            viewModelScope.launch { runMigration() }
        } else {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    coroutineScope {
                        if (pendingPhotoCount > 0) launch { sectionChildRepository.clearOldV2UserImagePaths() }
                        if (pendingSigCount   > 0) launch { sectionChildRepository.clearOldV2SignatureImagePaths() }
                    }
                }
                prefsManager.setV3AllV2FilesMigratedToV3FilesStructure()
                _migrationState.value = MigrationUiState.PermissionDenied
            }
        }
    }

    private suspend fun runMigration() {
        val total = pendingPhotoCount + pendingSigCount
        var done  = 0
        _migrationState.value = MigrationUiState.Running(done, total)

        withContext(Dispatchers.IO) {
            val v3Base = context.getExternalFilesDir(null) ?: return@withContext
            val v1Base = File(Environment.getExternalStorageDirectory(), "Nithra/SmartResume")

            if (pendingPhotoCount > 0) {
                val dst = File(v3Base, SrDir.USER_IMAGE).also { it.mkdirs() }
                sectionChildRepository.getOldV2UserImagePaths().forEach { oldPath ->
                    val f = File(oldPath)
                    if (f.exists() && f.length() > 0) {
                        runCatching {
                            val nf = File(dst, f.name)
                            f.copyTo(nf, overwrite = true); f.delete()
                            sectionChildRepository.updateV2UserImagePath(oldPath, nf.absolutePath)
                        }.onFailure { sectionChildRepository.clearV2UserImagePath(oldPath) }
                    } else sectionChildRepository.clearV2UserImagePath(oldPath)
                    done++
                    _migrationState.value = MigrationUiState.Running(done, total)
                }
                File(v1Base, "Photo").deleteRecursively()
            }

            if (pendingSigCount > 0) {
                val dst = File(v3Base, SrDir.SIGNATURE).also { it.mkdirs() }
                sectionChildRepository.getOldV2SignaturePaths().forEach { oldPath ->
                    val f = File(oldPath)
                    if (f.exists() && f.length() > 0) {
                        runCatching {
                            val nf = File(dst, f.name)
                            f.copyTo(nf, overwrite = true); f.delete()
                            sectionChildRepository.updateV2SignaturePath(oldPath, nf.absolutePath)
                        }.onFailure { sectionChildRepository.clearV2SignaturePath(oldPath) }
                    } else sectionChildRepository.clearV2SignaturePath(oldPath)
                    done++
                    _migrationState.value = MigrationUiState.Running(done, total)
                }
                File(v1Base, "Signature").deleteRecursively()
            }

            val filesSrc = File(v1Base, "Files")
            if (filesSrc.exists()) {
                val dst = File(v3Base, SrDir.GENERATED_RESUME).also { it.mkdirs() }
                filesSrc.listFiles()?.forEach { it.copyTo(File(dst, it.name), overwrite = true) }
                filesSrc.deleteRecursively()
            }
            v1Base.deleteRecursively()
        }
        prefsManager.setV3AllV2FilesMigratedToV3FilesStructure()
        _migrationState.value = MigrationUiState.Finished
    }

    fun sendFeedback(email: String, feedback: String) {
        viewModelScope.launch {
            apiRepository.postFeedback(feedback = feedback, email = email)
        }
    }

    fun createDummyProfile() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val json = context.assets
                    .open("${AssetDir.JSON}/${AssetFile.DUMMY_RESUMES_JSON}")
                    .bufferedReader().use { it.readText() }
                val data = Gson().fromJson(json, DummyResumesJson::class.java)

                val maxPos = userProfileRepository.getAllOnce().maxOfOrNull { it.indexPosition } ?: -1
                var posOffset = 0

                for (profile in data.userProfileArrayList.orEmpty()) {
                    val newProfileId = userProfileRepository.insert(
                        UserProfile(
                            name = profile.upName.orEmpty(),
                            indexPosition = maxPos + 1 + posOffset,
                            isSampleProfile = profile.upIsSampleProfile,
                            sampleProfileId = profile.sampleProfileId,
                            resumeFormatBaseId = profile.resumeFormatBaseId,
                            fontStyle = profile.upFontStyle.orEmpty(),
                            fontSize = profile.upFontSize,
                            backgroundColor = profile.upBackgroundColor.orEmpty(),
                            resumeFileName = profile.upResumeFileName
                        )
                    ).toInt()
                    posOffset++

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
            }
            _dummyProfileCreated.emit(Unit)
        }
    }
}

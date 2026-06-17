package com.nithra.nithraresume.ui.main


import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.nithra.nithraresume.BuildConfig
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
import com.nithra.nithraresume.data.repository.ResumeFormatRepository
import com.nithra.nithraresume.data.repository.SectionChildRepository
import com.nithra.nithraresume.data.repository.SectionHeadRepository
import com.nithra.nithraresume.data.repository.UserProfileRepository
import com.nithra.nithraresume.utils.AnalyticsManager
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
    data object ShowSafFolderPicker : MigrationUiState()
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
    private val resumeFormatRepository: ResumeFormatRepository,
    private val sectionHeadRepository: SectionHeadRepository,
    private val sectionChildRepository: SectionChildRepository,
    private val prefsManager: PrefsManager,
    private val analyticsManager: AnalyticsManager
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
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val resumeGeneratedCount: StateFlow<Int> = prefsManager.v2ResumeGeneratedCount
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

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
        analyticsManager.logRateUsLoveIt()
        _showDoYouLoveAppDialog.value = false
        _showRateUs5StarsDialog.value = true
    }

    fun onCouldBeBetterClicked() {
        analyticsManager.logRateUsCouldBeBetter()
        _showDoYouLoveAppDialog.value = false
        viewModelScope.launch { _rateUsEvent.emit(RateUsEvent.ShowFeedbackThenExit) }
    }

    fun onDoYouLoveAppDismissed() {
        _showDoYouLoveAppDialog.value = false
        viewModelScope.launch { _rateUsEvent.emit(RateUsEvent.TriggerExit) }
    }

    fun onSureTakeMeThere() {
        analyticsManager.logRateUsRateNow()
        _showRateUs5StarsDialog.value = false
        markRateUsDone()
        viewModelScope.launch { _rateUsEvent.emit(RateUsEvent.OpenPlayStore) }
    }

    fun onNoThanks() {
        analyticsManager.logRateUsNoThanks()
        _showRateUs5StarsDialog.value = false
        markRateUsDone()
        viewModelScope.launch { _rateUsEvent.emit(RateUsEvent.TriggerExit) }
    }

    fun onMaybeLater() {
        analyticsManager.logRateUsLater()
        _showRateUs5StarsDialog.value = false
        viewModelScope.launch { _rateUsEvent.emit(RateUsEvent.TriggerExit) }
    }

    private val _dummyProfileCreated = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val dummyProfileCreated: SharedFlow<Unit> = _dummyProfileCreated.asSharedFlow()

    private val _migrationState = MutableStateFlow<MigrationUiState>(MigrationUiState.Idle)
    val migrationState: StateFlow<MigrationUiState> = _migrationState

    private var pendingPhotoCount = 0
    private var pendingSigCount   = 0
    private var pendingPdfCount   = 0
    private var pdfsFoundViaFile  = false
    private val discoveredMediaStorePdfs = mutableListOf<Pair<Uri, String>>()

    private fun countOldV1PdfsAndPopulateList(): Int {
        discoveredMediaStorePdfs.clear()
        pdfsFoundViaFile = false
        val v1Base = File(Environment.getExternalStorageDirectory(), "Nithra/SmartResume")
        val filesSrc = File(v1Base, "Files")

        // Try direct File approach first
        if (filesSrc.exists()) {
            val count = filesSrc.walk().count { it.isFile && it.name.endsWith(".pdf", ignoreCase = true) }
            if (count > 0) {
                pdfsFoundViaFile = true
                return count
            }
        }

        // Fallback: Query MediaStore database
        @Suppress("DEPRECATION")
        val collection = MediaStore.Files.getContentUri("external")
        @Suppress("DEPRECATION")
        val projection = arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DISPLAY_NAME)
        @Suppress("DEPRECATION")
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ? AND ${MediaStore.Files.FileColumns.DATA} LIKE ?"
        val selectionArgs = arrayOf("application/pdf", "%/Nithra/SmartResume/Files/%")

        runCatching {
            context.contentResolver.query(collection, projection, selection, selectionArgs, null)?.use { cursor ->
                @Suppress("DEPRECATION")
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                @Suppress("DEPRECATION")
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol)
                    val uri = ContentUris.withAppendedId(collection, id)
                    discoveredMediaStorePdfs.add(uri to name)
                }
            }
        }
        return discoveredMediaStorePdfs.size
    }

    init {
        viewModelScope.launch {
            // Create example and new profiles on first v1 check
            if (!prefsManager.v1FirstCheck.first()) {
                createExampleProfile()
                createNewProfile()
                prefsManager.setV1FirstCheck(true)
            }

            val migrated = prefsManager.v3AllV2FilesMigratedToV3FilesStructure.first()
            if (migrated) return@launch

            val (photoCount, sigCount, pdfCount) = withContext(Dispatchers.IO) {
                coroutineScope {
                    val pc = async { sectionChildRepository.countOldV2UserImagePaths() }
                    val sc = async { sectionChildRepository.countOldV2SignaturePaths() }
                    val pdfFileCount = async { countOldV1PdfsAndPopulateList() }
                    Triple(pc.await(), sc.await(), pdfFileCount.await())
                }
            }
            if (photoCount == 0 && sigCount == 0 && pdfCount == 0) {
                prefsManager.setV3AllV2FilesMigratedToV3FilesStructure()
                return@launch
            }
            pendingPhotoCount = photoCount
            pendingSigCount   = sigCount
            pendingPdfCount   = pdfCount

            // If we have photos/signatures OR PDFs were found via File/MediaStore, request permission for silent migration
            if (pendingPhotoCount > 0 || pendingSigCount > 0 || pdfsFoundViaFile || discoveredMediaStorePdfs.isNotEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // On Android 13+, we rely on preserveLegacyExternalStorage to read our own files silently.
                    // No storage permission is declared in Manifest for API 33+.
                    runMigration(null)
                } else {
                    val permName = Manifest.permission.READ_EXTERNAL_STORAGE
                    val granted = ContextCompat.checkSelfPermission(context, permName) == PackageManager.PERMISSION_GRANTED
                    if (granted) runMigration(null)
                    else _migrationState.value = MigrationUiState.ShowRationale
                }
            } else if (pendingPdfCount > 0) {
                // PDFs exist but couldn't even be listed via File or MediaStore — show SAF picker
                _migrationState.value = MigrationUiState.ShowSafFolderPicker
            }
        }
    }

    fun acknowledgeMigrationDenied() {
        _migrationState.value = MigrationUiState.Finished
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            viewModelScope.launch { runMigration(null) }
        } else {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    coroutineScope {
                        if (pendingPhotoCount > 0) launch { sectionChildRepository.clearOldV2UserImagePaths() }
                        if (pendingSigCount > 0) launch { sectionChildRepository.clearOldV2SignatureImagePaths() }
                    }
                }
                // If hidden PDFs still exist and weren't found, show SAF picker
                if (pendingPdfCount > 0 && discoveredMediaStorePdfs.isEmpty() && !pdfsFoundViaFile) {
                    _migrationState.value = MigrationUiState.ShowSafFolderPicker
                } else {
                    prefsManager.setV3AllV2FilesMigratedToV3FilesStructure()
                    analyticsManager.logFileMigratePermissionDenied()
                    _migrationState.value = MigrationUiState.PermissionDenied
                }
            }
        }
    }

    fun onSafFolderSelected(treeUri: Uri) {
        viewModelScope.launch { runMigration(treeUri) }
    }

    private suspend fun runMigration(safTreeUri: Uri?) {
        val total = pendingPhotoCount + pendingSigCount + pendingPdfCount
        var done  = 0
        analyticsManager.logFileMigrateStarted()
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
            }

            val dstPdfFolder = File(v3Base, SrDir.GENERATED_RESUME).also { it.mkdirs() }

            // STRATEGY A: MediaStore List Migration (Silent Background)
            if (discoveredMediaStorePdfs.isNotEmpty()) {
                discoveredMediaStorePdfs.forEach { (uri, name) ->
                    runCatching {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            File(dstPdfFolder, name).outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                    done++
                    _migrationState.value = MigrationUiState.Running(done, total)
                }
            }
            // STRATEGY B: SAF Tree Picker Migration (User Intent Fallback)
            else if (safTreeUri != null) {
                val documentFolder = DocumentFile.fromTreeUri(context, safTreeUri)
                documentFolder?.listFiles()?.forEach { docFile ->
                    val name = docFile.name?.takeIf {
                        docFile.isFile && it.endsWith(".pdf", ignoreCase = true)
                    } ?: return@forEach
                    runCatching {
                        context.contentResolver.openInputStream(docFile.uri)?.use { input ->
                            File(dstPdfFolder, name).outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        docFile.delete()
                    }
                }
                done = total
                _migrationState.value = MigrationUiState.Running(done, total)
            }
            // STRATEGY C: Legacy Local Direct File Loop
            else {
                val filesSrc = File(v1Base, "Files")
                if (filesSrc.exists()) {
                    filesSrc.walk()
                        .filter { it.isFile && it.name.endsWith(".pdf", ignoreCase = true) }
                        .forEach { file ->
                            runCatching {
                                file.copyTo(File(dstPdfFolder, file.name), overwrite = true)
                                file.delete()
                            }
                            done++
                            _migrationState.value = MigrationUiState.Running(done, total)
                        }
                }
            }
        }
        prefsManager.setV3AllV2FilesMigratedToV3FilesStructure()
        analyticsManager.logFileMigrateFinished()
        _migrationState.value = MigrationUiState.Finished
    }

    private suspend fun createExampleProfile() {
        withContext(Dispatchers.IO) {
            val json = context.assets
                .open("${AssetDir.JSON}/${AssetFile.EXAMPLE_RESUMES_JSON}")
                .bufferedReader().use { it.readText() }
            val data = Gson().fromJson(json, ExampleResumesJson::class.java)

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
    }

    private suspend fun createNewProfile() {
        withContext(Dispatchers.IO) {
            val defaultFormat = resumeFormatRepository.getDefault()
                ?: resumeFormatRepository.getAll().first().firstOrNull()
                ?: return@withContext

            val existingProfiles = userProfileRepository.getAllOnce()
            val nextPosition = if (existingProfiles.isEmpty()) 0
            else existingProfiles.maxOf { it.indexPosition } + 1

            val profileId = userProfileRepository.insert(
                UserProfile(
                    id = 0,
                    name = "My Profile",
                    indexPosition = nextPosition,
                    isSampleProfile = false,
                    sampleProfileId = -1,
                    resumeFormatBaseId = defaultFormat.id,
                    fontStyle = defaultFormat.fontStyle,
                    fontSize = defaultFormat.fontSize,
                    backgroundColor = defaultFormat.backgroundColor,
                    resumeFileName = "My Profile"
                )
            )

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
        }
    }

    fun onScreenOpened() {
        viewModelScope.launch {
            val isV3NewInstall = prefsManager.v3IsPerfectNewSrv3User.first()
            val isV2NewInstall = prefsManager.v2IsPerfectNewSrv2User.first()
            analyticsManager.setUserInstallType(isV3NewInstall, isV2NewInstall)
            analyticsManager.logHomeScreenViewed(
                isNewUser   = !isV2NewInstall,
                versionCode = BuildConfig.VERSION_CODE,
                versionName = BuildConfig.VERSION_NAME
            )
        }
    }

    fun onNavSampleResumesClicked() { analyticsManager.logNavSampleResumes() }
    fun onNavNotificationsClicked() { analyticsManager.logNavNotifications() }
    fun onNavResumeTipsClicked()    { analyticsManager.logNavResumeTips() }
    fun onNavAppSettingsClicked()   { analyticsManager.logNavAppSettings() }
    fun onNavFeedbackClicked()      { analyticsManager.logNavFeedback() }
    fun onNavPrivacyPolicyClicked() { analyticsManager.logNavPrivacyPolicy() }
    fun onNavRateUsClicked()        { analyticsManager.logNavRateUs() }
    fun onNavInviteFriendsClicked() { analyticsManager.logNavInviteFriends() }

    fun sendFeedback(email: String, feedback: String) {
        analyticsManager.logFeedbackSubmitted()
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

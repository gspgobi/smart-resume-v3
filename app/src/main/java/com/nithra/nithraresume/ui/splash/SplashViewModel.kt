package com.nithra.nithraresume.ui.splash

import android.content.Context
import android.os.Environment
import android.provider.Settings
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
import com.nithra.nithraresume.data.repository.ResumeFormatRepository
import com.nithra.nithraresume.data.repository.SectionChildRepository
import com.nithra.nithraresume.data.repository.SectionHeadRepository
import com.nithra.nithraresume.data.repository.UserProfileRepository
import com.nithra.nithraresume.utils.AnalyticsManager
import com.nithra.nithraresume.utils.AssetDir
import com.nithra.nithraresume.utils.AssetFile
import com.nithra.nithraresume.utils.PrefsManager
import com.nithra.nithraresume.utils.SrDir
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

// ── JSON data classes for exampleResumes.json ─────────────────────────────────

private data class ExampleResumesJson(val userProfileArrayList: List<ExampleProfileJson>?)
private data class ExampleProfileJson(
    val upName: String?,
    val upIsSampleProfile: Boolean = false,
    val sampleProfileId: Int? = null,
    val resumeFormatBaseId: Int = 1,
    val upFontStyle: String?,
    val upFontSize: Int = 12,
    val upBackgroundColor: String?,
    val upResumeFileName: String?,
    val sectionHeadAddedArrayList: List<ExampleShaJson>?
)
private data class ExampleShaJson(
    val sectionHeadGroupBaseId: Int = 1,
    val sectionHeadBaseId: Int = 1,
    val sectionHeadSampleDataId: Int?,
    val shaTitle: String?,
    val shaIsEnable: Boolean = true,
    val shaIndexPosition: Int = 0,
    val sectionChild1: ExampleSc1Json?,
    val sectionChild2ArrayList: List<ExampleSc2Json>?,
    val sectionChild3ArrayList: List<ExampleSc3Json>?,
    val sectionChild4: ExampleSc4Json?,
    val sectionChild5: ExampleSc5Json?,
    val sectionChild6ArrayList: List<ExampleSc6Json>?,
    val sectionChild7ArrayList: List<ExampleSc7Json>?,
    val sectionChild8: ExampleSc8Json?
)
private data class ExampleSc1Json(
    val sc1Name: String?, val sc1Address: String?, val sc1Email: String?, val sc1Phone: String?,
    val sc1Gender: String?, val sc1Dob: String?, val sc1DobDateFormat: String?,
    val sc1Nationality: String?, val sc1UserImagePath: String?, val sc1IsUserImageEnable: Boolean = false
)
private data class ExampleSc2Json(
    val sc2IndexPosition: Int = 0, val sc2WorkRole: String?, val sc2CompanyName: String?,
    val sc2Subtitle: String?, val sc2WorkPeriod: String?,
    val sc2Accomplishments: String?, val sc2AccomplishmentsBulletType: String?
)
private data class ExampleSc3Json(
    val sc3IndexPosition: Int = 0, val sc3StudyDegree: String?, val sc3SchoolName: String?,
    val sc3Subtitle: String?, val sc3StudyPeriod: String?,
    val sc3Concentrates: String?, val sc3ConcentratesBulletType: String?
)
private data class ExampleSc4Json(
    val sc4DeclarationContent: String?, val sc4DeclarationContentBulletType: String?,
    val sc4Date: String?, val sc4DateDateFormat: String?, val sc4Place: String?,
    val sc4SignatureImagePath: String?, val sc4IsSignatureImageEnable: Boolean = false
)
private data class ExampleSc5Json(val sc5Content: String?, val sc5ContentBulletType: String?)
private data class ExampleSc6Json(
    val sc6IndexPosition: Int = 0, val sc6ContentTitle: String?, val sc6ContentDetail: String?
)
private data class ExampleSc7Json(
    val sc7IndexPosition: Int = 0, val sc7ContentTitle: String?, val sc7ContentSubtitle: String?,
    val sc7ContentDetail: String?, val sc7ContentDetailBulletType: String?
)
private data class ExampleSc8Json(
    val sc8Date: String?, val sc8DateDateFormat: String?,
    val sc8Address: String?, val sc8Content: String?
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefsManager: PrefsManager,
    private val apiRepository: ApiRepository,
    private val analyticsManager: AnalyticsManager,
    private val userProfileRepository: UserProfileRepository,
    private val resumeFormatRepository: ResumeFormatRepository,
    private val sectionHeadRepository: SectionHeadRepository,
    private val sectionChildRepository: SectionChildRepository
) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady

    init {
        viewModelScope.launch {
            performAppInit()
            _isReady.value = true
        }
    }

    private suspend fun performAppInit() {
        prefsManager.dumpPrefs()

        val currentVersionCode = BuildConfig.VERSION_CODE
        val storedVersionCode = prefsManager.v2CurrentAppVersionCode.first()

        if (prefsManager.v2IsPerfectNewSrv2User.first().not()) {
            createExampleProfile()
            createNewProfile()
        }

        if (storedVersionCode != currentVersionCode) {
            prefsManager.setV2CurrentAppVersionCode(currentVersionCode)
        }

        migrateV2FilesIfNeeded()

        // Set Android ID as Firebase Analytics user property
        val androidId = Settings.Secure.getString(
            context.contentResolver, Settings.Secure.ANDROID_ID
        ).orEmpty()
        analyticsManager.setUserId(androidId)

        // Retry FCM token registration if it never reached the server
        val tokenSent = prefsManager.v2FcmTokenSentToServer.first()
        if (!tokenSent) {
            val token = prefsManager.v2FcmTokenId.first()
            if (token.isNotEmpty()) {
                apiRepository.registerFcmToken(token, firstOrUpdate = "update")
            }
        }
    }

    private suspend fun migrateV2FilesIfNeeded() {
        if (prefsManager.v3AllV2FilesMigratedToV3FilesStructure.first()) return

        withContext(Dispatchers.IO) {
            val v1Base = File(Environment.getExternalStorageDirectory(), "Nithra/SmartResume")
            val v3Base = context.getExternalFilesDir(null) ?: return@withContext

            // Photo → UserImage
            val photoSrc = File(v1Base, "Photo")
            val userImageDst = File(v3Base, SrDir.USER_IMAGE).also { it.mkdirs() }
            if (photoSrc.exists()) {
                photoSrc.listFiles()?.forEach { it.copyTo(File(userImageDst, it.name), overwrite = true) }
                photoSrc.deleteRecursively()
            }

            // Signature → Signature
            val signatureSrc = File(v1Base, "Signature")
            val signatureDst = File(v3Base, SrDir.SIGNATURE).also { it.mkdirs() }
            if (signatureSrc.exists()) {
                signatureSrc.listFiles()?.forEach { it.copyTo(File(signatureDst, it.name), overwrite = true) }
                signatureSrc.deleteRecursively()
            }

            // Files → GeneratedResume
            val filesSrc = File(v1Base, "Files")
            val generatedDst = File(v3Base, SrDir.GENERATED_RESUME).also { it.mkdirs() }
            if (filesSrc.exists()) {
                filesSrc.listFiles()?.forEach { it.copyTo(File(generatedDst, it.name), overwrite = true) }
                filesSrc.deleteRecursively()
            }

            // Clean up the entire v1 base directory
            v1Base.deleteRecursively()

            // Update DB paths from v1 absolute paths to v3 absolute paths
            sectionChildRepository.migrateV2UserImagePaths(userImageDst.absolutePath)
            sectionChildRepository.migrateV2SignatureImagePaths(signatureDst.absolutePath)
        }

        prefsManager.setV3AllV2FilesMigratedToV3FilesStructure()
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
}

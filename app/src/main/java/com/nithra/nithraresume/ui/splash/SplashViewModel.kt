package com.nithra.nithraresume.ui.splash

import android.content.Context
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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

        if (storedVersionCode == 0) {
            analyticsManager.logFirstLaunch()
        }
        if (storedVersionCode != currentVersionCode) {
            prefsManager.setV2CurrentAppVersionCode(currentVersionCode)
        }

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


}

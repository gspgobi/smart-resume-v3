package com.nithra.nithraresume.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    companion object {
        // ── Common ─────────────────────────────────────────────────────────────
        private const val EVENT_SCREEN_VIEW          = "screen_view"
        private const val EVENT_FIRST_LAUNCH         = "first_launch"
        private const val EVENT_FEEDBACK_SUBMITTED   = "feedback_submitted"

        // ── Home / Navigation drawer ───────────────────────────────────────────
        private const val EVENT_HOME_SCREEN_VIEWED   = "home_screen_viewed"
        private const val EVENT_NAV_SAMPLE_RESUMES   = "nav_sample_resumes_interacted"
        private const val EVENT_NAV_NOTIFICATIONS    = "nav_notifications_interacted"
        private const val EVENT_NAV_RESUME_TIPS      = "nav_resume_making_tips_interacted"
        private const val EVENT_NAV_APP_SETTINGS     = "nav_app_settings_interacted"
        private const val EVENT_NAV_FEEDBACK         = "nav_feedback_interacted"
        private const val EVENT_NAV_PRIVACY_POLICY   = "nav_privacy_policy_interacted"
        private const val EVENT_NAV_RATE_US          = "nav_rate_us_interacted"
        private const val EVENT_NAV_INVITE_FRIENDS   = "nav_invite_friends_interacted"

        // ── Rate Us ────────────────────────────────────────────────────────────
        private const val EVENT_RATE_US_LOVE_IT      = "rate_us_love_it"
        private const val EVENT_RATE_US_COULD_BETTER = "rate_us_could_be_better"
        private const val EVENT_RATE_US_RATE_NOW     = "rate_us_rate_now"
        private const val EVENT_RATE_US_LATER        = "rate_us_later"
        private const val EVENT_RATE_US_NO_THANKS    = "rate_us_no_thanks"

        // ── Generate Resume (GR) ───────────────────────────────────────────────
        private const val EVENT_RESUME_GENERATED     = "resume_generated"
        private const val EVENT_RESUME_FAILED        = "resume_generation_failed"
        private const val EVENT_GR_RESET_TO_DEFAULT  = "gr_reset_to_default_interacted"

        // ── Profile ────────────────────────────────────────────────────────────
        private const val EVENT_PROFILE_CREATED      = "profile_created"
        private const val EVENT_UP_RENAME_PROFILE    = "up_rename_profile_interacted"
        private const val EVENT_UP_DELETE_PROFILE    = "up_delete_profile_interacted"
        private const val EVENT_UP_SAMPLE_RESUMES    = "up_sample_resumes_interacted"

        // ── Section Head (SHA) ─────────────────────────────────────────────────
        private const val EVENT_SHA_ADD_NEW_SECTION        = "sha_add_new_section_interacted"
        private const val EVENT_SHA_ADD_NEW_ADDON          = "sha_add_new_addon_interacted"
        private const val EVENT_SHA_DELETE_SECTION         = "sha_delete_section_interacted"
        private const val EVENT_SHA_DELETE_ADDON           = "sha_delete_addon_interacted"
        private const val EVENT_SHA_ENABLE_DISABLE_SECTION = "sha_enable_disable_section_interacted"
        private const val EVENT_SHA_ENABLE_DISABLE_ADDON   = "sha_enable_disable_addon_interacted"
        private const val EVENT_SHA_RESUME_FORMAT          = "sha_resume_format_interacted"
        private const val EVENT_SHA_GENERATE_RESUME        = "sha_generate_resume_interacted"
        private const val EVENT_SHA_VIEW_SHARE             = "sha_view_share_interacted"

        // ── View / Share (VS) ─────────────────────────────────────────────────
        private const val EVENT_VS_FILE_VIEW         = "vs_file_view_interacted"
        private const val EVENT_VS_FILE_SHARE        = "vs_file_share_interacted"
        private const val EVENT_VS_FILE_RENAME       = "vs_file_rename_interacted"
        private const val EVENT_VS_FILE_DELETE       = "vs_file_delete_interacted"

        // ── Resume Format (RF) ─────────────────────────────────────────────────
        private const val EVENT_RF_FORMAT_SELECT     = "rf_format_select_interacted"
        private const val EVENT_RF_FORMAT_PREVIEW    = "rf_format_preview_interacted"

        // ── Sample Resumes (SR) ────────────────────────────────────────────────
        private const val EVENT_SR_SAMPLE_PREVIEW    = "sr_sample_preview_interacted"

        // ── Notification List (NL) ─────────────────────────────────────────────
        private const val EVENT_NL_NOTIFICATION_SELECT     = "nl_notification_select_interacted"
        private const val EVENT_NL_NOTIFICATION_DELETE     = "nl_notification_delete_interacted"
        private const val EVENT_NL_NOTIFICATION_DELETE_ALL = "nl_notification_delete_all_interacted"

        // ── App Settings (AS) ─────────────────────────────────────────────────
        private const val EVENT_AS_NOTIF_SWITCH      = "as_notif_alert_switch_interacted"

        // ── Section Child 1 (SC1) ─────────────────────────────────────────────
        private const val EVENT_SC1_BROWSE_IMAGE     = "sc1_browse_new_image_interacted"
        private const val EVENT_SC1_DELETE_IMAGE     = "sc1_delete_image_interacted"
        private const val EVENT_SC1_SAVE             = "sc1_save_interacted"

        // ── Section Child 2 (SC2) ─────────────────────────────────────────────
        private const val EVENT_SC2_DELETE_DETAIL    = "sc2_delete_detail_interacted"
        private const val EVENT_SC2_SAVE_TITLE       = "sc2_save_title_interacted"
        private const val EVENT_SC2SUB_SAVE          = "sc2sub_save_interacted"

        // ── Section Child 3 (SC3) ─────────────────────────────────────────────
        private const val EVENT_SC3_DELETE_DETAIL    = "sc3_delete_detail_interacted"
        private const val EVENT_SC3_SAVE_TITLE       = "sc3_save_title_interacted"
        private const val EVENT_SC3SUB_SAVE          = "sc3sub_save_interacted"

        // ── Section Child 4 (SC4) ─────────────────────────────────────────────
        private const val EVENT_SC4_SAVE             = "sc4_save_interacted"
        private const val EVENT_SC4_NEW_SIGNATURE    = "sc4_new_signature_interacted"
        private const val EVENT_SC4_DELETE_SIGNATURE = "sc4_delete_signature_interacted"
        private const val EVENT_SC4SIG_SAVE          = "sc4sub_save_signature_interacted"

        // ── Section Child 5 (SC5) ─────────────────────────────────────────────
        private const val EVENT_SC5_SAVE             = "sc5_save_interacted"

        // ── Section Child 6 (SC6) ─────────────────────────────────────────────
        private const val EVENT_SC6_DELETE_DETAIL    = "sc6_delete_detail_interacted"
        private const val EVENT_SC6_SAVE_TITLE       = "sc6_save_title_interacted"
        private const val EVENT_SC6SUB_SAVE          = "sc6sub_save_interacted"

        // ── Section Child 7 (SC7) ─────────────────────────────────────────────
        private const val EVENT_SC7_DELETE_DETAIL    = "sc7_delete_detail_interacted"
        private const val EVENT_SC7_SAVE_TITLE       = "sc7_save_title_interacted"
        private const val EVENT_SC7SUB_SAVE          = "sc7sub_save_interacted"

        // ── Section Child 8 (SC8) ─────────────────────────────────────────────
        private const val EVENT_SC8_SAVE             = "sc8_save_interacted"

        // ── Param keys ────────────────────────────────────────────────────────
        private const val PARAM_SCREEN_NAME          = "screen_name"
        private const val PARAM_IS_NEW_USER          = "is_new_user"
        private const val PARAM_APP_VERSION_CODE     = "app_version_code"
        private const val PARAM_APP_VERSION_NAME     = "app_version_name"
        private const val PARAM_RESUME_FORMAT_ID     = "resume_format_id"
        private const val PARAM_FONT_STYLE           = "font_style"
        private const val PARAM_FONT_SIZE            = "font_size"
        private const val PARAM_BACKGROUND_COLOR     = "background_color"
        private const val PARAM_FILE_NAME            = "file_name"
        private const val PARAM_IS_FROM_SAMPLE       = "is_from_sample"
        private const val PARAM_SECTION_HEAD_BASE_ID = "section_head_base_id"
        private const val PARAM_ENABLE_OR_DISABLE    = "enable_or_disable"
        private const val PARAM_SAMPLE_PROFILE_ID    = "sample_profile_id"
        private const val PARAM_IS_NOTIFICATION_ENABLE = "is_notification_enable"
    }

    // ── User identity ─────────────────────────────────────────────────────────

    fun setUserId(androidId: String) {
        if (androidId.isEmpty()) return
        analytics.setUserId(androidId)
        analytics.setUserProperty("android_id", androidId)
    }

    // ── Screen view ───────────────────────────────────────────────────────────

    fun logScreenView(screenName: String) {
        analytics.logEvent(EVENT_SCREEN_VIEW, Bundle().apply {
            putString(PARAM_SCREEN_NAME, screenName)
        })
    }

    // ── Home / Launch ─────────────────────────────────────────────────────────

    fun logFirstLaunch() = analytics.logEvent(EVENT_FIRST_LAUNCH, null)

    fun logHomeScreenViewed(isNewUser: Boolean, versionCode: Int, versionName: String) {
        analytics.logEvent(EVENT_HOME_SCREEN_VIEWED, Bundle().apply {
            putBoolean(PARAM_IS_NEW_USER, isNewUser)
            putInt(PARAM_APP_VERSION_CODE, versionCode)
            putString(PARAM_APP_VERSION_NAME, versionName)
        })
    }

    // ── Navigation drawer ─────────────────────────────────────────────────────

    fun logNavSampleResumes()  = analytics.logEvent(EVENT_NAV_SAMPLE_RESUMES, null)
    fun logNavNotifications()  = analytics.logEvent(EVENT_NAV_NOTIFICATIONS, null)
    fun logNavResumeTips()     = analytics.logEvent(EVENT_NAV_RESUME_TIPS, null)
    fun logNavAppSettings()    = analytics.logEvent(EVENT_NAV_APP_SETTINGS, null)
    fun logNavFeedback()       = analytics.logEvent(EVENT_NAV_FEEDBACK, null)
    fun logNavPrivacyPolicy()  = analytics.logEvent(EVENT_NAV_PRIVACY_POLICY, null)
    fun logNavRateUs()         = analytics.logEvent(EVENT_NAV_RATE_US, null)
    fun logNavInviteFriends()  = analytics.logEvent(EVENT_NAV_INVITE_FRIENDS, null)

    // ── Rate Us ───────────────────────────────────────────────────────────────

    fun logRateUsLoveIt()        = analytics.logEvent(EVENT_RATE_US_LOVE_IT, null)
    fun logRateUsCouldBeBetter() = analytics.logEvent(EVENT_RATE_US_COULD_BETTER, null)
    fun logRateUsRateNow()       = analytics.logEvent(EVENT_RATE_US_RATE_NOW, null)
    fun logRateUsLater()         = analytics.logEvent(EVENT_RATE_US_LATER, null)
    fun logRateUsNoThanks()      = analytics.logEvent(EVENT_RATE_US_NO_THANKS, null)

    // ── Feedback ──────────────────────────────────────────────────────────────

    fun logFeedbackSubmitted() = analytics.logEvent(EVENT_FEEDBACK_SUBMITTED, null)

    // ── Resume generation ─────────────────────────────────────────────────────

    fun logResumeGenerated(formatId: Int, fontStyle: String, fontSize: Int, bgColor: String, fileName: String) {
        analytics.logEvent(EVENT_RESUME_GENERATED, Bundle().apply {
            putInt(PARAM_RESUME_FORMAT_ID, formatId)
            putString(PARAM_FONT_STYLE, fontStyle)
            putInt(PARAM_FONT_SIZE, fontSize)
            putString(PARAM_BACKGROUND_COLOR, bgColor)
            putString(PARAM_FILE_NAME, fileName)
        })
    }

    fun logResumeGenerationFailed() = analytics.logEvent(EVENT_RESUME_FAILED, null)
    fun logGrResetToDefault()       = analytics.logEvent(EVENT_GR_RESET_TO_DEFAULT, null)

    // ── Profile ───────────────────────────────────────────────────────────────

    fun logProfileCreated(isFromSample: Boolean) {
        analytics.logEvent(EVENT_PROFILE_CREATED, Bundle().apply {
            putBoolean(PARAM_IS_FROM_SAMPLE, isFromSample)
        })
    }

    fun logUpRenameProfile()   = analytics.logEvent(EVENT_UP_RENAME_PROFILE, null)
    fun logUpDeleteProfile()   = analytics.logEvent(EVENT_UP_DELETE_PROFILE, null)
    fun logUpSampleResumes()   = analytics.logEvent(EVENT_UP_SAMPLE_RESUMES, null)

    // ── Section Head ──────────────────────────────────────────────────────────

    fun logShaAddNewSection()  = analytics.logEvent(EVENT_SHA_ADD_NEW_SECTION, null)
    fun logShaAddNewAddon()    = analytics.logEvent(EVENT_SHA_ADD_NEW_ADDON, null)

    fun logShaDeleteSection(sectionHeadBaseId: Int) {
        analytics.logEvent(EVENT_SHA_DELETE_SECTION, Bundle().apply {
            putInt(PARAM_SECTION_HEAD_BASE_ID, sectionHeadBaseId)
        })
    }

    fun logShaDeleteAddon(sectionHeadBaseId: Int) {
        analytics.logEvent(EVENT_SHA_DELETE_ADDON, Bundle().apply {
            putInt(PARAM_SECTION_HEAD_BASE_ID, sectionHeadBaseId)
        })
    }

    fun logShaEnableDisableSection(enabled: Boolean) {
        analytics.logEvent(EVENT_SHA_ENABLE_DISABLE_SECTION, Bundle().apply {
            putString(PARAM_ENABLE_OR_DISABLE, if (enabled) "Enable" else "Disable")
        })
    }

    fun logShaEnableDisableAddon(enabled: Boolean) {
        analytics.logEvent(EVENT_SHA_ENABLE_DISABLE_ADDON, Bundle().apply {
            putString(PARAM_ENABLE_OR_DISABLE, if (enabled) "Enable" else "Disable")
        })
    }

    fun logShaResumeFormat()   = analytics.logEvent(EVENT_SHA_RESUME_FORMAT, null)
    fun logShaGenerateResume() = analytics.logEvent(EVENT_SHA_GENERATE_RESUME, null)
    fun logShaViewShare()      = analytics.logEvent(EVENT_SHA_VIEW_SHARE, null)

    // ── View / Share ──────────────────────────────────────────────────────────

    fun logVsFileView()   = analytics.logEvent(EVENT_VS_FILE_VIEW, null)
    fun logVsFileShare()  = analytics.logEvent(EVENT_VS_FILE_SHARE, null)
    fun logVsFileRename() = analytics.logEvent(EVENT_VS_FILE_RENAME, null)
    fun logVsFileDelete() = analytics.logEvent(EVENT_VS_FILE_DELETE, null)

    // ── Resume Format ─────────────────────────────────────────────────────────

    fun logRfFormatSelect(formatBaseId: Int) {
        analytics.logEvent(EVENT_RF_FORMAT_SELECT, Bundle().apply {
            putInt(PARAM_RESUME_FORMAT_ID, formatBaseId)
        })
    }

    fun logRfFormatPreview(formatPreviewId: Int) {
        analytics.logEvent(EVENT_RF_FORMAT_PREVIEW, Bundle().apply {
            putInt(PARAM_RESUME_FORMAT_ID, formatPreviewId)
        })
    }

    // ── Sample Resumes ────────────────────────────────────────────────────────

    fun logSrSamplePreview(sampleProfileId: Int) {
        analytics.logEvent(EVENT_SR_SAMPLE_PREVIEW, Bundle().apply {
            putInt(PARAM_SAMPLE_PROFILE_ID, sampleProfileId)
        })
    }

    // ── Notification List ─────────────────────────────────────────────────────

    fun logNlNotificationSelect()    = analytics.logEvent(EVENT_NL_NOTIFICATION_SELECT, null)
    fun logNlNotificationDelete()    = analytics.logEvent(EVENT_NL_NOTIFICATION_DELETE, null)
    fun logNlNotificationDeleteAll() = analytics.logEvent(EVENT_NL_NOTIFICATION_DELETE_ALL, null)

    // ── App Settings ──────────────────────────────────────────────────────────

    fun logAsNotifSwitch(enabled: Boolean) {
        analytics.logEvent(EVENT_AS_NOTIF_SWITCH, Bundle().apply {
            putBoolean(PARAM_IS_NOTIFICATION_ENABLE, enabled)
        })
    }

    // ── Section Child 1 ───────────────────────────────────────────────────────

    fun logSc1BrowseImage() = analytics.logEvent(EVENT_SC1_BROWSE_IMAGE, null)
    fun logSc1DeleteImage() = analytics.logEvent(EVENT_SC1_DELETE_IMAGE, null)
    fun logSc1Save()        = analytics.logEvent(EVENT_SC1_SAVE, null)

    // ── Section Child 2 ───────────────────────────────────────────────────────

    fun logSc2DeleteDetail() = analytics.logEvent(EVENT_SC2_DELETE_DETAIL, null)
    fun logSc2SaveTitle()    = analytics.logEvent(EVENT_SC2_SAVE_TITLE, null)
    fun logSc2SubSave()      = analytics.logEvent(EVENT_SC2SUB_SAVE, null)

    // ── Section Child 3 ───────────────────────────────────────────────────────

    fun logSc3DeleteDetail() = analytics.logEvent(EVENT_SC3_DELETE_DETAIL, null)
    fun logSc3SaveTitle()    = analytics.logEvent(EVENT_SC3_SAVE_TITLE, null)
    fun logSc3SubSave()      = analytics.logEvent(EVENT_SC3SUB_SAVE, null)

    // ── Section Child 4 ───────────────────────────────────────────────────────

    fun logSc4Save()            = analytics.logEvent(EVENT_SC4_SAVE, null)
    fun logSc4NewSignature()    = analytics.logEvent(EVENT_SC4_NEW_SIGNATURE, null)
    fun logSc4DeleteSignature() = analytics.logEvent(EVENT_SC4_DELETE_SIGNATURE, null)
    fun logSc4SigSave()         = analytics.logEvent(EVENT_SC4SIG_SAVE, null)

    // ── Section Child 5 ───────────────────────────────────────────────────────

    fun logSc5Save() = analytics.logEvent(EVENT_SC5_SAVE, null)

    // ── Section Child 6 ───────────────────────────────────────────────────────

    fun logSc6DeleteDetail() = analytics.logEvent(EVENT_SC6_DELETE_DETAIL, null)
    fun logSc6SaveTitle()    = analytics.logEvent(EVENT_SC6_SAVE_TITLE, null)
    fun logSc6SubSave()      = analytics.logEvent(EVENT_SC6SUB_SAVE, null)

    // ── Section Child 7 ───────────────────────────────────────────────────────

    fun logSc7DeleteDetail() = analytics.logEvent(EVENT_SC7_DELETE_DETAIL, null)
    fun logSc7SaveTitle()    = analytics.logEvent(EVENT_SC7_SAVE_TITLE, null)
    fun logSc7SubSave()      = analytics.logEvent(EVENT_SC7SUB_SAVE, null)

    // ── Section Child 8 ───────────────────────────────────────────────────────

    fun logSc8Save() = analytics.logEvent(EVENT_SC8_SAVE, null)
}

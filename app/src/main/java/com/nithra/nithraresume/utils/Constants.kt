package com.nithra.nithraresume.utils

import java.io.File

// ── Business limits ───────────────────────────────────────────────────────────

const val MAX_PROFILES          = 20
const val MAX_SECTIONS          = 20
const val MAX_CHILD_ITEMS       = 10

const val GENERATE_COUNT_SHOW_RATE_US = 3
const val GENERATE_COUNT_SHOW_AD      = 4

// ── Section head group IDs (match seed data) ──────────────────────────────────

const val GROUP_ID_SECTIONS = 1
const val GROUP_ID_ADDONS   = 2

// ── Sample data group names ───────────────────────────────────────────────────

const val SHSD_GROUP_STANDARD = "Standard"
const val SHSD_GROUP_CUSTOM   = "Custom"
const val SHSD_GROUP_ADDONS   = "Add-ons"

// ── Obj/Accomp dialog types ───────────────────────────────────────────────────

const val OBJ_ACCOMP_TYPE_OBJECTIVE      = 1
const val OBJ_ACCOMP_TYPE_ACCOMPLISHMENT = 2

// ── Gender options ────────────────────────────────────────────────────────────

const val GENDER_MALE   = "Male"
const val GENDER_FEMALE = "Female"

val ALL_GENDERS = listOf(GENDER_MALE, GENDER_FEMALE)

// ── Bullet types ──────────────────────────────────────────────────────────────

const val BULLET_NONE    = "None"
const val BULLET_REGULAR = "●"

val ALL_BULLET_TYPES = listOf(BULLET_NONE, BULLET_REGULAR)

// ── Resume background colours ─────────────────────────────────────────────────

const val BG_COLOR_WHITE = "White"
const val BG_COLOR_PEACH = "Peach"

val ALL_BG_COLORS = listOf(BG_COLOR_WHITE, BG_COLOR_PEACH)

// ── Date formats (12 active formats from V2) ──────────────────────────────────

val ALL_DATE_FORMATS = listOf(
    "MM/dd/yyyy",
    "MM-dd-yyyy",
    "dd/MM/yyyy",
    "dd-MM-yyyy",
    "MMM dd yyyy",
    "MMM-dd-yyyy",
    "dd MMM yyyy",
    "dd-MMM-yyyy",
    "MMMM dd yyyy",
    "MMMM-dd-yyyy",
    "dd MMMM yyyy",
    "dd-MMMM-yyyy"
)

// ── Font styles (asset file names) ────────────────────────────────────────────

const val FONT_TIMES_NEW_ROMAN = "TIMES NEW ROMAN.TTF"
const val FONT_ARIAL           = "ARIAL.TTF"
const val FONT_VANI            = "VANI.TTF"
const val FONT_DIDOT           = "DIDOT.TTF"
const val FONT_ANTIQUE         = "ANTQUAI.TTF"
const val FONT_AVENIR          = "AVENIR.TTF"
const val FONT_BASKERVILLE     = "BASKVILL.TTF"
const val FONT_VERDANA         = "VERDANA.TTF"

val ALL_FONT_STYLES = listOf(
    FONT_TIMES_NEW_ROMAN,
    FONT_ARIAL,
    FONT_VANI,
    FONT_DIDOT,
    FONT_ANTIQUE,
    FONT_AVENIR,
    FONT_BASKERVILLE,
    FONT_VERDANA
)

const val FONT_SUFFIX_TTF = ".TTF"
const val FONT_SIZE_MIN   = 8
const val FONT_SIZE_MAX   = 20
const val FONT_SIZE_DEFAULT = 12

// ── Asset directories ─────────────────────────────────────────────────────────

object AssetDir {
    const val FONTS                  = "fonts"
    const val JSON                   = "json"
    const val RESUME_FORMAT_PREVIEWS = "resume-format-previews"
    const val SAMPLE_RESUMES         = "sample-resumes"
    const val RESUME_GUIDE           = "resume-guide"
}

object AssetFile {
    const val RESUME_GUIDE_PDF       = "ResumeGuide.pdf"
    const val DUMMY_RESUMES_JSON     = "dummyResumes.json"
    const val EXAMPLE_RESUMES_JSON   = "exampleResumes.json"
    const val SAMPLE_RESUMES_JSON    = "sampleResumes.json"
    const val OBJECTIVES_JSON        = "objectives.json"
    const val ACCOMPLISHMENTS_JSON   = "accomplishments.json"
}

const val RESUME_FORMAT_PREVIEW_ASSET_PREFIX = "ResumeFormatPreview"
const val SAMPLE_RESUME_PREVIEW_ASSET_PREFIX = "SampleResume"
const val DOT_PDF = ".pdf"

// ── File / directory names (scoped storage — getExternalFilesDir) ─────────────

object SrDir {
    const val USER_IMAGE       = "UserImage"
    const val SIGNATURE        = "Signature"
    const val GENERATED_RESUME = "GeneratedResume"
}

object SrImagePrefix {
    const val USER_IMAGE  = "user_image_"
    const val SIGNATURE   = "signature_image_"
}

object SrImageSuffix {
    const val JPG = ".jpg"
    const val PNG = ".png"
}

const val FILE_NAME_RESERVED_CHARS = "|\\/?*<>:+\"[]'{}${"\u0000"}"

// ── FCM data keys (remote message payload) ────────────────────────────────────

object FcmKey {
    const val MSGTYPE       = "msgtype"
    const val NOTITYPE      = "notitype"
    const val TITLE         = "title"
    const val MESSAGE       = "message"
    const val IS_BACKGROUND = "is_background"
    const val IMAGE         = "image"
    const val TIMESTAMP     = "timestamp"
    const val P_NAME        = "p_name"
}

object FcmMsgType {
    const val CONTENT   = "C"
    const val LINK      = "L"
    const val PROMOTION = "P"
}

object FcmNotiType {
    const val BIG_TEXT         = "BT"
    const val BIG_IMAGE        = "BI"
    const val CUSTOM_BIG_TEXT  = "CBT"
    const val CUSTOM_BIG_IMAGE = "CBI"
}

// ── API endpoints (relative to base URL https://www.nithra.mobi/) ─────────────

object ApiEndpoint {
    const val FCM_REGISTER = "smfcm/register.php"
    const val APP_FEEDBACK = "apps/appfeedback.php"
    const val REFERRER     = "apps/referrer.php"
}

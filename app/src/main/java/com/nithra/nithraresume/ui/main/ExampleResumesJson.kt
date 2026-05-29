package com.nithra.nithraresume.ui.main

data class ExampleResumesJson(val userProfileArrayList: List<ExampleProfileJson>?)
data class ExampleProfileJson(
    val upName: String?,
    val upIsSampleProfile: Boolean = true,
    val sampleProfileId: Int = 0,
    val resumeFormatBaseId: Int = 1,
    val upFontStyle: String?,
    val upFontSize: Int = 12,
    val upBackgroundColor: String?,
    val upResumeFileName: String?,
    val sectionHeadAddedArrayList: List<ExampleShaJson>?
)
data class ExampleShaJson(
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
data class ExampleSc1Json(
    val sc1Name: String?, val sc1Address: String?, val sc1Email: String?, val sc1Phone: String?,
    val sc1Gender: String?, val sc1Dob: String?, val sc1DobDateFormat: String?,
    val sc1Nationality: String?, val sc1UserImagePath: String?, val sc1IsUserImageEnable: Boolean = false
)
data class ExampleSc2Json(
    val sc2IndexPosition: Int = 0, val sc2WorkRole: String?, val sc2CompanyName: String?,
    val sc2Subtitle: String?, val sc2WorkPeriod: String?,
    val sc2Accomplishments: String?, val sc2AccomplishmentsBulletType: String?
)
data class ExampleSc3Json(
    val sc3IndexPosition: Int = 0, val sc3StudyDegree: String?, val sc3SchoolName: String?,
    val sc3Subtitle: String?, val sc3StudyPeriod: String?,
    val sc3Concentrates: String?, val sc3ConcentratesBulletType: String?
)
data class ExampleSc4Json(
    val sc4DeclarationContent: String?, val sc4DeclarationContentBulletType: String?,
    val sc4Date: String?, val sc4DateDateFormat: String?, val sc4Place: String?,
    val sc4SignatureImagePath: String?, val sc4IsSignatureImageEnable: Boolean = false
)
data class ExampleSc5Json(val sc5Content: String?, val sc5ContentBulletType: String?)
data class ExampleSc6Json(
    val sc6IndexPosition: Int = 0, val sc6ContentTitle: String?, val sc6ContentDetail: String?
)
data class ExampleSc7Json(
    val sc7IndexPosition: Int = 0, val sc7ContentTitle: String?, val sc7ContentSubtitle: String?,
    val sc7ContentDetail: String?, val sc7ContentDetailBulletType: String?
)
data class ExampleSc8Json(
    val sc8Date: String?, val sc8DateDateFormat: String?,
    val sc8Address: String?, val sc8Content: String?
)

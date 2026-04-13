package com.nithra.nithraresume.data.model

import com.nithra.nithraresume.data.db.entity.UserProfileEntity

data class UserProfile(
    val id: Int = 0,
    val name: String,
    val indexPosition: Int,
    val isSampleProfile: Boolean,
    val sampleProfileId: Int?,
    val resumeFormatBaseId: Int,
    val fontStyle: String,
    val fontSize: Int,
    val backgroundColor: String,
    val resumeFileName: String?
)

fun UserProfileEntity.toModel() = UserProfile(
    id = userProfileId,
    name = upName,
    indexPosition = upIndexPosition,
    isSampleProfile = upIsSampleProfile,
    sampleProfileId = sampleProfileId,
    resumeFormatBaseId = resumeFormatBaseId,
    fontStyle = upFontStyle,
    fontSize = upFontSize,
    backgroundColor = upBackgroundColor.orEmpty(),
    resumeFileName = upResumeFileName
)

fun UserProfile.toEntity() = UserProfileEntity(
    userProfileId = id,
    upName = name,
    upIndexPosition = indexPosition,
    upIsSampleProfile = isSampleProfile,
    sampleProfileId = sampleProfileId,
    resumeFormatBaseId = resumeFormatBaseId,
    upFontStyle = fontStyle,
    upFontSize = fontSize,
    upBackgroundColor = backgroundColor,
    upResumeFileName = resumeFileName
)
// update 101

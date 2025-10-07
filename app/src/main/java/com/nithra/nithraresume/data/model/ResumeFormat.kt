package com.nithra.nithraresume.data.model

import com.nithra.nithraresume.data.db.entity.ResumeFormatBaseEntity

data class ResumeFormat(
    val id: Int = 0,
    val title: String,
    val description: String,
    val isDefault: Boolean,
    val fontStyle: String,
    val fontSize: Int,
    val backgroundColor: String
)

fun ResumeFormatBaseEntity.toModel() = ResumeFormat(
    id = resumeFormatBaseId,
    title = resumeFormatBaseTitle.orEmpty(),
    description = resumeFormatBaseDescription.orEmpty(),
    isDefault = resumeFormatBaseIsDefault,
    fontStyle = upFontStyle,
    fontSize = upFontSize,
    backgroundColor = upBackgroundColor.orEmpty()
)

fun ResumeFormat.toEntity() = ResumeFormatBaseEntity(
    resumeFormatBaseId = id,
    resumeFormatBaseTitle = title,
    resumeFormatBaseDescription = description,
    resumeFormatBaseIsDefault = isDefault,
    upFontStyle = fontStyle,
    upFontSize = fontSize,
    upBackgroundColor = backgroundColor
)
// update 88

package com.nithra.nithraresume.data.model

import com.nithra.nithraresume.data.db.entity.SectionChild4Entity

/** Declaration + Signature */
data class SectionChild4(
    val id: Int = 0,
    val sectionHeadAddedId: Int,
    val declarationContent: String,
    val declarationContentBulletType: String,
    val date: String,
    val dateDateFormat: String,
    val place: String,
    val signatureImagePath: String,
    val isSignatureImageEnable: Boolean
)

fun SectionChild4Entity.toModel() = SectionChild4(
    id = sectionChild4Id,
    sectionHeadAddedId = sectionHeadAddedId,
    declarationContent = sc4DeclarationContent.orEmpty(),
    declarationContentBulletType = sc4DeclarationContentBulletType.orEmpty(),
    date = sc4Date.orEmpty(),
    dateDateFormat = sc4DateDateFormat.orEmpty(),
    place = sc4Place.orEmpty(),
    signatureImagePath = sc4SignatureImagePath.orEmpty(),
    isSignatureImageEnable = (sc4IsSignatureImageEnable ?: 0) == 1
)

fun SectionChild4.toEntity() = SectionChild4Entity(
    sectionChild4Id = id,
    sectionHeadAddedId = sectionHeadAddedId,
    sc4DeclarationContent = declarationContent,
    sc4DeclarationContentBulletType = declarationContentBulletType,
    sc4Date = date,
    sc4DateDateFormat = dateDateFormat,
    sc4Place = place,
    sc4SignatureImagePath = signatureImagePath,
    sc4IsSignatureImageEnable = if (isSignatureImageEnable) 1 else 0
)
// update 92

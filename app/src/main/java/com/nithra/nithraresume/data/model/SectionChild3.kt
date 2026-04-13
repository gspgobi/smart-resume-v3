package com.nithra.nithraresume.data.model

import com.nithra.nithraresume.data.db.entity.SectionChild3Entity

/** Education */
data class SectionChild3(
    val id: Int = 0,
    val sectionHeadAddedId: Int,
    val indexPosition: Int,
    val studyDegree: String,
    val schoolName: String,
    val subtitle: String,
    val studyPeriod: String,
    val concentrates: String,
    val concentratesBulletType: String
)

fun SectionChild3Entity.toModel() = SectionChild3(
    id = sectionChild3Id,
    sectionHeadAddedId = sectionHeadAddedId,
    indexPosition = sc3IndexPosition,
    studyDegree = sc3StudyDegree.orEmpty(),
    schoolName = sc3SchoolName.orEmpty(),
    subtitle = sc3Subtitle.orEmpty(),
    studyPeriod = sc3StudyPeriod.orEmpty(),
    concentrates = sc3Concentrates.orEmpty(),
    concentratesBulletType = sc3ConcentratesBulletType.orEmpty()
)

fun SectionChild3.toEntity() = SectionChild3Entity(
    sectionChild3Id = id,
    sectionHeadAddedId = sectionHeadAddedId,
    sc3IndexPosition = indexPosition,
    sc3StudyDegree = studyDegree,
    sc3SchoolName = schoolName,
    sc3Subtitle = subtitle,
    sc3StudyPeriod = studyPeriod,
    sc3Concentrates = concentrates,
    sc3ConcentratesBulletType = concentratesBulletType
)

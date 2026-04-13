package com.nithra.nithraresume.data.model

import com.nithra.nithraresume.data.db.entity.SectionChild2Entity

/** Work Experience */
data class SectionChild2(
    val id: Int = 0,
    val sectionHeadAddedId: Int,
    val indexPosition: Int,
    val workRole: String,
    val companyName: String,
    val subtitle: String,
    val workPeriod: String,
    val accomplishments: String,
    val accomplishmentsBulletType: String
)

fun SectionChild2Entity.toModel() = SectionChild2(
    id = sectionChild2Id,
    sectionHeadAddedId = sectionHeadAddedId,
    indexPosition = sc2IndexPosition,
    workRole = sc2WorkRole.orEmpty(),
    companyName = sc2CompanyName.orEmpty(),
    subtitle = sc2Subtitle.orEmpty(),
    workPeriod = sc2WorkPeriod.orEmpty(),
    accomplishments = sc2Accomplishments.orEmpty(),
    accomplishmentsBulletType = sc2AccomplishmentsBulletType.orEmpty()
)

fun SectionChild2.toEntity() = SectionChild2Entity(
    sectionChild2Id = id,
    sectionHeadAddedId = sectionHeadAddedId,
    sc2IndexPosition = indexPosition,
    sc2WorkRole = workRole,
    sc2CompanyName = companyName,
    sc2Subtitle = subtitle,
    sc2WorkPeriod = workPeriod,
    sc2Accomplishments = accomplishments,
    sc2AccomplishmentsBulletType = accomplishmentsBulletType
)

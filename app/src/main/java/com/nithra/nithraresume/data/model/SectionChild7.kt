package com.nithra.nithraresume.data.model

import com.nithra.nithraresume.data.db.entity.SectionChild7Entity

/** Multiple Item Text */
data class SectionChild7(
    val id: Int = 0,
    val sectionHeadAddedId: Int,
    val indexPosition: Int,
    val contentTitle: String,
    val contentSubtitle: String,
    val contentDetail: String,
    val contentDetailBulletType: String
)

fun SectionChild7Entity.toModel() = SectionChild7(
    id = sectionChild7Id,
    sectionHeadAddedId = sectionHeadAddedId,
    indexPosition = sc7IndexPosition,
    contentTitle = sc7ContentTitle.orEmpty(),
    contentSubtitle = sc7ContentSubtitle.orEmpty(),
    contentDetail = sc7ContentDetail.orEmpty(),
    contentDetailBulletType = sc7ContentDetailBulletType.orEmpty()
)

fun SectionChild7.toEntity() = SectionChild7Entity(
    sectionChild7Id = id,
    sectionHeadAddedId = sectionHeadAddedId,
    sc7IndexPosition = indexPosition,
    sc7ContentTitle = contentTitle,
    sc7ContentSubtitle = contentSubtitle,
    sc7ContentDetail = contentDetail,
    sc7ContentDetailBulletType = contentDetailBulletType
)

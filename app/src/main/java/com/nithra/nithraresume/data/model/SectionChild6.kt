package com.nithra.nithraresume.data.model

import com.nithra.nithraresume.data.db.entity.SectionChild6Entity

/** Split Text — title + detail pairs */
data class SectionChild6(
    val id: Int = 0,
    val sectionHeadAddedId: Int,
    val indexPosition: Int,
    val contentTitle: String,
    val contentDetail: String
)

fun SectionChild6Entity.toModel() = SectionChild6(
    id = sectionChild6Id,
    sectionHeadAddedId = sectionHeadAddedId,
    indexPosition = sc6IndexPosition,
    contentTitle = sc6ContentTitle.orEmpty(),
    contentDetail = sc6ContentDetail.orEmpty()
)

fun SectionChild6.toEntity() = SectionChild6Entity(
    sectionChild6Id = id,
    sectionHeadAddedId = sectionHeadAddedId,
    sc6IndexPosition = indexPosition,
    sc6ContentTitle = contentTitle,
    sc6ContentDetail = contentDetail
)
// update 94

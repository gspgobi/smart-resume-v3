package com.nithra.nithraresume.data.model

import com.nithra.nithraresume.data.db.entity.SectionChild5Entity

/** Paragraph / Bulleted Text */
data class SectionChild5(
    val id: Int = 0,
    val sectionHeadAddedId: Int,
    val content: String,
    val contentBulletType: String
)

fun SectionChild5Entity.toModel() = SectionChild5(
    id = sectionChild5Id,
    sectionHeadAddedId = sectionHeadAddedId,
    content = sc5Content.orEmpty(),
    contentBulletType = sc5ContentBulletType.orEmpty()
)

fun SectionChild5.toEntity() = SectionChild5Entity(
    sectionChild5Id = id,
    sectionHeadAddedId = sectionHeadAddedId,
    sc5Content = content,
    sc5ContentBulletType = contentBulletType
)

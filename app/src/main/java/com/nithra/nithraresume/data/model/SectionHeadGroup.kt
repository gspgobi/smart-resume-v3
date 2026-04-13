package com.nithra.nithraresume.data.model

import com.nithra.nithraresume.data.db.entity.SectionHeadGroupBaseEntity

data class SectionHeadGroup(
    val id: Int = 0,
    val title: String,
    val isEditable: Boolean
)

fun SectionHeadGroupBaseEntity.toModel() = SectionHeadGroup(
    id = sectionHeadGroupBaseId,
    title = shgbTitle.orEmpty(),
    isEditable = shgbIsEditable
)

fun SectionHeadGroup.toEntity() = SectionHeadGroupBaseEntity(
    sectionHeadGroupBaseId = id,
    shgbTitle = title,
    shgbIsEditable = isEditable
)
// update 99

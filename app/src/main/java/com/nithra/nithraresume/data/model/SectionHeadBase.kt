package com.nithra.nithraresume.data.model

import com.nithra.nithraresume.data.db.entity.SectionHeadBaseEntity

data class SectionHeadBase(
    val id: Int = 0,
    val groupId: Int,
    val title: String,
    val hasChild: Boolean,
    val childTableName: String
)

fun SectionHeadBaseEntity.toModel() = SectionHeadBase(
    id = sectionHeadBaseId,
    groupId = sectionHeadGroupBaseId,
    title = shbTitle.orEmpty(),
    hasChild = shbHasChild,
    childTableName = scTableName.orEmpty()
)

fun SectionHeadBase.toEntity() = SectionHeadBaseEntity(
    sectionHeadBaseId = id,
    sectionHeadGroupBaseId = groupId,
    shbTitle = title,
    shbHasChild = hasChild,
    scTableName = childTableName
)

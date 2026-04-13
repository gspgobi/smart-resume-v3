package com.nithra.nithraresume.data.model

import com.nithra.nithraresume.data.db.entity.SectionHeadAddedEntity

data class SectionHeadAdded(
    val id: Int = 0,
    val profileId: Int,
    val groupBaseId: Int,
    val headBaseId: Int,
    val sampleDataId: Int?,
    val title: String,
    val isEnable: Boolean,
    val indexPosition: Int
)

fun SectionHeadAddedEntity.toModel() = SectionHeadAdded(
    id = sectionHeadAddedId,
    profileId = profileId,
    groupBaseId = sectionHeadGroupBaseId,
    headBaseId = sectionHeadBaseId,
    sampleDataId = sectionHeadSampleDataId,
    title = shaTitle.orEmpty(),
    isEnable = shaIsEnable,
    indexPosition = shaIndexPosition
)

fun SectionHeadAdded.toEntity() = SectionHeadAddedEntity(
    sectionHeadAddedId = id,
    profileId = profileId,
    sectionHeadGroupBaseId = groupBaseId,
    sectionHeadBaseId = headBaseId,
    sectionHeadSampleDataId = sampleDataId,
    shaTitle = title,
    shaIsEnable = isEnable,
    shaIndexPosition = indexPosition
)
// update 97

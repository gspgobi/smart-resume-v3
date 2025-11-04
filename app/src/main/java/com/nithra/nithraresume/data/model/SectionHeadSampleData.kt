package com.nithra.nithraresume.data.model

import com.nithra.nithraresume.data.db.entity.SectionHeadSampleDataEntity

data class SectionHeadSampleData(
    val id: Int = 0,
    val title: String,
    val isEnable: Boolean,
    val isDefault: Boolean,
    val groupName: String,
    val sectionHeadBaseId: Int,
    val sectionHeadGroupBaseId: Int,
    val indexPosition: Int
)

fun SectionHeadSampleDataEntity.toModel() = SectionHeadSampleData(
    id = sectionHeadSampleDataId,
    title = shsdTitle.orEmpty(),
    isEnable = shsdIsEnable,
    isDefault = shsdIsDefault,
    groupName = shsdGroupName.orEmpty(),
    sectionHeadBaseId = sectionHeadBaseId,
    sectionHeadGroupBaseId = sectionHeadGroupBaseId,
    indexPosition = shsdIndexPosition
)

fun SectionHeadSampleData.toEntity() = SectionHeadSampleDataEntity(
    sectionHeadSampleDataId = id,
    shsdTitle = title,
    shsdIsEnable = isEnable,
    shsdIsDefault = isDefault,
    shsdGroupName = groupName,
    sectionHeadBaseId = sectionHeadBaseId,
    sectionHeadGroupBaseId = sectionHeadGroupBaseId,
    shsdIndexPosition = indexPosition
)
// update 100

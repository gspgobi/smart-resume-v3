package com.nithra.nithraresume.data.model

import com.nithra.nithraresume.data.db.entity.SectionChild8Entity

/** Cover Letter (Add-on) */
data class SectionChild8(
    val id: Int = 0,
    val sectionHeadAddedId: Int,
    val date: String,
    val dateDateFormat: String,
    val address: String,
    val content: String
)

fun SectionChild8Entity.toModel() = SectionChild8(
    id = sectionChild8Id,
    sectionHeadAddedId = sectionHeadAddedId,
    date = sc8Date.orEmpty(),
    dateDateFormat = sc8DateDateFormat.orEmpty(),
    address = sc8Address.orEmpty(),
    content = sc8Content.orEmpty()
)

fun SectionChild8.toEntity() = SectionChild8Entity(
    sectionChild8Id = id,
    sectionHeadAddedId = sectionHeadAddedId,
    sc8Date = date,
    sc8DateDateFormat = dateDateFormat,
    sc8Address = address,
    sc8Content = content
)
// update 96

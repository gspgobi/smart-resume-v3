package com.nithra.nithraresume.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "section_head_group_base")
data class SectionHeadGroupBaseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "section_head_group_base_id")
    val sectionHeadGroupBaseId: Int = 0,

    @ColumnInfo(name = "shgb_title")
    val shgbTitle: String?,

    @ColumnInfo(name = "shgb_is_editable")
    val shgbIsEditable: Boolean
)
// update 84

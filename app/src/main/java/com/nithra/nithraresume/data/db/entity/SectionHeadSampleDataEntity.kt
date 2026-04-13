package com.nithra.nithraresume.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "section_head_sample_data")
data class SectionHeadSampleDataEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "section_head_sample_data_id")
    val sectionHeadSampleDataId: Int = 0,

    @ColumnInfo(name = "shsd_title")
    val shsdTitle: String?,

    @ColumnInfo(name = "shsd_is_enable")
    val shsdIsEnable: Boolean,

    @ColumnInfo(name = "shsd_is_default")
    val shsdIsDefault: Boolean,

    @ColumnInfo(name = "shsd_group_name")
    val shsdGroupName: String?,

    @ColumnInfo(name = "section_head_base_id")
    val sectionHeadBaseId: Int,

    @ColumnInfo(name = "section_head_group_base_id")
    val sectionHeadGroupBaseId: Int,

    @ColumnInfo(name = "shsd_index_position")
    val shsdIndexPosition: Int
)
// update 85

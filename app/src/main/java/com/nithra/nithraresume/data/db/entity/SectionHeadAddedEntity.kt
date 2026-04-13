package com.nithra.nithraresume.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "section_head_added")
data class SectionHeadAddedEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "section_head_added_id")
    val sectionHeadAddedId: Int = 0,

    @ColumnInfo(name = "profile_id")
    val profileId: Int,

    @ColumnInfo(name = "section_head_group_base_id")
    val sectionHeadGroupBaseId: Int,

    @ColumnInfo(name = "section_head_base_id")
    val sectionHeadBaseId: Int,

    @ColumnInfo(name = "section_head_sample_data_id")
    val sectionHeadSampleDataId: Int?,

    @ColumnInfo(name = "sha_title")
    val shaTitle: String?,

    @ColumnInfo(name = "sha_is_enable")
    val shaIsEnable: Boolean,

    @ColumnInfo(name = "sha_index_position")
    val shaIndexPosition: Int
)

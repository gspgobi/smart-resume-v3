package com.nithra.nithraresume.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "section_head_base")
data class SectionHeadBaseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "section_head_base_id")
    val sectionHeadBaseId: Int = 0,

    @ColumnInfo(name = "section_head_group_base_id")
    val sectionHeadGroupBaseId: Int,

    @ColumnInfo(name = "shb_title")
    val shbTitle: String?,

    @ColumnInfo(name = "shb_has_child")
    val shbHasChild: Boolean,

    @ColumnInfo(name = "sc_table_name")
    val scTableName: String?
)
// update 83

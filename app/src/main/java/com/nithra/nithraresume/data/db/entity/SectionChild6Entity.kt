package com.nithra.nithraresume.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Split Text — title + detail pairs */
@Entity(tableName = "section_child_6")
data class SectionChild6Entity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "section_child_6_id")
    val sectionChild6Id: Int = 0,

    @ColumnInfo(name = "section_head_added_id")
    val sectionHeadAddedId: Int,

    @ColumnInfo(name = "sc6_index_position")
    val sc6IndexPosition: Int,

    @ColumnInfo(name = "sc6_content_title")
    val sc6ContentTitle: String?,

    @ColumnInfo(name = "sc6_content_detail")
    val sc6ContentDetail: String?
)

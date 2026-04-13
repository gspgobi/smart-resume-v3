package com.nithra.nithraresume.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Multiple Item Text */
@Entity(tableName = "section_child_7")
data class SectionChild7Entity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "section_child_7_id")
    val sectionChild7Id: Int = 0,

    @ColumnInfo(name = "section_head_added_id")
    val sectionHeadAddedId: Int,

    @ColumnInfo(name = "sc7_index_position")
    val sc7IndexPosition: Int,

    @ColumnInfo(name = "sc7_content_title")
    val sc7ContentTitle: String?,

    @ColumnInfo(name = "sc7_content_subtitle")
    val sc7ContentSubtitle: String?,

    @ColumnInfo(name = "sc7_content_detail")
    val sc7ContentDetail: String?,

    @ColumnInfo(name = "sc7_content_detail_bullet_type")
    val sc7ContentDetailBulletType: String?
)

package com.nithra.nithraresume.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Paragraph / Bulleted Text */
@Entity(tableName = "section_child_5")
data class SectionChild5Entity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "section_child_5_id")
    val sectionChild5Id: Int = 0,

    @ColumnInfo(name = "section_head_added_id")
    val sectionHeadAddedId: Int,

    @ColumnInfo(name = "sc5_content")
    val sc5Content: String?,

    @ColumnInfo(name = "sc5_content_bullet_type")
    val sc5ContentBulletType: String?
)

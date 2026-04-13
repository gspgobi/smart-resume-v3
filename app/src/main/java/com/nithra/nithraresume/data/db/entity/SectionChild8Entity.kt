package com.nithra.nithraresume.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Cover Letter (Add-on) */
@Entity(tableName = "section_child_8")
data class SectionChild8Entity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "section_child_8_id")
    val sectionChild8Id: Int = 0,

    @ColumnInfo(name = "section_head_added_id")
    val sectionHeadAddedId: Int,

    @ColumnInfo(name = "sc8_date")
    val sc8Date: String?,

    @ColumnInfo(name = "sc8_date_date_format")
    val sc8DateDateFormat: String?,

    @ColumnInfo(name = "sc8_address")
    val sc8Address: String?,

    @ColumnInfo(name = "sc8_content")
    val sc8Content: String?
)

package com.nithra.nithraresume.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resume_format_base")
data class ResumeFormatBaseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "resume_format_base_id")
    val resumeFormatBaseId: Int = 0,

    @ColumnInfo(name = "resume_format_base_title")
    val resumeFormatBaseTitle: String?,

    @ColumnInfo(name = "resume_format_base_description")
    val resumeFormatBaseDescription: String?,

    @ColumnInfo(name = "resume_format_base_is_default")
    val resumeFormatBaseIsDefault: Boolean,

    @ColumnInfo(name = "up_font_style")
    val upFontStyle: String,

    @ColumnInfo(name = "up_font_size")
    val upFontSize: Int,

    // Typo "backgroud" preserved — must match V2 column name exactly
    @ColumnInfo(name = "up_backgroud_color")
    val upBackgroundColor: String?
)
// update 73

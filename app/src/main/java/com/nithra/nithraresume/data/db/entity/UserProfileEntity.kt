package com.nithra.nithraresume.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_profile_id")
    val userProfileId: Int = 0,

    @ColumnInfo(name = "up_name")
    val upName: String,

    @ColumnInfo(name = "up_index_position")
    val upIndexPosition: Int,

    @ColumnInfo(name = "up_is_sample_profile")
    val upIsSampleProfile: Boolean,

    @ColumnInfo(name = "sample_profile_id")
    val sampleProfileId: Int?,

    @ColumnInfo(name = "resume_format_base_id")
    val resumeFormatBaseId: Int,

    @ColumnInfo(name = "up_font_style")
    val upFontStyle: String,

    @ColumnInfo(name = "up_font_size")
    val upFontSize: Int,

    // Typo "backgroud" preserved — must match V2 column name exactly
    @ColumnInfo(name = "up_backgroud_color")
    val upBackgroundColor: String?,

    @ColumnInfo(name = "up_resume_file_name")
    val upResumeFileName: String?
)

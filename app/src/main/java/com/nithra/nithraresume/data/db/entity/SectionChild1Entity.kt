package com.nithra.nithraresume.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Contact Information */
@Entity(tableName = "section_child_1")
data class SectionChild1Entity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "section_child_1_id")
    val sectionChild1Id: Int = 0,

    @ColumnInfo(name = "section_head_added_id")
    val sectionHeadAddedId: Int,

    @ColumnInfo(name = "sc1_name")
    val sc1Name: String?,

    @ColumnInfo(name = "sc1_address")
    val sc1Address: String?,

    @ColumnInfo(name = "sc1_email")
    val sc1Email: String?,

    @ColumnInfo(name = "sc1_phone")
    val sc1Phone: String?,

    @ColumnInfo(name = "sc1_gender")
    val sc1Gender: String?,

    @ColumnInfo(name = "sc1_dob")
    val sc1Dob: String?,

    @ColumnInfo(name = "sc1_dob_date_format")
    val sc1DobDateFormat: String?,

    @ColumnInfo(name = "sc1_nationality")
    val sc1Nationality: String?,

    @ColumnInfo(name = "sc1_user_image_path")
    val sc1UserImagePath: String?,

    @ColumnInfo(name = "sc1_is_user_image_enable")
    val sc1IsUserImageEnable: Int?
)

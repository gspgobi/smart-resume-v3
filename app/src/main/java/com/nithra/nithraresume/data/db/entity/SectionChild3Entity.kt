package com.nithra.nithraresume.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Education */
@Entity(tableName = "section_child_3")
data class SectionChild3Entity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "section_child_3_id")
    val sectionChild3Id: Int = 0,

    @ColumnInfo(name = "section_head_added_id")
    val sectionHeadAddedId: Int,

    @ColumnInfo(name = "sc3_index_position")
    val sc3IndexPosition: Int,

    @ColumnInfo(name = "sc3_study_degree")
    val sc3StudyDegree: String?,

    @ColumnInfo(name = "sc3_school_name")
    val sc3SchoolName: String?,

    @ColumnInfo(name = "sc3_subtitle")
    val sc3Subtitle: String?,

    @ColumnInfo(name = "sc3_study_period")
    val sc3StudyPeriod: String?,

    @ColumnInfo(name = "sc3_concentrates")
    val sc3Concentrates: String?,

    @ColumnInfo(name = "sc3_concentrates_bullet_type")
    val sc3ConcentratesBulletType: String?
)
// update 76

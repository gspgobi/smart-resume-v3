package com.nithra.nithraresume.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Work Experience */
@Entity(tableName = "section_child_2")
data class SectionChild2Entity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "section_child_2_id")
    val sectionChild2Id: Int = 0,

    @ColumnInfo(name = "section_head_added_id")
    val sectionHeadAddedId: Int,

    @ColumnInfo(name = "sc2_index_position")
    val sc2IndexPosition: Int,

    @ColumnInfo(name = "sc2_work_role")
    val sc2WorkRole: String?,

    @ColumnInfo(name = "sc2_company_name")
    val sc2CompanyName: String?,

    @ColumnInfo(name = "sc2_subtitle")
    val sc2Subtitle: String?,

    @ColumnInfo(name = "sc2_work_period")
    val sc2WorkPeriod: String?,

    @ColumnInfo(name = "sc2_accomplishments")
    val sc2Accomplishments: String?,

    @ColumnInfo(name = "sc2_accomplishments_bullet_type")
    val sc2AccomplishmentsBulletType: String?
)
// update 75

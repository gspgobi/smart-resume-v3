package com.nithra.nithraresume.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Declaration + Signature */
@Entity(tableName = "section_child_4")
data class SectionChild4Entity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "section_child_4_id")
    val sectionChild4Id: Int = 0,

    @ColumnInfo(name = "section_head_added_id")
    val sectionHeadAddedId: Int,

    @ColumnInfo(name = "sc4_declaration_content")
    val sc4DeclarationContent: String?,

    @ColumnInfo(name = "sc4_declaration_content_bullet_type")
    val sc4DeclarationContentBulletType: String?,

    @ColumnInfo(name = "sc4_date")
    val sc4Date: String?,

    @ColumnInfo(name = "sc4_date_date_format")
    val sc4DateDateFormat: String?,

    @ColumnInfo(name = "sc4_place")
    val sc4Place: String?,

    @ColumnInfo(name = "sc4_signature_image_path")
    val sc4SignatureImagePath: String?,

    @ColumnInfo(name = "sc4_is_signature_image_enable")
    val sc4IsSignatureImageEnable: Int?
)

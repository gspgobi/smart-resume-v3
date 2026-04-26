package com.nithra.nithraresume.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nithra.nithraresume.data.db.dao.FcmDataDao
import com.nithra.nithraresume.data.db.dao.ResumeFormatBaseDao
import com.nithra.nithraresume.data.db.dao.SectionChildListDao
import com.nithra.nithraresume.data.db.dao.SectionChildSingleDao
import com.nithra.nithraresume.data.db.dao.SectionHeadAddedDao
import com.nithra.nithraresume.data.db.dao.SectionHeadBaseDao
import com.nithra.nithraresume.data.db.dao.SectionHeadGroupBaseDao
import com.nithra.nithraresume.data.db.dao.SectionHeadSampleDataDao
import com.nithra.nithraresume.data.db.dao.UserProfileDao
import com.nithra.nithraresume.data.db.entity.FcmDataEntity
import com.nithra.nithraresume.data.db.entity.ResumeFormatBaseEntity
import com.nithra.nithraresume.data.db.entity.SectionChild1Entity
import com.nithra.nithraresume.data.db.entity.SectionChild2Entity
import com.nithra.nithraresume.data.db.entity.SectionChild3Entity
import com.nithra.nithraresume.data.db.entity.SectionChild4Entity
import com.nithra.nithraresume.data.db.entity.SectionChild5Entity
import com.nithra.nithraresume.data.db.entity.SectionChild6Entity
import com.nithra.nithraresume.data.db.entity.SectionChild7Entity
import com.nithra.nithraresume.data.db.entity.SectionChild8Entity
import com.nithra.nithraresume.data.db.entity.SectionHeadAddedEntity
import com.nithra.nithraresume.data.db.entity.SectionHeadBaseEntity
import com.nithra.nithraresume.data.db.entity.SectionHeadGroupBaseEntity
import com.nithra.nithraresume.data.db.entity.SectionHeadSampleDataEntity
import com.nithra.nithraresume.data.db.entity.UserProfileEntity

@Database(
    entities = [
        ResumeFormatBaseEntity::class,
        SectionHeadGroupBaseEntity::class,
        SectionHeadBaseEntity::class,
        SectionHeadSampleDataEntity::class,
        UserProfileEntity::class,
        SectionHeadAddedEntity::class,
        SectionChild1Entity::class,
        SectionChild2Entity::class,
        SectionChild3Entity::class,
        SectionChild4Entity::class,
        SectionChild5Entity::class,
        SectionChild6Entity::class,
        SectionChild7Entity::class,
        SectionChild8Entity::class,
        FcmDataEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class SmartResumeDatabase : RoomDatabase() {

    abstract fun resumeFormatBaseDao(): ResumeFormatBaseDao
    abstract fun sectionHeadGroupBaseDao(): SectionHeadGroupBaseDao
    abstract fun sectionHeadBaseDao(): SectionHeadBaseDao
    abstract fun sectionHeadSampleDataDao(): SectionHeadSampleDataDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun sectionHeadAddedDao(): SectionHeadAddedDao
    abstract fun sectionChildSingleDao(): SectionChildSingleDao
    abstract fun sectionChildListDao(): SectionChildListDao
    abstract fun fcmDataDao(): FcmDataDao

    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "smart_resume_v2.db"

        /**
         * Inserts the same seed rows that V2 inserted in SQLiteOpenHelper.onCreate.
         * Column order matches the V2 CREATE TABLE column order exactly so that
         * positional VALUES (...) inserts land in the right columns.
         */
        val seedCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                // ── resume_format_base ────────────────────────────────────────
                // (id, title, description, is_default, font_style, font_size, bg_color)
                db.execSQL("INSERT INTO resume_format_base VALUES (1,'Functional','Functional Description',1,'TIMES NEW ROMAN.TTF',12,'White')")
                db.execSQL("INSERT INTO resume_format_base VALUES (2,'Harvard','Harvard Description',0,'TIMES NEW ROMAN.TTF',12,'White')")
                db.execSQL("INSERT INTO resume_format_base VALUES (3,'Classic','Classic Description',0,'TIMES NEW ROMAN.TTF',12,'White')")
                db.execSQL("INSERT INTO resume_format_base VALUES (4,'Modern','Modern Description',0,'TIMES NEW ROMAN.TTF',12,'White')")
                db.execSQL("INSERT INTO resume_format_base VALUES (5,'Simple','Simple Description',0,'TIMES NEW ROMAN.TTF',12,'White')")
                db.execSQL("INSERT INTO resume_format_base VALUES (6,'Grayscale','Grayscale Description',0,'TIMES NEW ROMAN.TTF',12,'White')")

                // ── section_head_group_base ───────────────────────────────────
                // (id, title, is_editable)
                db.execSQL("INSERT INTO section_head_group_base VALUES (1,'Sections',1)")
                db.execSQL("INSERT INTO section_head_group_base VALUES (2,'Add-ons',1)")

                // ── section_head_base ─────────────────────────────────────────
                // (id, group_base_id, title, has_child, sc_table_name)
                db.execSQL("INSERT INTO section_head_base VALUES (1,1,'CONTACT INFORMATION',0,'section_child_1')")
                db.execSQL("INSERT INTO section_head_base VALUES (2,1,'WORK EXPERIENCE',1,'section_child_2')")
                db.execSQL("INSERT INTO section_head_base VALUES (3,1,'EDUCATION',1,'section_child_3')")
                db.execSQL("INSERT INTO section_head_base VALUES (4,1,'DECLARATION',0,'section_child_4')")
                db.execSQL("INSERT INTO section_head_base VALUES (5,1,'PARAGRAPH / BULLETED TEXT',0,'section_child_5')")
                db.execSQL("INSERT INTO section_head_base VALUES (6,1,'SPLIT TEXT',1,'section_child_6')")
                db.execSQL("INSERT INTO section_head_base VALUES (7,1,'MULTIPLE ITEM TEXT',1,'section_child_7')")
                db.execSQL("INSERT INTO section_head_base VALUES (8,2,'COVER LETTER',0,'section_child_8')")

                // ── section_head_sample_data ──────────────────────────────────
                // (id, title, is_enable, is_default, group_name, base_id, group_base_id, index_position)
                db.execSQL("INSERT INTO section_head_sample_data VALUES (1,'CONTACT INFORMATION',1,1,'Standard',1,1,0)")
                db.execSQL("INSERT INTO section_head_sample_data VALUES (2,'OBJECTIVE',1,1,'Standard',5,1,1)")
                db.execSQL("INSERT INTO section_head_sample_data VALUES (3,'KEY QUALIFICATIONS',1,1,'Standard',5,1,2)")
                db.execSQL("INSERT INTO section_head_sample_data VALUES (4,'WORK EXPERIENCE',1,1,'Standard',2,1,3)")
                db.execSQL("INSERT INTO section_head_sample_data VALUES (5,'EDUCATION',1,1,'Standard',3,1,4)")
                db.execSQL("INSERT INTO section_head_sample_data VALUES (6,'REFERENCE',1,1,'Standard',5,1,5)")
                db.execSQL("INSERT INTO section_head_sample_data VALUES (7,'PROJECTS',0,0,'Standard',7,1,6)")
                db.execSQL("INSERT INTO section_head_sample_data VALUES (8,'ACHIEVEMENTS',0,0,'Standard',5,1,7)")
                db.execSQL("INSERT INTO section_head_sample_data VALUES (9,'AREA OF INTEREST',0,0,'Standard',5,1,8)")
                db.execSQL("INSERT INTO section_head_sample_data VALUES (10,'COMPUTER SOFT SKILLS',0,0,'Standard',6,1,9)")
                db.execSQL("INSERT INTO section_head_sample_data VALUES (11,'CO-CURRICULAR ACTIVITIES',0,0,'Standard',5,1,10)")
                db.execSQL("INSERT INTO section_head_sample_data VALUES (12,'EXTRA-CURRICULAR ACTIVITIES',0,0,'Standard',5,1,11)")
                db.execSQL("INSERT INTO section_head_sample_data VALUES (13,'ADDITIONAL INFO',0,0,'Standard',6,1,12)")
                db.execSQL("INSERT INTO section_head_sample_data VALUES (14,'DECLARATION',0,0,'Standard',4,1,13)")
                db.execSQL("INSERT INTO section_head_sample_data VALUES (15,'PROFESSIONAL SUMMARY',0,0,'Standard',5,1,14)")
                db.execSQL("INSERT INTO section_head_sample_data VALUES (16,'PARAGRAPH / BULLETED TEXT',0,0,'Custom',5,1,15)")
                db.execSQL("INSERT INTO section_head_sample_data VALUES (17,'SPLIT TEXT',0,0,'Custom',6,1,16)")
                db.execSQL("INSERT INTO section_head_sample_data VALUES (18,'MULTIPLE ITEM TEXT',0,0,'Custom',7,1,17)")
                db.execSQL("INSERT INTO section_head_sample_data VALUES (19,'COVER LETTER',1,1,'Add-ons',8,2,0)")
            }
        }
    }
}

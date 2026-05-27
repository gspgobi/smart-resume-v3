package com.nithra.nithraresume.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Fixes two schema mismatches introduced when the app moved from SQLiteOpenHelper (v2) to
 * Room (v3), both targeting the same database file (smart_resume_v2.db, user_version 1):
 *
 *  1. Primary-key columns: SQLiteOpenHelper didn't emit explicit NOT NULL on INTEGER PRIMARY KEY;
 *     Room requires it.  PRAGMA table_info() returns notnull=0 for every PK in the v2 DB, so
 *     Room's schema validator rejects every table.
 *
 *  2. Boolean columns: SQLiteOpenHelper used the "BOOLEAN" type name (affinity=NUMERIC=1); Room
 *     maps Kotlin Boolean to "INTEGER" (affinity=INTEGER=3).
 *
 * Every table is recreated using the rename-create-copy-drop pattern.  fcm_data may or may not
 * have existed in the v2 build, so its existence is checked at runtime.
 */
@Volatile var migrationRan1to2: Boolean = false

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        migrationRan1to2 = true

        // ── resume_format_base ────────────────────────────────────────────────
        db.execSQL("ALTER TABLE `resume_format_base` RENAME TO `resume_format_base_old`")
        db.execSQL("""
            CREATE TABLE `resume_format_base` (
                `resume_format_base_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `resume_format_base_title` TEXT,
                `resume_format_base_description` TEXT,
                `resume_format_base_is_default` INTEGER NOT NULL,
                `up_font_style` TEXT NOT NULL,
                `up_font_size` INTEGER NOT NULL,
                `up_backgroud_color` TEXT
            )
        """)
        db.execSQL("""
            INSERT INTO `resume_format_base`
                (`resume_format_base_id`,`resume_format_base_title`,`resume_format_base_description`,
                 `resume_format_base_is_default`,`up_font_style`,`up_font_size`,`up_backgroud_color`)
            SELECT
                `resume_format_base_id`,`resume_format_base_title`,`resume_format_base_description`,
                `resume_format_base_is_default`,`up_font_style`,`up_font_size`,`up_backgroud_color`
            FROM `resume_format_base_old`
        """)
        db.execSQL("DROP TABLE `resume_format_base_old`")

        // ── section_head_group_base ───────────────────────────────────────────
        db.execSQL("ALTER TABLE `section_head_group_base` RENAME TO `section_head_group_base_old`")
        db.execSQL("""
            CREATE TABLE `section_head_group_base` (
                `section_head_group_base_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `shgb_title` TEXT,
                `shgb_is_editable` INTEGER NOT NULL
            )
        """)
        db.execSQL("""
            INSERT INTO `section_head_group_base`
                (`section_head_group_base_id`,`shgb_title`,`shgb_is_editable`)
            SELECT `section_head_group_base_id`,`shgb_title`,`shgb_is_editable`
            FROM `section_head_group_base_old`
        """)
        db.execSQL("DROP TABLE `section_head_group_base_old`")

        // ── section_head_base ─────────────────────────────────────────────────
        db.execSQL("ALTER TABLE `section_head_base` RENAME TO `section_head_base_old`")
        db.execSQL("""
            CREATE TABLE `section_head_base` (
                `section_head_base_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `section_head_group_base_id` INTEGER NOT NULL,
                `shb_title` TEXT,
                `shb_has_child` INTEGER NOT NULL,
                `sc_table_name` TEXT
            )
        """)
        db.execSQL("""
            INSERT INTO `section_head_base`
                (`section_head_base_id`,`section_head_group_base_id`,`shb_title`,
                 `shb_has_child`,`sc_table_name`)
            SELECT `section_head_base_id`,`section_head_group_base_id`,`shb_title`,
                   `shb_has_child`,`sc_table_name`
            FROM `section_head_base_old`
        """)
        db.execSQL("DROP TABLE `section_head_base_old`")

        // ── section_head_sample_data ──────────────────────────────────────────
        db.execSQL("ALTER TABLE `section_head_sample_data` RENAME TO `section_head_sample_data_old`")
        db.execSQL("""
            CREATE TABLE `section_head_sample_data` (
                `section_head_sample_data_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `shsd_title` TEXT,
                `shsd_is_enable` INTEGER NOT NULL,
                `shsd_is_default` INTEGER NOT NULL,
                `shsd_group_name` TEXT,
                `section_head_base_id` INTEGER NOT NULL,
                `section_head_group_base_id` INTEGER NOT NULL,
                `shsd_index_position` INTEGER NOT NULL
            )
        """)
        db.execSQL("""
            INSERT INTO `section_head_sample_data`
                (`section_head_sample_data_id`,`shsd_title`,`shsd_is_enable`,`shsd_is_default`,
                 `shsd_group_name`,`section_head_base_id`,`section_head_group_base_id`,
                 `shsd_index_position`)
            SELECT `section_head_sample_data_id`,`shsd_title`,`shsd_is_enable`,`shsd_is_default`,
                   `shsd_group_name`,`section_head_base_id`,`section_head_group_base_id`,
                   `shsd_index_position`
            FROM `section_head_sample_data_old`
        """)
        db.execSQL("DROP TABLE `section_head_sample_data_old`")

        // ── user_profile ──────────────────────────────────────────────────────
        db.execSQL("ALTER TABLE `user_profile` RENAME TO `user_profile_old`")
        db.execSQL("""
            CREATE TABLE `user_profile` (
                `user_profile_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `up_name` TEXT NOT NULL,
                `up_index_position` INTEGER NOT NULL,
                `up_is_sample_profile` INTEGER NOT NULL,
                `sample_profile_id` INTEGER,
                `resume_format_base_id` INTEGER NOT NULL,
                `up_font_style` TEXT NOT NULL,
                `up_font_size` INTEGER NOT NULL,
                `up_backgroud_color` TEXT,
                `up_resume_file_name` TEXT
            )
        """)
        db.execSQL("""
            INSERT INTO `user_profile`
                (`user_profile_id`,`up_name`,`up_index_position`,`up_is_sample_profile`,
                 `sample_profile_id`,`resume_format_base_id`,`up_font_style`,`up_font_size`,
                 `up_backgroud_color`,`up_resume_file_name`)
            SELECT `user_profile_id`,`up_name`,`up_index_position`,`up_is_sample_profile`,
                   `sample_profile_id`,`resume_format_base_id`,`up_font_style`,`up_font_size`,
                   `up_backgroud_color`,`up_resume_file_name`
            FROM `user_profile_old`
        """)
        db.execSQL("DROP TABLE `user_profile_old`")

        // ── section_head_added ────────────────────────────────────────────────
        db.execSQL("ALTER TABLE `section_head_added` RENAME TO `section_head_added_old`")
        db.execSQL("""
            CREATE TABLE `section_head_added` (
                `section_head_added_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `profile_id` INTEGER NOT NULL,
                `section_head_group_base_id` INTEGER NOT NULL,
                `section_head_base_id` INTEGER NOT NULL,
                `section_head_sample_data_id` INTEGER,
                `sha_title` TEXT,
                `sha_is_enable` INTEGER NOT NULL,
                `sha_index_position` INTEGER NOT NULL
            )
        """)
        db.execSQL("""
            INSERT INTO `section_head_added`
                (`section_head_added_id`,`profile_id`,`section_head_group_base_id`,
                 `section_head_base_id`,`section_head_sample_data_id`,`sha_title`,
                 `sha_is_enable`,`sha_index_position`)
            SELECT `section_head_added_id`,`profile_id`,`section_head_group_base_id`,
                   `section_head_base_id`,`section_head_sample_data_id`,`sha_title`,
                   `sha_is_enable`,`sha_index_position`
            FROM `section_head_added_old`
        """)
        db.execSQL("DROP TABLE `section_head_added_old`")

        // ── section_child_1 ───────────────────────────────────────────────────
        db.execSQL("ALTER TABLE `section_child_1` RENAME TO `section_child_1_old`")
        db.execSQL("""
            CREATE TABLE `section_child_1` (
                `section_child_1_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `section_head_added_id` INTEGER NOT NULL,
                `sc1_name` TEXT, `sc1_address` TEXT, `sc1_email` TEXT, `sc1_phone` TEXT,
                `sc1_gender` TEXT, `sc1_dob` TEXT, `sc1_dob_date_format` TEXT,
                `sc1_nationality` TEXT, `sc1_user_image_path` TEXT,
                `sc1_is_user_image_enable` INTEGER
            )
        """)
        db.execSQL("""
            INSERT INTO `section_child_1`
                (`section_child_1_id`,`section_head_added_id`,`sc1_name`,`sc1_address`,
                 `sc1_email`,`sc1_phone`,`sc1_gender`,`sc1_dob`,`sc1_dob_date_format`,
                 `sc1_nationality`,`sc1_user_image_path`,`sc1_is_user_image_enable`)
            SELECT `section_child_1_id`,`section_head_added_id`,`sc1_name`,`sc1_address`,
                   `sc1_email`,`sc1_phone`,`sc1_gender`,`sc1_dob`,`sc1_dob_date_format`,
                   `sc1_nationality`,`sc1_user_image_path`,`sc1_is_user_image_enable`
            FROM `section_child_1_old`
        """)
        db.execSQL("DROP TABLE `section_child_1_old`")

        // ── section_child_2 ───────────────────────────────────────────────────
        db.execSQL("ALTER TABLE `section_child_2` RENAME TO `section_child_2_old`")
        db.execSQL("""
            CREATE TABLE `section_child_2` (
                `section_child_2_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `section_head_added_id` INTEGER NOT NULL,
                `sc2_index_position` INTEGER NOT NULL,
                `sc2_work_role` TEXT, `sc2_company_name` TEXT, `sc2_subtitle` TEXT,
                `sc2_work_period` TEXT, `sc2_accomplishments` TEXT,
                `sc2_accomplishments_bullet_type` TEXT
            )
        """)
        db.execSQL("""
            INSERT INTO `section_child_2`
                (`section_child_2_id`,`section_head_added_id`,`sc2_index_position`,
                 `sc2_work_role`,`sc2_company_name`,`sc2_subtitle`,`sc2_work_period`,
                 `sc2_accomplishments`,`sc2_accomplishments_bullet_type`)
            SELECT `section_child_2_id`,`section_head_added_id`,`sc2_index_position`,
                   `sc2_work_role`,`sc2_company_name`,`sc2_subtitle`,`sc2_work_period`,
                   `sc2_accomplishments`,`sc2_accomplishments_bullet_type`
            FROM `section_child_2_old`
        """)
        db.execSQL("DROP TABLE `section_child_2_old`")

        // ── section_child_3 ───────────────────────────────────────────────────
        db.execSQL("ALTER TABLE `section_child_3` RENAME TO `section_child_3_old`")
        db.execSQL("""
            CREATE TABLE `section_child_3` (
                `section_child_3_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `section_head_added_id` INTEGER NOT NULL,
                `sc3_index_position` INTEGER NOT NULL,
                `sc3_study_degree` TEXT, `sc3_school_name` TEXT, `sc3_subtitle` TEXT,
                `sc3_study_period` TEXT, `sc3_concentrates` TEXT,
                `sc3_concentrates_bullet_type` TEXT
            )
        """)
        db.execSQL("""
            INSERT INTO `section_child_3`
                (`section_child_3_id`,`section_head_added_id`,`sc3_index_position`,
                 `sc3_study_degree`,`sc3_school_name`,`sc3_subtitle`,`sc3_study_period`,
                 `sc3_concentrates`,`sc3_concentrates_bullet_type`)
            SELECT `section_child_3_id`,`section_head_added_id`,`sc3_index_position`,
                   `sc3_study_degree`,`sc3_school_name`,`sc3_subtitle`,`sc3_study_period`,
                   `sc3_concentrates`,`sc3_concentrates_bullet_type`
            FROM `section_child_3_old`
        """)
        db.execSQL("DROP TABLE `section_child_3_old`")

        // ── section_child_4 ───────────────────────────────────────────────────
        db.execSQL("ALTER TABLE `section_child_4` RENAME TO `section_child_4_old`")
        db.execSQL("""
            CREATE TABLE `section_child_4` (
                `section_child_4_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `section_head_added_id` INTEGER NOT NULL,
                `sc4_declaration_content` TEXT, `sc4_declaration_content_bullet_type` TEXT,
                `sc4_date` TEXT, `sc4_date_date_format` TEXT, `sc4_place` TEXT,
                `sc4_signature_image_path` TEXT, `sc4_is_signature_image_enable` INTEGER
            )
        """)
        db.execSQL("""
            INSERT INTO `section_child_4`
                (`section_child_4_id`,`section_head_added_id`,`sc4_declaration_content`,
                 `sc4_declaration_content_bullet_type`,`sc4_date`,`sc4_date_date_format`,
                 `sc4_place`,`sc4_signature_image_path`,`sc4_is_signature_image_enable`)
            SELECT `section_child_4_id`,`section_head_added_id`,`sc4_declaration_content`,
                   `sc4_declaration_content_bullet_type`,`sc4_date`,`sc4_date_date_format`,
                   `sc4_place`,`sc4_signature_image_path`,`sc4_is_signature_image_enable`
            FROM `section_child_4_old`
        """)
        db.execSQL("DROP TABLE `section_child_4_old`")

        // ── section_child_5 ───────────────────────────────────────────────────
        db.execSQL("ALTER TABLE `section_child_5` RENAME TO `section_child_5_old`")
        db.execSQL("""
            CREATE TABLE `section_child_5` (
                `section_child_5_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `section_head_added_id` INTEGER NOT NULL,
                `sc5_content` TEXT, `sc5_content_bullet_type` TEXT
            )
        """)
        db.execSQL("""
            INSERT INTO `section_child_5`
                (`section_child_5_id`,`section_head_added_id`,`sc5_content`,
                 `sc5_content_bullet_type`)
            SELECT `section_child_5_id`,`section_head_added_id`,`sc5_content`,
                   `sc5_content_bullet_type`
            FROM `section_child_5_old`
        """)
        db.execSQL("DROP TABLE `section_child_5_old`")

        // ── section_child_6 ───────────────────────────────────────────────────
        db.execSQL("ALTER TABLE `section_child_6` RENAME TO `section_child_6_old`")
        db.execSQL("""
            CREATE TABLE `section_child_6` (
                `section_child_6_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `section_head_added_id` INTEGER NOT NULL,
                `sc6_index_position` INTEGER NOT NULL,
                `sc6_content_title` TEXT, `sc6_content_detail` TEXT
            )
        """)
        db.execSQL("""
            INSERT INTO `section_child_6`
                (`section_child_6_id`,`section_head_added_id`,`sc6_index_position`,
                 `sc6_content_title`,`sc6_content_detail`)
            SELECT `section_child_6_id`,`section_head_added_id`,`sc6_index_position`,
                   `sc6_content_title`,`sc6_content_detail`
            FROM `section_child_6_old`
        """)
        db.execSQL("DROP TABLE `section_child_6_old`")

        // ── section_child_7 ───────────────────────────────────────────────────
        db.execSQL("ALTER TABLE `section_child_7` RENAME TO `section_child_7_old`")
        db.execSQL("""
            CREATE TABLE `section_child_7` (
                `section_child_7_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `section_head_added_id` INTEGER NOT NULL,
                `sc7_index_position` INTEGER NOT NULL,
                `sc7_content_title` TEXT, `sc7_content_subtitle` TEXT,
                `sc7_content_detail` TEXT, `sc7_content_detail_bullet_type` TEXT
            )
        """)
        db.execSQL("""
            INSERT INTO `section_child_7`
                (`section_child_7_id`,`section_head_added_id`,`sc7_index_position`,
                 `sc7_content_title`,`sc7_content_subtitle`,`sc7_content_detail`,
                 `sc7_content_detail_bullet_type`)
            SELECT `section_child_7_id`,`section_head_added_id`,`sc7_index_position`,
                   `sc7_content_title`,`sc7_content_subtitle`,`sc7_content_detail`,
                   `sc7_content_detail_bullet_type`
            FROM `section_child_7_old`
        """)
        db.execSQL("DROP TABLE `section_child_7_old`")

        // ── section_child_8 ───────────────────────────────────────────────────
        db.execSQL("ALTER TABLE `section_child_8` RENAME TO `section_child_8_old`")
        db.execSQL("""
            CREATE TABLE `section_child_8` (
                `section_child_8_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `section_head_added_id` INTEGER NOT NULL,
                `sc8_date` TEXT, `sc8_date_date_format` TEXT,
                `sc8_address` TEXT, `sc8_content` TEXT
            )
        """)
        db.execSQL("""
            INSERT INTO `section_child_8`
                (`section_child_8_id`,`section_head_added_id`,`sc8_date`,
                 `sc8_date_date_format`,`sc8_address`,`sc8_content`)
            SELECT `section_child_8_id`,`section_head_added_id`,`sc8_date`,
                   `sc8_date_date_format`,`sc8_address`,`sc8_content`
            FROM `section_child_8_old`
        """)
        db.execSQL("DROP TABLE `section_child_8_old`")

        // ── fcm_data (may not have existed in v2) ─────────────────────────────
        val cursor = db.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='fcm_data'"
        )
        val fcmExisted = cursor.count > 0
        cursor.close()

        if (fcmExisted) {
            db.execSQL("ALTER TABLE `fcm_data` RENAME TO `fcm_data_old`")
            db.execSQL("""
                CREATE TABLE `fcm_data` (
                    `fcm_data_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `fcm_message_type` TEXT, `fcm_notification_type` TEXT,
                    `fcm_title` TEXT, `fcm_message` TEXT, `fcm_image_url` TEXT,
                    `fcm_timestamp` TEXT, `fcm_is_read` INTEGER
                )
            """)
            db.execSQL("""
                INSERT INTO `fcm_data`
                    (`fcm_data_id`,`fcm_message_type`,`fcm_notification_type`,`fcm_title`,
                     `fcm_message`,`fcm_image_url`,`fcm_timestamp`,`fcm_is_read`)
                SELECT `fcm_data_id`,`fcm_message_type`,`fcm_notification_type`,`fcm_title`,
                       `fcm_message`,`fcm_image_url`,`fcm_timestamp`,`fcm_is_read`
                FROM `fcm_data_old`
            """)
            db.execSQL("DROP TABLE `fcm_data_old`")
        } else {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `fcm_data` (
                    `fcm_data_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `fcm_message_type` TEXT, `fcm_notification_type` TEXT,
                    `fcm_title` TEXT, `fcm_message` TEXT, `fcm_image_url` TEXT,
                    `fcm_timestamp` TEXT, `fcm_is_read` INTEGER
                )
            """)
        }
    }
}

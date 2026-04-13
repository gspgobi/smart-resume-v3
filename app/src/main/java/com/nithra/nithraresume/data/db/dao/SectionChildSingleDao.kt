package com.nithra.nithraresume.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nithra.nithraresume.data.db.entity.SectionChild1Entity
import com.nithra.nithraresume.data.db.entity.SectionChild4Entity
import com.nithra.nithraresume.data.db.entity.SectionChild5Entity
import com.nithra.nithraresume.data.db.entity.SectionChild8Entity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for section types that hold a single record per section_head_added:
 *   child_1 (Contact Info), child_4 (Declaration), child_5 (Paragraph), child_8 (Cover Letter)
 */
@Dao
interface SectionChildSingleDao {

    // ── Child 1 — Contact Information ────────────────────────────────────────

    @Query("SELECT * FROM section_child_1 WHERE section_head_added_id = :headId LIMIT 1")
    fun getChild1ByHeadId(headId: Int): Flow<SectionChild1Entity?>

    @Query("SELECT * FROM section_child_1 WHERE section_head_added_id = :headId LIMIT 1")
    suspend fun getChild1ByHeadIdOnce(headId: Int): SectionChild1Entity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild1(entity: SectionChild1Entity): Long

    @Update
    suspend fun updateChild1(entity: SectionChild1Entity)

    @Query("DELETE FROM section_child_1 WHERE section_head_added_id = :headId")
    suspend fun deleteChild1ByHeadId(headId: Int)

    // ── Child 4 — Declaration + Signature ────────────────────────────────────

    @Query("SELECT * FROM section_child_4 WHERE section_head_added_id = :headId LIMIT 1")
    fun getChild4ByHeadId(headId: Int): Flow<SectionChild4Entity?>

    @Query("SELECT * FROM section_child_4 WHERE section_head_added_id = :headId LIMIT 1")
    suspend fun getChild4ByHeadIdOnce(headId: Int): SectionChild4Entity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild4(entity: SectionChild4Entity): Long

    @Update
    suspend fun updateChild4(entity: SectionChild4Entity)

    @Query("DELETE FROM section_child_4 WHERE section_head_added_id = :headId")
    suspend fun deleteChild4ByHeadId(headId: Int)

    // ── Child 5 — Paragraph / Bulleted Text ──────────────────────────────────

    @Query("SELECT * FROM section_child_5 WHERE section_head_added_id = :headId LIMIT 1")
    fun getChild5ByHeadId(headId: Int): Flow<SectionChild5Entity?>

    @Query("SELECT * FROM section_child_5 WHERE section_head_added_id = :headId LIMIT 1")
    suspend fun getChild5ByHeadIdOnce(headId: Int): SectionChild5Entity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild5(entity: SectionChild5Entity): Long

    @Update
    suspend fun updateChild5(entity: SectionChild5Entity)

    @Query("DELETE FROM section_child_5 WHERE section_head_added_id = :headId")
    suspend fun deleteChild5ByHeadId(headId: Int)

    // ── Child 8 — Cover Letter ────────────────────────────────────────────────

    @Query("SELECT * FROM section_child_8 WHERE section_head_added_id = :headId LIMIT 1")
    fun getChild8ByHeadId(headId: Int): Flow<SectionChild8Entity?>

    @Query("SELECT * FROM section_child_8 WHERE section_head_added_id = :headId LIMIT 1")
    suspend fun getChild8ByHeadIdOnce(headId: Int): SectionChild8Entity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild8(entity: SectionChild8Entity): Long

    @Update
    suspend fun updateChild8(entity: SectionChild8Entity)

    @Query("DELETE FROM section_child_8 WHERE section_head_added_id = :headId")
    suspend fun deleteChild8ByHeadId(headId: Int)
}

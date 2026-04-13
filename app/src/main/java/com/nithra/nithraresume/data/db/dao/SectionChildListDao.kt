package com.nithra.nithraresume.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nithra.nithraresume.data.db.entity.SectionChild2Entity
import com.nithra.nithraresume.data.db.entity.SectionChild3Entity
import com.nithra.nithraresume.data.db.entity.SectionChild6Entity
import com.nithra.nithraresume.data.db.entity.SectionChild7Entity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for section types that hold a list of items per section_head_added:
 *   child_2 (Work Experience), child_3 (Education),
 *   child_6 (Split Text), child_7 (Multiple Item Text)
 */
@Dao
interface SectionChildListDao {

    // ── Child 2 — Work Experience ─────────────────────────────────────────────

    @Query("SELECT * FROM section_child_2 WHERE section_head_added_id = :headId ORDER BY sc2_index_position ASC")
    fun getChild2ByHeadId(headId: Int): Flow<List<SectionChild2Entity>>

    @Query("SELECT * FROM section_child_2 WHERE section_head_added_id = :headId ORDER BY sc2_index_position ASC")
    suspend fun getChild2ByHeadIdOnce(headId: Int): List<SectionChild2Entity>

    @Query("SELECT * FROM section_child_2 WHERE section_child_2_id = :id")
    suspend fun getChild2ById(id: Int): SectionChild2Entity?

    @Query("SELECT COUNT(*) FROM section_child_2 WHERE section_head_added_id = :headId")
    suspend fun getChild2Count(headId: Int): Int

    @Insert
    suspend fun insertChild2(entity: SectionChild2Entity): Long

    @Update
    suspend fun updateChild2(entity: SectionChild2Entity)

    @Delete
    suspend fun deleteChild2(entity: SectionChild2Entity)

    @Query("DELETE FROM section_child_2 WHERE section_head_added_id = :headId")
    suspend fun deleteChild2ByHeadId(headId: Int)

    @Query("UPDATE section_child_2 SET sc2_index_position = :position WHERE section_child_2_id = :id")
    suspend fun updateChild2Position(id: Int, position: Int)

    // ── Child 3 — Education ───────────────────────────────────────────────────

    @Query("SELECT * FROM section_child_3 WHERE section_head_added_id = :headId ORDER BY sc3_index_position ASC")
    fun getChild3ByHeadId(headId: Int): Flow<List<SectionChild3Entity>>

    @Query("SELECT * FROM section_child_3 WHERE section_head_added_id = :headId ORDER BY sc3_index_position ASC")
    suspend fun getChild3ByHeadIdOnce(headId: Int): List<SectionChild3Entity>

    @Query("SELECT * FROM section_child_3 WHERE section_child_3_id = :id")
    suspend fun getChild3ById(id: Int): SectionChild3Entity?

    @Query("SELECT COUNT(*) FROM section_child_3 WHERE section_head_added_id = :headId")
    suspend fun getChild3Count(headId: Int): Int

    @Insert
    suspend fun insertChild3(entity: SectionChild3Entity): Long

    @Update
    suspend fun updateChild3(entity: SectionChild3Entity)

    @Delete
    suspend fun deleteChild3(entity: SectionChild3Entity)

    @Query("DELETE FROM section_child_3 WHERE section_head_added_id = :headId")
    suspend fun deleteChild3ByHeadId(headId: Int)

    @Query("UPDATE section_child_3 SET sc3_index_position = :position WHERE section_child_3_id = :id")
    suspend fun updateChild3Position(id: Int, position: Int)

    // ── Child 6 — Split Text ──────────────────────────────────────────────────

    @Query("SELECT * FROM section_child_6 WHERE section_head_added_id = :headId ORDER BY sc6_index_position ASC")
    fun getChild6ByHeadId(headId: Int): Flow<List<SectionChild6Entity>>

    @Query("SELECT * FROM section_child_6 WHERE section_head_added_id = :headId ORDER BY sc6_index_position ASC")
    suspend fun getChild6ByHeadIdOnce(headId: Int): List<SectionChild6Entity>

    @Query("SELECT * FROM section_child_6 WHERE section_child_6_id = :id")
    suspend fun getChild6ById(id: Int): SectionChild6Entity?

    @Query("SELECT COUNT(*) FROM section_child_6 WHERE section_head_added_id = :headId")
    suspend fun getChild6Count(headId: Int): Int

    @Insert
    suspend fun insertChild6(entity: SectionChild6Entity): Long

    @Update
    suspend fun updateChild6(entity: SectionChild6Entity)

    @Delete
    suspend fun deleteChild6(entity: SectionChild6Entity)

    @Query("DELETE FROM section_child_6 WHERE section_head_added_id = :headId")
    suspend fun deleteChild6ByHeadId(headId: Int)

    @Query("UPDATE section_child_6 SET sc6_index_position = :position WHERE section_child_6_id = :id")
    suspend fun updateChild6Position(id: Int, position: Int)

    // ── Child 7 — Multiple Item Text ──────────────────────────────────────────

    @Query("SELECT * FROM section_child_7 WHERE section_head_added_id = :headId ORDER BY sc7_index_position ASC")
    fun getChild7ByHeadId(headId: Int): Flow<List<SectionChild7Entity>>

    @Query("SELECT * FROM section_child_7 WHERE section_head_added_id = :headId ORDER BY sc7_index_position ASC")
    suspend fun getChild7ByHeadIdOnce(headId: Int): List<SectionChild7Entity>

    @Query("SELECT * FROM section_child_7 WHERE section_child_7_id = :id")
    suspend fun getChild7ById(id: Int): SectionChild7Entity?

    @Query("SELECT COUNT(*) FROM section_child_7 WHERE section_head_added_id = :headId")
    suspend fun getChild7Count(headId: Int): Int

    @Insert
    suspend fun insertChild7(entity: SectionChild7Entity): Long

    @Update
    suspend fun updateChild7(entity: SectionChild7Entity)

    @Delete
    suspend fun deleteChild7(entity: SectionChild7Entity)

    @Query("DELETE FROM section_child_7 WHERE section_head_added_id = :headId")
    suspend fun deleteChild7ByHeadId(headId: Int)

    @Query("UPDATE section_child_7 SET sc7_index_position = :position WHERE section_child_7_id = :id")
    suspend fun updateChild7Position(id: Int, position: Int)
}
// update 65

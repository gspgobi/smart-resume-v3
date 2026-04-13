package com.nithra.nithraresume.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nithra.nithraresume.data.db.entity.SectionHeadAddedEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionHeadAddedDao {

    /** Reactive list for the section head screen — ordered by position. */
    @Query("SELECT * FROM section_head_added WHERE profile_id = :profileId ORDER BY sha_index_position ASC")
    fun getByProfileId(profileId: Int): Flow<List<SectionHeadAddedEntity>>

    /** One-shot read used by PDF builder on Dispatchers.IO. */
    @Query("SELECT * FROM section_head_added WHERE profile_id = :profileId ORDER BY sha_index_position ASC")
    suspend fun getByProfileIdOnce(profileId: Int): List<SectionHeadAddedEntity>

    /** Enabled sections only — used to render resume sections. */
    @Query("SELECT * FROM section_head_added WHERE profile_id = :profileId AND sha_is_enable = 1 ORDER BY sha_index_position ASC")
    suspend fun getEnabledByProfileId(profileId: Int): List<SectionHeadAddedEntity>

    @Query("SELECT * FROM section_head_added WHERE section_head_added_id = :id")
    suspend fun getById(id: Int): SectionHeadAddedEntity?

    @Query("SELECT COUNT(*) FROM section_head_added WHERE profile_id = :profileId")
    suspend fun getCountByProfileId(profileId: Int): Int

    @Insert
    suspend fun insert(entity: SectionHeadAddedEntity): Long

    @Update
    suspend fun update(entity: SectionHeadAddedEntity)

    @Delete
    suspend fun delete(entity: SectionHeadAddedEntity)

    @Query("DELETE FROM section_head_added WHERE profile_id = :profileId")
    suspend fun deleteByProfileId(profileId: Int)

    @Query("UPDATE section_head_added SET sha_index_position = :position WHERE section_head_added_id = :id")
    suspend fun updatePosition(id: Int, position: Int)

    @Query("UPDATE section_head_added SET sha_is_enable = :isEnable WHERE section_head_added_id = :id")
    suspend fun updateIsEnable(id: Int, isEnable: Boolean)

    @Query("UPDATE section_head_added SET sha_title = :title WHERE section_head_added_id = :id")
    suspend fun updateTitle(id: Int, title: String)
}

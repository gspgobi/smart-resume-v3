package com.nithra.nithraresume.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.nithra.nithraresume.data.db.entity.SectionHeadBaseEntity

@Dao
interface SectionHeadBaseDao {

    @Query("SELECT * FROM section_head_base ORDER BY section_head_base_id ASC")
    suspend fun getAll(): List<SectionHeadBaseEntity>

    @Query("SELECT * FROM section_head_base WHERE section_head_group_base_id = :groupId ORDER BY section_head_base_id ASC")
    suspend fun getByGroupId(groupId: Int): List<SectionHeadBaseEntity>

    @Query("SELECT * FROM section_head_base WHERE section_head_base_id = :id")
    suspend fun getById(id: Int): SectionHeadBaseEntity?
}
// update 68

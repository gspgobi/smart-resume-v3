package com.nithra.nithraresume.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.nithra.nithraresume.data.db.entity.SectionHeadGroupBaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionHeadGroupBaseDao {

    @Query("SELECT * FROM section_head_group_base ORDER BY section_head_group_base_id ASC")
    fun getAll(): Flow<List<SectionHeadGroupBaseEntity>>

    @Query("SELECT * FROM section_head_group_base ORDER BY section_head_group_base_id ASC")
    suspend fun getAllOnce(): List<SectionHeadGroupBaseEntity>
}

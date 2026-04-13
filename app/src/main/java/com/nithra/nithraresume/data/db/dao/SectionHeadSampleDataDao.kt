package com.nithra.nithraresume.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.nithra.nithraresume.data.db.entity.SectionHeadSampleDataEntity

@Dao
interface SectionHeadSampleDataDao {

    @Query("SELECT * FROM section_head_sample_data ORDER BY shsd_index_position ASC")
    suspend fun getAll(): List<SectionHeadSampleDataEntity>

    @Query("SELECT * FROM section_head_sample_data WHERE section_head_group_base_id = :groupId ORDER BY shsd_index_position ASC")
    suspend fun getByGroupId(groupId: Int): List<SectionHeadSampleDataEntity>

    @Query("SELECT * FROM section_head_sample_data WHERE shsd_is_default = 1 ORDER BY shsd_index_position ASC")
    suspend fun getDefaults(): List<SectionHeadSampleDataEntity>

    @Query("SELECT * FROM section_head_sample_data WHERE section_head_base_id = :baseId ORDER BY shsd_index_position ASC")
    suspend fun getByBaseId(baseId: Int): List<SectionHeadSampleDataEntity>

    @Query("SELECT * FROM section_head_sample_data WHERE section_head_sample_data_id = :id")
    suspend fun getById(id: Int): SectionHeadSampleDataEntity?
}

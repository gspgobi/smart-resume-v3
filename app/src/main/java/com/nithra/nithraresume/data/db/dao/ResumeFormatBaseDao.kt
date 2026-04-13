package com.nithra.nithraresume.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.nithra.nithraresume.data.db.entity.ResumeFormatBaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResumeFormatBaseDao {

    @Query("SELECT * FROM resume_format_base ORDER BY resume_format_base_id ASC")
    fun getAll(): Flow<List<ResumeFormatBaseEntity>>

    @Query("SELECT * FROM resume_format_base WHERE resume_format_base_id = :id")
    suspend fun getById(id: Int): ResumeFormatBaseEntity?

    @Query("SELECT * FROM resume_format_base WHERE resume_format_base_is_default = 1 LIMIT 1")
    suspend fun getDefault(): ResumeFormatBaseEntity?

    @Update
    suspend fun update(entity: ResumeFormatBaseEntity)
}

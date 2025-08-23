package com.nithra.nithraresume.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nithra.nithraresume.data.db.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profile WHERE up_is_sample_profile = 0 ORDER BY up_index_position ASC")
    fun getAll(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profile WHERE up_is_sample_profile = 0 ORDER BY up_index_position ASC")
    suspend fun getAllOnce(): List<UserProfileEntity>

    @Query("SELECT * FROM user_profile WHERE user_profile_id = :id")
    suspend fun getById(id: Int): UserProfileEntity?

    @Query("SELECT COUNT(*) FROM user_profile WHERE up_is_sample_profile = 0")
    suspend fun getCount(): Int

    @Insert
    suspend fun insert(entity: UserProfileEntity): Long

    @Update
    suspend fun update(entity: UserProfileEntity)

    @Delete
    suspend fun delete(entity: UserProfileEntity)

    @Query("UPDATE user_profile SET up_resume_file_name = :fileName WHERE user_profile_id = :id")
    suspend fun updateResumeFileName(id: Int, fileName: String)

    @Query("UPDATE user_profile SET resume_format_base_id = :formatId, up_font_style = :fontStyle, up_font_size = :fontSize, up_backgroud_color = :bgColor WHERE user_profile_id = :id")
    suspend fun updateFormatSettings(id: Int, formatId: Int, fontStyle: String, fontSize: Int, bgColor: String)
}
// update 00

package com.nithra.nithraresume.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.nithra.nithraresume.data.db.entity.FcmDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FcmDataDao {

    @Query("SELECT * FROM fcm_data ORDER BY fcm_data_id DESC")
    fun getAll(): Flow<List<FcmDataEntity>>

    @Query("SELECT * FROM fcm_data WHERE fcm_data_id = :id")
    suspend fun getById(id: Int): FcmDataEntity?

    @Query("SELECT COUNT(*) FROM fcm_data WHERE fcm_is_read = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert
    suspend fun insert(entity: FcmDataEntity): Long

    @Delete
    suspend fun delete(entity: FcmDataEntity)

    @Query("UPDATE fcm_data SET fcm_is_read = 1 WHERE fcm_data_id = :id")
    suspend fun markAsRead(id: Int)

    @Query("UPDATE fcm_data SET fcm_is_read = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM fcm_data")
    suspend fun deleteAll()
}
// update 63

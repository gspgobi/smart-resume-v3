package com.nithra.nithraresume.data.repository

import com.nithra.nithraresume.data.db.dao.FcmDataDao
import com.nithra.nithraresume.data.model.FcmData
import com.nithra.nithraresume.data.model.toEntity
import com.nithra.nithraresume.data.model.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmRepository @Inject constructor(
    private val dao: FcmDataDao
) {

    fun getAll(): Flow<List<FcmData>> =
        dao.getAll().map { list -> list.map { it.toModel() } }

    suspend fun getById(id: Int): FcmData? =
        dao.getById(id)?.toModel()

    fun getUnreadCount(): Flow<Int> =
        dao.getUnreadCount()

    suspend fun insert(data: FcmData): Long =
        dao.insert(data.toEntity())

    suspend fun delete(data: FcmData) =
        dao.delete(data.toEntity())

    suspend fun markAsRead(id: Int) =
        dao.markAsRead(id)

    suspend fun markAllAsRead() =
        dao.markAllAsRead()

    suspend fun deleteAll() =
        dao.deleteAll()
}
// update 102

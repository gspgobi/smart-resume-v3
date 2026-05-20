package com.nithra.nithraresume.data.repository

import com.nithra.nithraresume.data.db.dao.UserProfileDao
import com.nithra.nithraresume.data.model.UserProfile
import com.nithra.nithraresume.data.model.toEntity
import com.nithra.nithraresume.data.model.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val dao: UserProfileDao
) {

    fun getAll(): Flow<List<UserProfile>> =
        dao.getAll().map { list -> list.map { it.toModel() } }

    suspend fun getAllOnce(): List<UserProfile> =
        dao.getAllOnce().map { it.toModel() }

    suspend fun getById(id: Int): UserProfile? =
        dao.getById(id)?.toModel()

    fun getByIdFlow(id: Int): Flow<UserProfile?> =
        dao.getByIdFlow(id).map { it?.toModel() }

    /** Returns the new row id. Enforces max-20-profile limit at call site. */
    suspend fun insert(profile: UserProfile): Long =
        dao.insert(profile.toEntity())

    suspend fun update(profile: UserProfile) =
        dao.update(profile.toEntity())

    suspend fun delete(profile: UserProfile) =
        dao.delete(profile.toEntity())

    suspend fun getCount(): Int =
        dao.getCount()

    suspend fun updateResumeFileName(id: Int, fileName: String) =
        dao.updateResumeFileName(id, fileName)

    suspend fun updateFormatSettings(
        id: Int,
        formatId: Int,
        fontStyle: String,
        fontSize: Int,
        backgroundColor: String
    ) = dao.updateFormatSettings(id, formatId, fontStyle, fontSize, backgroundColor)
}

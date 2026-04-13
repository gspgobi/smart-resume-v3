package com.nithra.nithraresume.data.repository

import com.nithra.nithraresume.data.db.dao.ResumeFormatBaseDao
import com.nithra.nithraresume.data.model.ResumeFormat
import com.nithra.nithraresume.data.model.toEntity
import com.nithra.nithraresume.data.model.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResumeFormatRepository @Inject constructor(
    private val dao: ResumeFormatBaseDao
) {

    fun getAll(): Flow<List<ResumeFormat>> =
        dao.getAll().map { list -> list.map { it.toModel() } }

    suspend fun getById(id: Int): ResumeFormat? =
        dao.getById(id)?.toModel()

    suspend fun getDefault(): ResumeFormat? =
        dao.getDefault()?.toModel()

    suspend fun update(format: ResumeFormat) =
        dao.update(format.toEntity())
}

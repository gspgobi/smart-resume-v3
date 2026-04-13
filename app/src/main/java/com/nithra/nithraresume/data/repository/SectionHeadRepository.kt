package com.nithra.nithraresume.data.repository

import com.nithra.nithraresume.data.db.dao.SectionHeadAddedDao
import com.nithra.nithraresume.data.db.dao.SectionHeadBaseDao
import com.nithra.nithraresume.data.db.dao.SectionHeadGroupBaseDao
import com.nithra.nithraresume.data.db.dao.SectionHeadSampleDataDao
import com.nithra.nithraresume.data.model.SectionHeadAdded
import com.nithra.nithraresume.data.model.SectionHeadBase
import com.nithra.nithraresume.data.model.SectionHeadGroup
import com.nithra.nithraresume.data.model.SectionHeadSampleData
import com.nithra.nithraresume.data.model.toEntity
import com.nithra.nithraresume.data.model.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SectionHeadRepository @Inject constructor(
    private val groupDao: SectionHeadGroupBaseDao,
    private val baseDao: SectionHeadBaseDao,
    private val sampleDao: SectionHeadSampleDataDao,
    private val addedDao: SectionHeadAddedDao
) {

    // ── Section Head Group ────────────────────────────────────────────────────

    fun getAllGroups(): Flow<List<SectionHeadGroup>> =
        groupDao.getAll().map { list -> list.map { it.toModel() } }

    suspend fun getAllGroupsOnce(): List<SectionHeadGroup> =
        groupDao.getAllOnce().map { it.toModel() }

    // ── Section Head Base ─────────────────────────────────────────────────────

    suspend fun getAllBase(): List<SectionHeadBase> =
        baseDao.getAll().map { it.toModel() }

    suspend fun getBaseByGroupId(groupId: Int): List<SectionHeadBase> =
        baseDao.getByGroupId(groupId).map { it.toModel() }

    suspend fun getBaseById(id: Int): SectionHeadBase? =
        baseDao.getById(id)?.toModel()

    // ── Section Head Sample Data ──────────────────────────────────────────────

    suspend fun getAllSampleData(): List<SectionHeadSampleData> =
        sampleDao.getAll().map { it.toModel() }

    suspend fun getSampleDataByGroupId(groupId: Int): List<SectionHeadSampleData> =
        sampleDao.getByGroupId(groupId).map { it.toModel() }

    suspend fun getDefaultSampleData(): List<SectionHeadSampleData> =
        sampleDao.getDefaults().map { it.toModel() }

    suspend fun getSampleDataByBaseId(baseId: Int): List<SectionHeadSampleData> =
        sampleDao.getByBaseId(baseId).map { it.toModel() }

    suspend fun getSampleDataById(id: Int): SectionHeadSampleData? =
        sampleDao.getById(id)?.toModel()

    // ── Section Head Added ────────────────────────────────────────────────────

    fun getAddedByProfileId(profileId: Int): Flow<List<SectionHeadAdded>> =
        addedDao.getByProfileId(profileId).map { list -> list.map { it.toModel() } }

    suspend fun getAddedByProfileIdOnce(profileId: Int): List<SectionHeadAdded> =
        addedDao.getByProfileIdOnce(profileId).map { it.toModel() }

    suspend fun getEnabledByProfileId(profileId: Int): List<SectionHeadAdded> =
        addedDao.getEnabledByProfileId(profileId).map { it.toModel() }

    suspend fun getAddedById(id: Int): SectionHeadAdded? =
        addedDao.getById(id)?.toModel()

    suspend fun getAddedCountByProfileId(profileId: Int): Int =
        addedDao.getCountByProfileId(profileId)

    /** Returns new row id. Caller must check max-20-section limit first. */
    suspend fun insertAdded(section: SectionHeadAdded): Long =
        addedDao.insert(section.toEntity())

    suspend fun updateAdded(section: SectionHeadAdded) =
        addedDao.update(section.toEntity())

    suspend fun deleteAdded(section: SectionHeadAdded) =
        addedDao.delete(section.toEntity())

    suspend fun deleteAddedByProfileId(profileId: Int) =
        addedDao.deleteByProfileId(profileId)

    suspend fun updateAddedPosition(id: Int, position: Int) =
        addedDao.updatePosition(id, position)

    suspend fun updateAddedIsEnable(id: Int, isEnable: Boolean) =
        addedDao.updateIsEnable(id, isEnable)

    suspend fun updateAddedTitle(id: Int, title: String) =
        addedDao.updateTitle(id, title)
}
// update 105

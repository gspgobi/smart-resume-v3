package com.nithra.nithraresume.data.repository

import com.nithra.nithraresume.data.db.dao.SectionChildListDao
import com.nithra.nithraresume.data.db.dao.SectionChildSingleDao
import com.nithra.nithraresume.data.model.SectionChild1
import com.nithra.nithraresume.data.model.SectionChild2
import com.nithra.nithraresume.data.model.SectionChild3
import com.nithra.nithraresume.data.model.SectionChild4
import com.nithra.nithraresume.data.model.SectionChild5
import com.nithra.nithraresume.data.model.SectionChild6
import com.nithra.nithraresume.data.model.SectionChild7
import com.nithra.nithraresume.data.model.SectionChild8
import com.nithra.nithraresume.data.model.toEntity
import com.nithra.nithraresume.data.model.toModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SectionChildRepository @Inject constructor(
    private val singleDao: SectionChildSingleDao,
    private val listDao: SectionChildListDao
) {

    // ── Child 1 — Contact Information ─────────────────────────────────────────

    fun getChild1(headId: Int): Flow<SectionChild1?> =
        singleDao.getChild1ByHeadId(headId).map { it?.toModel() }

    suspend fun getChild1Once(headId: Int): SectionChild1? =
        singleDao.getChild1ByHeadIdOnce(headId)?.toModel()

    suspend fun saveChild1(child: SectionChild1): Long =
        singleDao.insertChild1(child.toEntity())

    suspend fun updateChild1(child: SectionChild1) =
        singleDao.updateChild1(child.toEntity())

    suspend fun deleteChild1(headId: Int) =
        singleDao.deleteChild1ByHeadId(headId)

    suspend fun migrateV2UserImagePaths(newDir: String) =
        singleDao.migrateV2UserImagePaths(newDir)

    suspend fun migrateV2SignatureImagePaths(newDir: String) =
        singleDao.migrateV2SignatureImagePaths(newDir)

    // ── Child 2 — Work Experience ─────────────────────────────────────────────

    fun getChild2List(headId: Int): Flow<List<SectionChild2>> =
        listDao.getChild2ByHeadId(headId).map { list -> list.map { it.toModel() } }

    suspend fun getChild2ListOnce(headId: Int): List<SectionChild2> =
        listDao.getChild2ByHeadIdOnce(headId).map { it.toModel() }

    suspend fun getChild2ById(id: Int): SectionChild2? =
        listDao.getChild2ById(id)?.toModel()

    suspend fun getChild2Count(headId: Int): Int =
        listDao.getChild2Count(headId)

    suspend fun insertChild2(child: SectionChild2): Long =
        listDao.insertChild2(child.toEntity())

    suspend fun updateChild2(child: SectionChild2) =
        listDao.updateChild2(child.toEntity())

    suspend fun deleteChild2(child: SectionChild2) =
        listDao.deleteChild2(child.toEntity())

    suspend fun deleteChild2ByHeadId(headId: Int) =
        listDao.deleteChild2ByHeadId(headId)

    suspend fun updateChild2Position(id: Int, position: Int) =
        listDao.updateChild2Position(id, position)

    // ── Child 3 — Education ───────────────────────────────────────────────────

    fun getChild3List(headId: Int): Flow<List<SectionChild3>> =
        listDao.getChild3ByHeadId(headId).map { list -> list.map { it.toModel() } }

    suspend fun getChild3ListOnce(headId: Int): List<SectionChild3> =
        listDao.getChild3ByHeadIdOnce(headId).map { it.toModel() }

    suspend fun getChild3ById(id: Int): SectionChild3? =
        listDao.getChild3ById(id)?.toModel()

    suspend fun getChild3Count(headId: Int): Int =
        listDao.getChild3Count(headId)

    suspend fun insertChild3(child: SectionChild3): Long =
        listDao.insertChild3(child.toEntity())

    suspend fun updateChild3(child: SectionChild3) =
        listDao.updateChild3(child.toEntity())

    suspend fun deleteChild3(child: SectionChild3) =
        listDao.deleteChild3(child.toEntity())

    suspend fun deleteChild3ByHeadId(headId: Int) =
        listDao.deleteChild3ByHeadId(headId)

    suspend fun updateChild3Position(id: Int, position: Int) =
        listDao.updateChild3Position(id, position)

    // ── Child 4 — Declaration + Signature ─────────────────────────────────────

    fun getChild4(headId: Int): Flow<SectionChild4?> =
        singleDao.getChild4ByHeadId(headId).map { it?.toModel() }

    suspend fun getChild4Once(headId: Int): SectionChild4? =
        singleDao.getChild4ByHeadIdOnce(headId)?.toModel()

    suspend fun saveChild4(child: SectionChild4): Long =
        singleDao.insertChild4(child.toEntity())

    suspend fun updateChild4(child: SectionChild4) =
        singleDao.updateChild4(child.toEntity())

    suspend fun deleteChild4(headId: Int) =
        singleDao.deleteChild4ByHeadId(headId)

    // ── Child 5 — Paragraph / Bulleted Text ───────────────────────────────────

    fun getChild5(headId: Int): Flow<SectionChild5?> =
        singleDao.getChild5ByHeadId(headId).map { it?.toModel() }

    suspend fun getChild5Once(headId: Int): SectionChild5? =
        singleDao.getChild5ByHeadIdOnce(headId)?.toModel()

    suspend fun saveChild5(child: SectionChild5): Long =
        singleDao.insertChild5(child.toEntity())

    suspend fun updateChild5(child: SectionChild5) =
        singleDao.updateChild5(child.toEntity())

    suspend fun deleteChild5(headId: Int) =
        singleDao.deleteChild5ByHeadId(headId)

    // ── Child 6 — Split Text ───────────────────────────────────────────────────

    fun getChild6List(headId: Int): Flow<List<SectionChild6>> =
        listDao.getChild6ByHeadId(headId).map { list -> list.map { it.toModel() } }

    suspend fun getChild6ListOnce(headId: Int): List<SectionChild6> =
        listDao.getChild6ByHeadIdOnce(headId).map { it.toModel() }

    suspend fun getChild6ById(id: Int): SectionChild6? =
        listDao.getChild6ById(id)?.toModel()

    suspend fun getChild6Count(headId: Int): Int =
        listDao.getChild6Count(headId)

    suspend fun insertChild6(child: SectionChild6): Long =
        listDao.insertChild6(child.toEntity())

    suspend fun updateChild6(child: SectionChild6) =
        listDao.updateChild6(child.toEntity())

    suspend fun deleteChild6(child: SectionChild6) =
        listDao.deleteChild6(child.toEntity())

    suspend fun deleteChild6ByHeadId(headId: Int) =
        listDao.deleteChild6ByHeadId(headId)

    suspend fun updateChild6Position(id: Int, position: Int) =
        listDao.updateChild6Position(id, position)

    // ── Child 7 — Multiple Item Text ──────────────────────────────────────────

    fun getChild7List(headId: Int): Flow<List<SectionChild7>> =
        listDao.getChild7ByHeadId(headId).map { list -> list.map { it.toModel() } }

    suspend fun getChild7ListOnce(headId: Int): List<SectionChild7> =
        listDao.getChild7ByHeadIdOnce(headId).map { it.toModel() }

    suspend fun getChild7ById(id: Int): SectionChild7? =
        listDao.getChild7ById(id)?.toModel()

    suspend fun getChild7Count(headId: Int): Int =
        listDao.getChild7Count(headId)

    suspend fun insertChild7(child: SectionChild7): Long =
        listDao.insertChild7(child.toEntity())

    suspend fun updateChild7(child: SectionChild7) =
        listDao.updateChild7(child.toEntity())

    suspend fun deleteChild7(child: SectionChild7) =
        listDao.deleteChild7(child.toEntity())

    suspend fun deleteChild7ByHeadId(headId: Int) =
        listDao.deleteChild7ByHeadId(headId)

    suspend fun updateChild7Position(id: Int, position: Int) =
        listDao.updateChild7Position(id, position)

    // ── Child 8 — Cover Letter ─────────────────────────────────────────────────

    fun getChild8(headId: Int): Flow<SectionChild8?> =
        singleDao.getChild8ByHeadId(headId).map { it?.toModel() }

    suspend fun getChild8Once(headId: Int): SectionChild8? =
        singleDao.getChild8ByHeadIdOnce(headId)?.toModel()

    suspend fun saveChild8(child: SectionChild8): Long =
        singleDao.insertChild8(child.toEntity())

    suspend fun updateChild8(child: SectionChild8) =
        singleDao.updateChild8(child.toEntity())

    suspend fun deleteChild8(headId: Int) =
        singleDao.deleteChild8ByHeadId(headId)
}

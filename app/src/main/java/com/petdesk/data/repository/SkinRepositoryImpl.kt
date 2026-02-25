package com.petdesk.data.repository

import com.petdesk.data.local.dao.SkinDao
import com.petdesk.data.local.entity.SkinEntity
import com.petdesk.domain.repository.SkinRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Skin Repository Implementation
 */
@Singleton
class SkinRepositoryImpl @Inject constructor(
    private val skinDao: SkinDao
) : SkinRepository {

    override fun getAllSkins(): Flow<List<SkinEntity>> = skinDao.getAllSkins()

    override fun getUnlockedSkins(): Flow<List<SkinEntity>> = skinDao.getUnlockedSkins()

    override fun getSkinsByCategory(category: String): Flow<List<SkinEntity>> =
        skinDao.getSkinsByCategory(category)

    override fun searchSkins(keyword: String): Flow<List<SkinEntity>> =
        skinDao.searchSkins(keyword)

    override suspend fun getSkinById(id: Long): SkinEntity? = skinDao.getSkinById(id)

    override suspend fun getDefaultSkin(): SkinEntity? = skinDao.getDefaultSkin()

    override suspend fun insertSkin(skin: SkinEntity): Long = skinDao.insertSkin(skin)

    override suspend fun insertSkins(skins: List<SkinEntity>) = skinDao.insertSkins(skins)

    override suspend fun updateSkin(skin: SkinEntity) = skinDao.updateSkin(skin)

    override suspend fun deleteSkin(skin: SkinEntity) = skinDao.deleteSkin(skin)

    override suspend fun updateUnlockStatus(id: Long, isUnlocked: Boolean) =
        skinDao.updateUnlockStatus(id, isUnlocked)

    override suspend fun getSkinCount(): Int = skinDao.getSkinCount()
}

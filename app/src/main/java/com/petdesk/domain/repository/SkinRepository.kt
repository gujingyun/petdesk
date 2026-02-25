package com.petdesk.domain.repository

import com.petdesk.data.local.entity.SkinEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for skin management
 */
interface SkinRepository {
    fun getAllSkins(): Flow<List<SkinEntity>>
    fun getUnlockedSkins(): Flow<List<SkinEntity>>
    fun getSkinsByCategory(category: String): Flow<List<SkinEntity>>
    fun searchSkins(keyword: String): Flow<List<SkinEntity>>
    suspend fun getSkinById(id: Long): SkinEntity?
    suspend fun getDefaultSkin(): SkinEntity?
    suspend fun insertSkin(skin: SkinEntity): Long
    suspend fun insertSkins(skins: List<SkinEntity>)
    suspend fun updateSkin(skin: SkinEntity)
    suspend fun deleteSkin(skin: SkinEntity)
    suspend fun updateUnlockStatus(id: Long, isUnlocked: Boolean)
    suspend fun getSkinCount(): Int
}

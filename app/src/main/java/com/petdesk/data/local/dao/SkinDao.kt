package com.petdesk.data.local.dao

import androidx.room.*
import com.petdesk.data.local.entity.SkinEntity
import kotlinx.coroutines.flow.Flow

/**
 * 皮肤表 DAO
 */
@Dao
interface SkinDao {
    @Query("SELECT * FROM skins ORDER BY isDefault DESC, createdAt DESC")
    fun getAllSkins(): Flow<List<SkinEntity>>

    @Query("SELECT * FROM skins WHERE isUnlocked = 1 ORDER BY isDefault DESC, createdAt DESC")
    fun getUnlockedSkins(): Flow<List<SkinEntity>>

    @Query("SELECT * FROM skins WHERE category = :category ORDER BY createdAt DESC")
    fun getSkinsByCategory(category: String): Flow<List<SkinEntity>>

    @Query("SELECT * FROM skins WHERE id = :id")
    suspend fun getSkinById(id: Long): SkinEntity?

    @Query("SELECT * FROM skins WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultSkin(): SkinEntity?

    @Query("SELECT * FROM skins WHERE name LIKE '%' || :keyword || '%'")
    fun searchSkins(keyword: String): Flow<List<SkinEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkin(skin: SkinEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkins(skins: List<SkinEntity>)

    @Update
    suspend fun updateSkin(skin: SkinEntity)

    @Delete
    suspend fun deleteSkin(skin: SkinEntity)

    @Query("UPDATE skins SET isUnlocked = :isUnlocked WHERE id = :id")
    suspend fun updateUnlockStatus(id: Long, isUnlocked: Boolean)

    @Query("SELECT COUNT(*) FROM skins")
    suspend fun getSkinCount(): Int
}

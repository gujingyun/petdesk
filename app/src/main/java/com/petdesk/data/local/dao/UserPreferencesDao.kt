package com.petdesk.data.local.dao

import androidx.room.*
import com.petdesk.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

/**
 * 用户配置表 DAO
 */
@Dao
interface UserPreferencesDao {
    @Query("SELECT * FROM user_preferences WHERE userId = :userId ORDER BY category, `key`")
    fun getPreferencesByUserId(userId: Long): Flow<List<UserPreferencesEntity>>

    @Query("SELECT * FROM user_preferences WHERE userId = :userId AND category = :category ORDER BY `key`")
    fun getPreferencesByCategory(userId: Long, category: String): Flow<List<UserPreferencesEntity>>

    @Query("SELECT * FROM user_preferences WHERE userId = :userId AND `key` = :key")
    suspend fun getPreferenceByKey(userId: Long, key: String): UserPreferencesEntity?

    @Query("SELECT * FROM user_preferences WHERE userId = :userId AND `key` = :key")
    fun getPreferenceByKeyFlow(userId: Long, key: String): Flow<UserPreferencesEntity?>

    @Query("SELECT * FROM user_preferences WHERE userId = :userId AND `key` LIKE :keyPattern || '%'")
    fun getPreferencesByKeyPattern(userId: Long, keyPattern: String): Flow<List<UserPreferencesEntity>>

    @Query("SELECT * FROM user_preferences WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedPreferences(userId: Long): List<UserPreferencesEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(preference: UserPreferencesEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(preferences: List<UserPreferencesEntity>)

    @Update
    suspend fun updatePreference(preference: UserPreferencesEntity)

    @Delete
    suspend fun deletePreference(preference: UserPreferencesEntity)

    @Query("DELETE FROM user_preferences WHERE userId = :userId")
    suspend fun deleteAllPreferencesByUserId(userId: Long)

    @Query("DELETE FROM user_preferences WHERE userId = :userId AND category = :category")
    suspend fun deletePreferencesByCategory(userId: Long, category: String)

    @Query("UPDATE user_preferences SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("UPDATE user_preferences SET value = :value, updatedAt = :timestamp WHERE userId = :userId AND `key` = :key")
    suspend fun updateValue(userId: Long, key: String, value: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM user_preferences WHERE userId = :userId")
    suspend fun getPreferenceCount(userId: Long): Int
}

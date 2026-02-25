package com.petdesk.domain.repository

import com.petdesk.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user preferences management
 */
interface UserPreferencesRepository {
    fun getPreferencesByUserId(userId: Long): Flow<List<UserPreferencesEntity>>
    fun getPreferencesByCategory(userId: Long, category: String): Flow<List<UserPreferencesEntity>>
    fun getPreferenceByKeyFlow(userId: Long, key: String): Flow<UserPreferencesEntity?>
    fun getPreferencesByKeyPattern(userId: Long, keyPattern: String): Flow<List<UserPreferencesEntity>>
    suspend fun getPreferenceByKey(userId: Long, key: String): UserPreferencesEntity?
    suspend fun getUnsyncedPreferences(userId: Long): List<UserPreferencesEntity>
    suspend fun insertPreference(preference: UserPreferencesEntity): Long
    suspend fun insertPreferences(preferences: List<UserPreferencesEntity>)
    suspend fun updatePreference(preference: UserPreferencesEntity)
    suspend fun deletePreference(preference: UserPreferencesEntity)
    suspend fun deleteAllPreferencesByUserId(userId: Long)
    suspend fun deletePreferencesByCategory(userId: Long, category: String)
    suspend fun markAsSynced(id: Long)
    suspend fun updateValue(userId: Long, key: String, value: String)
    suspend fun getPreferenceCount(userId: Long): Int
}

package com.petdesk.data.repository

import com.petdesk.data.local.dao.UserPreferencesDao
import com.petdesk.data.local.entity.UserPreferencesEntity
import com.petdesk.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User Preferences Repository Implementation
 */
@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val userPreferencesDao: UserPreferencesDao
) : UserPreferencesRepository {

    override fun getPreferencesByUserId(userId: Long): Flow<List<UserPreferencesEntity>> =
        userPreferencesDao.getPreferencesByUserId(userId)

    override fun getPreferencesByCategory(userId: Long, category: String): Flow<List<UserPreferencesEntity>> =
        userPreferencesDao.getPreferencesByCategory(userId, category)

    override fun getPreferenceByKeyFlow(userId: Long, key: String): Flow<UserPreferencesEntity?> =
        userPreferencesDao.getPreferenceByKeyFlow(userId, key)

    override fun getPreferencesByKeyPattern(userId: Long, keyPattern: String): Flow<List<UserPreferencesEntity>> =
        userPreferencesDao.getPreferencesByKeyPattern(userId, keyPattern)

    override suspend fun getPreferenceByKey(userId: Long, key: String): UserPreferencesEntity? =
        userPreferencesDao.getPreferenceByKey(userId, key)

    override suspend fun getUnsyncedPreferences(userId: Long): List<UserPreferencesEntity> =
        userPreferencesDao.getUnsyncedPreferences(userId)

    override suspend fun insertPreference(preference: UserPreferencesEntity): Long =
        userPreferencesDao.insertPreference(preference)

    override suspend fun insertPreferences(preferences: List<UserPreferencesEntity>) =
        userPreferencesDao.insertPreferences(preferences)

    override suspend fun updatePreference(preference: UserPreferencesEntity) =
        userPreferencesDao.updatePreference(preference)

    override suspend fun deletePreference(preference: UserPreferencesEntity) =
        userPreferencesDao.deletePreference(preference)

    override suspend fun deleteAllPreferencesByUserId(userId: Long) =
        userPreferencesDao.deleteAllPreferencesByUserId(userId)

    override suspend fun deletePreferencesByCategory(userId: Long, category: String) =
        userPreferencesDao.deletePreferencesByCategory(userId, category)

    override suspend fun markAsSynced(id: Long) = userPreferencesDao.markAsSynced(id)

    override suspend fun updateValue(userId: Long, key: String, value: String) =
        userPreferencesDao.updateValue(userId, key, value, System.currentTimeMillis())

    override suspend fun getPreferenceCount(userId: Long): Int =
        userPreferencesDao.getPreferenceCount(userId)
}

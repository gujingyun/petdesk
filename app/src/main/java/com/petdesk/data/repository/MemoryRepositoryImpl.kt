package com.petdesk.data.repository

import com.petdesk.data.local.dao.MemoryDao
import com.petdesk.data.local.entity.MemoryEntity
import com.petdesk.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Memory Repository Implementation
 */
@Singleton
class MemoryRepositoryImpl @Inject constructor(
    private val memoryDao: MemoryDao
) : MemoryRepository {

    override fun getMemoriesByUserId(userId: Long): Flow<List<MemoryEntity>> =
        memoryDao.getMemoriesByUserId(userId)

    override fun getMemoriesByCategory(userId: Long, category: String): Flow<List<MemoryEntity>> =
        memoryDao.getMemoriesByCategory(userId, category)

    override fun getRecentMemories(userId: Long, limit: Int): Flow<List<MemoryEntity>> =
        memoryDao.getRecentMemories(userId, limit)

    override fun getMemoriesByKeyPattern(userId: Long, keyPattern: String): Flow<List<MemoryEntity>> =
        memoryDao.getMemoriesByKeyPattern(userId, keyPattern)

    override suspend fun getMemoryByKey(userId: Long, key: String): MemoryEntity? =
        memoryDao.getMemoryByKey(userId, key)

    override suspend fun insertMemory(memory: MemoryEntity): Long =
        memoryDao.insertMemory(memory)

    override suspend fun insertMemories(memories: List<MemoryEntity>) =
        memoryDao.insertMemories(memories)

    override suspend fun updateMemory(memory: MemoryEntity) =
        memoryDao.updateMemory(memory)

    override suspend fun deleteMemory(memory: MemoryEntity) =
        memoryDao.deleteMemory(memory)

    override suspend fun deleteAllMemoriesByUserId(userId: Long) =
        memoryDao.deleteAllMemoriesByUserId(userId)

    override suspend fun deleteMemoriesByCategory(userId: Long, category: String) =
        memoryDao.deleteMemoriesByCategory(userId, category)

    override suspend fun updateAccessInfo(id: Long) =
        memoryDao.updateAccessInfo(id, System.currentTimeMillis())

    override suspend fun getMemoryCount(userId: Long): Int =
        memoryDao.getMemoryCount(userId)
}

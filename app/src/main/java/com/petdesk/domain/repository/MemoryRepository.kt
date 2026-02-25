package com.petdesk.domain.repository

import com.petdesk.data.local.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for memory management
 */
interface MemoryRepository {
    fun getMemoriesByUserId(userId: Long): Flow<List<MemoryEntity>>
    fun getMemoriesByCategory(userId: Long, category: String): Flow<List<MemoryEntity>>
    fun getRecentMemories(userId: Long, limit: Int): Flow<List<MemoryEntity>>
    fun getMemoriesByKeyPattern(userId: Long, keyPattern: String): Flow<List<MemoryEntity>>
    suspend fun getMemoryByKey(userId: Long, key: String): MemoryEntity?
    suspend fun insertMemory(memory: MemoryEntity): Long
    suspend fun insertMemories(memories: List<MemoryEntity>)
    suspend fun updateMemory(memory: MemoryEntity)
    suspend fun deleteMemory(memory: MemoryEntity)
    suspend fun deleteAllMemoriesByUserId(userId: Long)
    suspend fun deleteMemoriesByCategory(userId: Long, category: String)
    suspend fun updateAccessInfo(id: Long)
    suspend fun getMemoryCount(userId: Long): Int
}

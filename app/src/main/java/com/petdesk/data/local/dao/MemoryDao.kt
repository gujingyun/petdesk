package com.petdesk.data.local.dao

import androidx.room.*
import com.petdesk.data.local.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 记忆表 DAO
 */
@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories WHERE userId = :userId ORDER BY importance DESC, updatedAt DESC")
    fun getMemoriesByUserId(userId: Long): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE userId = :userId AND category = :category ORDER BY importance DESC")
    fun getMemoriesByCategory(userId: Long, category: String): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE userId = :userId AND `key` = :key")
    suspend fun getMemoryByKey(userId: Long, key: String): MemoryEntity?

    @Query("SELECT * FROM memories WHERE userId = :userId AND `key` LIKE :keyPattern || '%'")
    fun getMemoriesByKeyPattern(userId: Long, keyPattern: String): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE userId = :userId ORDER BY lastAccessedAt DESC LIMIT :limit")
    fun getRecentMemories(userId: Long, limit: Int): Flow<List<MemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemories(memories: List<MemoryEntity>)

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Delete
    suspend fun deleteMemory(memory: MemoryEntity)

    @Query("DELETE FROM memories WHERE userId = :userId")
    suspend fun deleteAllMemoriesByUserId(userId: Long)

    @Query("DELETE FROM memories WHERE userId = :userId AND category = :category")
    suspend fun deleteMemoriesByCategory(userId: Long, category: String)

    @Query("UPDATE memories SET accessCount = accessCount + 1, lastAccessedAt = :timestamp WHERE id = :id")
    suspend fun updateAccessInfo(id: Long, timestamp: Long)

    @Query("SELECT COUNT(*) FROM memories WHERE userId = :userId")
    suspend fun getMemoryCount(userId: Long): Int
}

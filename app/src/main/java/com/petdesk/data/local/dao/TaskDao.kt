package com.petdesk.data.local.dao

import androidx.room.*
import com.petdesk.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * 任务记录表 DAO
 */
@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTasksByUserId(userId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND status = :status ORDER BY priority DESC, createdAt DESC")
    fun getTasksByStatus(userId: Long, status: Int): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND taskType = :taskType ORDER BY createdAt DESC")
    fun getTasksByType(userId: Long, taskType: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND scheduledAt > 0 AND scheduledAt <= :time AND status = 0 ORDER BY priority DESC")
    fun getScheduledTasks(userId: Long, time: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentTasks(userId: Long, limit: Int): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND title LIKE '%' || :keyword || '%'")
    fun searchTasks(userId: Long, keyword: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE userId = :userId")
    suspend fun deleteAllTasksByUserId(userId: Long)

    @Query("UPDATE tasks SET status = :status, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateTaskStatus(id: Long, status: Int, timestamp: Long)

    @Query("UPDATE tasks SET status = 2, completedAt = :timestamp, result = :result WHERE id = :id")
    suspend fun completeTask(id: Long, timestamp: Long, result: String)

    @Query("UPDATE tasks SET status = 3, errorMessage = :errorMessage WHERE id = :id")
    suspend fun failTask(id: Long, errorMessage: String)

    @Query("SELECT COUNT(*) FROM tasks WHERE userId = :userId AND status = :status")
    suspend fun getTaskCountByStatus(userId: Long, status: Int): Int
}

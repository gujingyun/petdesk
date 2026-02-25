package com.petdesk.domain.repository

import com.petdesk.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for task management
 */
interface TaskRepository {
    fun getTasksByUserId(userId: Long): Flow<List<TaskEntity>>
    fun getTasksByStatus(userId: Long, status: Int): Flow<List<TaskEntity>>
    fun getTasksByType(userId: Long, taskType: String): Flow<List<TaskEntity>>
    fun getScheduledTasks(userId: Long, time: Long): Flow<List<TaskEntity>>
    fun getRecentTasks(userId: Long, limit: Int): Flow<List<TaskEntity>>
    fun searchTasks(userId: Long, keyword: String): Flow<List<TaskEntity>>
    suspend fun getTaskById(id: Long): TaskEntity?
    suspend fun insertTask(task: TaskEntity): Long
    suspend fun updateTask(task: TaskEntity)
    suspend fun deleteTask(task: TaskEntity)
    suspend fun deleteAllTasksByUserId(userId: Long)
    suspend fun updateTaskStatus(id: Long, status: Int)
    suspend fun completeTask(id: Long, result: String)
    suspend fun failTask(id: Long, errorMessage: String)
    suspend fun getTaskCountByStatus(userId: Long, status: Int): Int
}

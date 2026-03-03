package com.petdesk.domain.repository

import com.petdesk.domain.model.Task
import com.petdesk.domain.model.TaskStatus
import com.petdesk.domain.model.TaskType
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for task management
 */
interface TaskRepository {
    fun getTasksByUserId(userId: Long): Flow<List<Task>>
    fun getTasksByStatus(userId: Long, status: TaskStatus): Flow<List<Task>>
    fun getTasksByType(userId: Long, taskType: TaskType): Flow<List<Task>>
    fun getScheduledTasks(userId: Long, time: Long): Flow<List<Task>>
    fun getUpcomingScheduledTasks(userId: Long): Flow<List<Task>>
    fun getRecentTasks(userId: Long, limit: Int): Flow<List<Task>>
    fun getPendingTasks(userId: Long): Flow<List<Task>>
    fun searchTasks(userId: Long, keyword: String): Flow<List<Task>>
    suspend fun getTaskById(id: Long): Task?
    suspend fun insertTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun deleteAllTasksByUserId(userId: Long)
    suspend fun updateTaskStatus(id: Long, status: TaskStatus)
    suspend fun startTask(id: Long)
    suspend fun completeTask(id: Long, result: String)
    suspend fun failTask(id: Long, errorMessage: String)
    suspend fun cancelTask(id: Long)
    suspend fun getTaskCountByStatus(userId: Long, status: TaskStatus): Int
    suspend fun getActiveTaskCount(userId: Long): Int
}

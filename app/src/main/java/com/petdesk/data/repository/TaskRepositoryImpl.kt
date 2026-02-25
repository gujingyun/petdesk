package com.petdesk.data.repository

import com.petdesk.data.local.dao.TaskDao
import com.petdesk.data.local.entity.TaskEntity
import com.petdesk.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Task Repository Implementation
 */
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getTasksByUserId(userId: Long): Flow<List<TaskEntity>> =
        taskDao.getTasksByUserId(userId)

    override fun getTasksByStatus(userId: Long, status: Int): Flow<List<TaskEntity>> =
        taskDao.getTasksByStatus(userId, status)

    override fun getTasksByType(userId: Long, taskType: String): Flow<List<TaskEntity>> =
        taskDao.getTasksByType(userId, taskType)

    override fun getScheduledTasks(userId: Long, time: Long): Flow<List<TaskEntity>> =
        taskDao.getScheduledTasks(userId, time)

    override fun getRecentTasks(userId: Long, limit: Int): Flow<List<TaskEntity>> =
        taskDao.getRecentTasks(userId, limit)

    override fun searchTasks(userId: Long, keyword: String): Flow<List<TaskEntity>> =
        taskDao.searchTasks(userId, keyword)

    override suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)

    override suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    override suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    override suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    override suspend fun deleteAllTasksByUserId(userId: Long) =
        taskDao.deleteAllTasksByUserId(userId)

    override suspend fun updateTaskStatus(id: Long, status: Int) =
        taskDao.updateTaskStatus(id, status, System.currentTimeMillis())

    override suspend fun completeTask(id: Long, result: String) =
        taskDao.completeTask(id, System.currentTimeMillis(), result)

    override suspend fun failTask(id: Long, errorMessage: String) =
        taskDao.failTask(id, errorMessage)

    override suspend fun getTaskCountByStatus(userId: Long, status: Int): Int =
        taskDao.getTaskCountByStatus(userId, status)
}

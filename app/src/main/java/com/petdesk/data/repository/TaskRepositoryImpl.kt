package com.petdesk.data.repository

import com.petdesk.data.local.dao.TaskDao
import com.petdesk.data.local.entity.toDomainModel
import com.petdesk.data.local.entity.toEntity
import com.petdesk.domain.model.Task
import com.petdesk.domain.model.TaskStatus
import com.petdesk.domain.model.TaskType
import com.petdesk.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Task Repository Implementation
 */
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override fun getTasksByUserId(userId: Long): Flow<List<Task>> =
        taskDao.getTasksByUserId(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }

    override fun getTasksByStatus(userId: Long, status: TaskStatus): Flow<List<Task>> =
        taskDao.getTasksByStatus(userId, status.name).map { entities ->
            entities.map { it.toDomainModel() }
        }

    override fun getTasksByType(userId: Long, taskType: TaskType): Flow<List<Task>> =
        taskDao.getTasksByType(userId, taskType.name).map { entities ->
            entities.map { it.toDomainModel() }
        }

    override fun getScheduledTasks(userId: Long, time: Long): Flow<List<Task>> =
        taskDao.getScheduledTasks(userId, time).map { entities ->
            entities.map { it.toDomainModel() }
        }

    override fun getRecentTasks(userId: Long, limit: Int): Flow<List<Task>> =
        taskDao.getRecentTasks(userId, limit).map { entities ->
            entities.map { it.toDomainModel() }
        }

    override fun getPendingTasks(userId: Long): Flow<List<Task>> =
        taskDao.getPendingTasks(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }

    override fun searchTasks(userId: Long, keyword: String): Flow<List<Task>> =
        taskDao.searchTasks(userId, keyword).map { entities ->
            entities.map { it.toDomainModel() }
        }

    override suspend fun getTaskById(id: Long): Task? =
        taskDao.getTaskById(id)?.toDomainModel()

    override suspend fun insertTask(task: Task): Long =
        taskDao.insertTask(task.toEntity())

    override suspend fun updateTask(task: Task) =
        taskDao.updateTask(task.toEntity())

    override suspend fun deleteTask(task: Task) =
        taskDao.deleteTask(task.toEntity())

    override suspend fun deleteAllTasksByUserId(userId: Long) =
        taskDao.deleteAllTasksByUserId(userId)

    override suspend fun updateTaskStatus(id: Long, status: TaskStatus) =
        taskDao.updateTaskStatus(id, status.name, System.currentTimeMillis())

    override suspend fun startTask(id: Long) =
        taskDao.startTask(id, System.currentTimeMillis())

    override suspend fun completeTask(id: Long, result: String) =
        taskDao.completeTask(id, System.currentTimeMillis(), result)

    override suspend fun failTask(id: Long, errorMessage: String) =
        taskDao.failTask(id, System.currentTimeMillis(), errorMessage)

    override suspend fun cancelTask(id: Long) =
        taskDao.cancelTask(id, System.currentTimeMillis())

    override suspend fun getTaskCountByStatus(userId: Long, status: TaskStatus): Int =
        taskDao.getTaskCountByStatus(userId, status.name)

    override suspend fun getActiveTaskCount(userId: Long): Int =
        taskDao.getActiveTaskCount(userId)
}

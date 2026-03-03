package com.petdesk.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petdesk.domain.model.Task
import com.petdesk.domain.model.TaskPriority
import com.petdesk.domain.model.TaskStatus
import com.petdesk.domain.model.TaskType
import com.petdesk.domain.repository.TaskRepository
import com.petdesk.domain.usecase.TaskSchedulerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 定时任务状态
 */
data class ScheduledTaskState(
    val scheduledTasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 定时任务 ViewModel
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ScheduledTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val taskSchedulerUseCase: TaskSchedulerUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduledTaskState())
    val state: StateFlow<ScheduledTaskState> = _state.asStateFlow()

    private val _userId = MutableStateFlow(1L)

    init {
        loadScheduledTasks()
    }

    /**
     * 加载定时任务列表
     */
    private fun loadScheduledTasks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            _userId.flatMapLatest { userId ->
                taskRepository.getUpcomingScheduledTasks(userId)
            }.collect { tasks ->
                _state.update {
                    it.copy(
                        scheduledTasks = tasks.sortedBy { task -> task.scheduledAt },
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * 创建定时任务
     */
    fun createScheduledTask(
        title: String,
        description: String,
        scheduledAt: Long,
        taskType: TaskType = TaskType.REMINDER,
        priority: TaskPriority = TaskPriority.NORMAL
    ) {
        viewModelScope.launch {
            try {
                val task = Task(
                    userId = _userId.value,
                    title = title,
                    description = description,
                    type = taskType,
                    status = TaskStatus.PENDING,
                    priority = priority,
                    scheduledAt = scheduledAt,
                    createdAt = System.currentTimeMillis()
                )

                val taskId = taskRepository.insertTask(task)
                val insertedTask = task.copy(id = taskId)

                // 调度任务
                taskSchedulerUseCase.scheduleTask(insertedTask) { scheduledTask ->
                    executeScheduledTask(scheduledTask)
                }

            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * 执行定时任务
     */
    private suspend fun executeScheduledTask(task: Task) {
        try {
            taskRepository.startTask(task.id)
            // 这里可以调用 TaskExecutionViewModel 来执行实际任务
            // 完成后更新状态
            taskRepository.completeTask(task.id, "定时任务执行完成")
        } catch (e: Exception) {
            taskRepository.failTask(task.id, e.message ?: "执行失败")
        }
    }

    /**
     * 取消定时任务
     */
    fun cancelScheduledTask(taskId: Long) {
        viewModelScope.launch {
            try {
                taskSchedulerUseCase.cancelScheduledTask(taskId)
                taskRepository.cancelTask(taskId)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * 删除定时任务
     */
    fun deleteScheduledTask(taskId: Long) {
        viewModelScope.launch {
            try {
                taskSchedulerUseCase.cancelScheduledTask(taskId)
                val task = taskRepository.getTaskById(taskId)
                task?.let { taskRepository.deleteTask(it) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * 立即执行定时任务
     */
    fun executeNow(taskId: Long) {
        viewModelScope.launch {
            try {
                val task = taskRepository.getTaskById(taskId)
                task?.let {
                    taskSchedulerUseCase.cancelScheduledTask(taskId)
                    executeScheduledTask(it.copy(scheduledAt = System.currentTimeMillis()))
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        taskSchedulerUseCase.cancelAllScheduledTasks()
    }
}

package com.petdesk.domain.usecase

import com.petdesk.domain.model.Task
import com.petdesk.domain.model.TaskStatus
import com.petdesk.domain.repository.TaskRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

/**
 * 定时任务调度器
 * 负责管理定时任务的调度和执行
 */
class TaskSchedulerUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val scheduledJobs = mutableMapOf<Long, Job>()

    /**
     * 调度一个定时任务
     */
    fun scheduleTask(task: Task, onExecute: suspend (Task) -> Unit) {
        val delay = task.scheduledAt - System.currentTimeMillis()

        if (delay <= 0) {
            // 如果已经过了执行时间，立即执行
            scope.launch {
                onExecute(task)
            }
            return
        }

        // 取消之前同 ID 的任务（如果存在）
        scheduledJobs[task.id]?.cancel()

        // 调度新任务
        val job = scope.launch {
            delay(delay)
            // 检查任务是否仍然有效
            val currentTask = taskRepository.getTaskById(task.id)
            if (currentTask != null && currentTask.status == TaskStatus.PENDING) {
                onExecute(currentTask)
            }
        }

        scheduledJobs[task.id] = job
    }

    /**
     * 取消一个定时任务
     */
    fun cancelScheduledTask(taskId: Long) {
        scheduledJobs[taskId]?.cancel()
        scheduledJobs.remove(taskId)
    }

    /**
     * 取消所有定时任务
     */
    fun cancelAllScheduledTasks() {
        scheduledJobs.values.forEach { it.cancel() }
        scheduledJobs.clear()
    }

    /**
     * 检查任务是否已被调度
     */
    fun isTaskScheduled(taskId: Long): Boolean {
        return scheduledJobs[taskId]?.isActive == true
    }

    /**
     * 获取待执行的定时任务数量
     */
    fun getScheduledTaskCount(): Int = scheduledJobs.count { it.value.isActive }

    /**
     * 清理资源
     */
    fun dispose() {
        scope.cancel()
    }
}

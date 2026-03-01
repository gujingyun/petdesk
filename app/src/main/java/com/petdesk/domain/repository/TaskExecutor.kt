package com.petdesk.domain.repository

import com.petdesk.domain.model.Task
import com.petdesk.domain.model.agent.StepExecutionResult
import com.petdesk.domain.model.agent.TaskExecutionResult
import com.petdesk.domain.model.agent.TaskPlan
import kotlinx.coroutines.flow.Flow

/**
 * 任务执行器接口
 */
interface TaskExecutor {
    
    /**
     * 执行任务规划
     * @param plan 任务规划
     * @param task 关联的任务
     * @return 执行结果流
     */
    fun execute(plan: TaskPlan, task: Task): Flow<TaskExecutionResult>
    
    /**
     * 执行单个步骤
     */
    suspend fun executeStep(
        step: com.petdesk.domain.model.agent.TaskStep,
        context: Map<String, Any> = emptyMap()
    ): StepExecutionResult
    
    /**
     * 取消正在执行的任务
     */
    suspend fun cancel(taskId: Long)
    
    /**
     * 获取任务执行状态
     */
    suspend fun getExecutionStatus(taskId: Long): ExecutionStatus
}

/**
 * 任务执行状态
 */
sealed class ExecutionStatus {
    object Pending : ExecutionStatus()
    object Running : ExecutionStatus()
    object Paused : ExecutionStatus()
    data class Completed(val result: TaskExecutionResult) : ExecutionStatus()
    data class Failed(val error: String) : ExecutionStatus()
    object Cancelled : ExecutionStatus()
}

package com.petdesk.data.repository

import com.petdesk.domain.model.Task
import com.petdesk.domain.model.agent.StepExecutionResult
import com.petdesk.domain.model.agent.TaskExecutionResult
import com.petdesk.domain.model.agent.TaskPlan
import com.petdesk.domain.model.agent.TaskStep
import com.petdesk.domain.repository.ExecutionStatus
import com.petdesk.domain.repository.TaskExecutor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 任务执行器实现
 */
@Singleton
class TaskExecutorImpl @Inject constructor() : TaskExecutor {

    private val mutex = Mutex()
    private val runningTasks = mutableMapOf<Long, Boolean>()

    override fun execute(plan: TaskPlan, task: Task): Flow<TaskExecutionResult> = flow {
        mutex.withLock {
            runningTasks[task.id] = true
        }

        val stepResults = mutableListOf<StepExecutionResult>()
        val startTime = System.currentTimeMillis()
        var success = true
        var errorMessage = ""

        try {
            for (step in plan.steps) {
                if (runningTasks[task.id] != true) {
                    // 任务被取消
                    emit(
                        TaskExecutionResult(
                            plan = plan,
                            success = false,
                            stepResults = stepResults,
                            errorMessage = "任务已取消",
                            totalDuration = System.currentTimeMillis() - startTime
                        )
                    )
                    return@flow
                }

                // 检查依赖
                if (!checkDependencies(step, stepResults)) {
                    continue
                }

                val result = executeStep(step)
                stepResults.add(result)

                if (!result.success) {
                    success = false
                    errorMessage = result.errorMessage
                    break
                }
            }
        } catch (e: Exception) {
            success = false
            errorMessage = e.message ?: "未知错误"
        } finally {
            mutex.withLock {
                runningTasks.remove(task.id)
            }
        }

        emit(
            TaskExecutionResult(
                plan = plan,
                success = success,
                stepResults = stepResults,
                finalResult = if (success) "任务执行完成" else "",
                errorMessage = errorMessage,
                totalDuration = System.currentTimeMillis() - startTime
            )
        )
    }

    override suspend fun executeStep(
        step: TaskStep,
        context: Map<String, Any>
    ): StepExecutionResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            // 根据 action 执行不同的操作
            val result = when (step.action.lowercase()) {
                "execute" -> executeGeneric(step, context)
                "wait" -> executeWait(step, context)
                "notify" -> executeNotify(step, context)
                else -> executeGeneric(step, context)
            }

            StepExecutionResult(
                stepId = step.stepId,
                success = true,
                output = result,
                duration = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            StepExecutionResult(
                stepId = step.stepId,
                success = false,
                errorMessage = e.message ?: "执行失败",
                duration = System.currentTimeMillis() - startTime
            )
        }
    }

    override suspend fun cancel(taskId: Long) {
        mutex.withLock {
            runningTasks.remove(taskId)
        }
    }

    override suspend fun getExecutionStatus(taskId: Long): ExecutionStatus {
        return if (runningTasks[taskId] == true) {
            ExecutionStatus.Running
        } else {
            ExecutionStatus.Pending
        }
    }

    private fun checkDependencies(
        step: TaskStep, 
        stepResults: List<StepExecutionResult>
    ): Boolean {
        if (step.dependsOn.isEmpty()) return true
        
        return step.dependsOn.all { depId ->
            stepResults.any { it.stepId == depId && it.success }
        }
    }

    private suspend fun executeGeneric(
        step: TaskStep, 
        context: Map<String, Any>
    ): String {
        // 通用执行逻辑
        return "步骤 ${step.stepId} 执行完成：${step.description}"
    }

    private suspend fun executeWait(
        step: TaskStep, 
        context: Map<String, Any>
    ): String {
        val duration = step.parameters["duration"]?.toLongOrNull() ?: 1000L
        kotlinx.coroutines.delay(duration)
        return "等待 ${duration}ms 完成"
    }

    private suspend fun executeNotify(
        step: TaskStep, 
        context: Map<String, Any>
    ): String {
        val message = step.parameters["message"] ?: "通知"
        // 这里可以集成通知系统
        return "发送通知：$message"
    }
}

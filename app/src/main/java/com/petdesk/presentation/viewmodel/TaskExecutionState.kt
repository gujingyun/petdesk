package com.petdesk.presentation.viewmodel

import com.petdesk.domain.model.Task
import com.petdesk.domain.model.TaskStatus
import com.petdesk.domain.model.agent.StepExecutionResult
import com.petdesk.domain.model.agent.TaskPlan

/**
 * 任务执行状态
 */
sealed class TaskExecutionState {
    object Idle : TaskExecutionState()
    object Loading : TaskExecutionState()
    data class Planning(val message: String = "正在分析任务...") : TaskExecutionState()
    data class Planned(val plan: TaskPlan, val requiresConfirmation: Boolean = false) : TaskExecutionState()
    data class Executing(
        val currentStep: Int,
        val totalSteps: Int,
        val currentStepDescription: String,
        val stepResults: List<StepExecutionResult> = emptyList()
    ) : TaskExecutionState()
    data class Success(
        val task: Task,
        val finalResult: String,
        val stepResults: List<StepExecutionResult>
    ) : TaskExecutionState()
    data class Failure(
        val task: Task,
        val errorMessage: String,
        val failedStep: Int? = null,
        val stepResults: List<StepExecutionResult> = emptyList()
    ) : TaskExecutionState()
    data class Cancelled(
        val task: Task,
        val completedSteps: List<StepExecutionResult> = emptyList()
    ) : TaskExecutionState()
}

/**
 * 任务执行进度信息
 */
data class TaskExecutionProgress(
    val taskId: Long = 0,
    val taskTitle: String = "",
    val currentStep: Int = 0,
    val totalSteps: Int = 0,
    val currentStepDescription: String = "",
    val stepResults: List<StepExecutionResult> = emptyList(),
    val elapsedTime: Long = 0,
    val estimatedRemainingTime: Long = 0
) {
    val progressPercentage: Float
        get() = if (totalSteps > 0) currentStep.toFloat() / totalSteps else 0f

    val isComplete: Boolean
        get() = currentStep >= totalSteps
}

/**
 * 任务执行 UI 状态
 */
data class TaskExecutionUiState(
    val executionState: TaskExecutionState = TaskExecutionState.Idle,
    val progress: TaskExecutionProgress = TaskExecutionProgress(),
    val canRetry: Boolean = false,
    val canCancel: Boolean = false,
    val canConfirm: Boolean = false,
    val error: String? = null
)

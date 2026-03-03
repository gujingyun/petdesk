package com.petdesk.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petdesk.data.local.entity.UserEntity
import com.petdesk.domain.model.Task
import com.petdesk.domain.model.TaskPriority
import com.petdesk.domain.model.TaskStatus
import com.petdesk.domain.model.TaskType
import com.petdesk.domain.model.agent.IntentRecognitionResult
import com.petdesk.domain.model.agent.StepExecutionResult
import com.petdesk.domain.model.agent.TaskExecutionResult
import com.petdesk.domain.model.agent.TaskPlan
import com.petdesk.domain.repository.ExecutionStatus
import com.petdesk.domain.repository.IntentRecognizerRepository
import com.petdesk.domain.repository.TaskExecutor
import com.petdesk.domain.repository.TaskPlannerRepository
import com.petdesk.domain.repository.TaskRepository
import com.petdesk.domain.repository.UserRepository
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 任务执行  ViewModel
 *负责管理任务执行状态、进度和结果反馈
 */
@HiltViewModel
class TaskExecutionViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val taskExecutor: TaskExecutor,
    private val intentRecognizerRepository: IntentRecognizerRepository,
    private val taskPlannerRepository: TaskPlannerRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskExecutionUiState())
    val uiState: StateFlow<TaskExecutionUiState> = _uiState.asStateFlow()

    private val _currentTask = MutableStateFlow<Task?>(null)
    val currentTask: StateFlow<Task?> = _currentTask.asStateFlow()

    private var executionJob: Job? = null
    private var startTime: Long = 0

    /**
     * 开始执行任务（从用户输入开始）
     */
    fun startTaskExecution(userInput: String, userId: Long = 1L) {
        executionJob?.cancel()

        _uiState.value = TaskExecutionUiState(
            executionState = TaskExecutionState.Loading,
            canCancel = false
        )

        executionJob = viewModelScope.launch {
            try {
                // 步骤 1: 意图识别
                updateState(
                    executionState = TaskExecutionState.Planning("正在识别任务意图...")
                )

                val intent = intentRecognizerRepository.recognizeIntent(
                    userInput = userInput,
                    context = emptyList()
                )

                Log.d("TaskExecution", "Intent recognized: type=${intent.taskType}, confidence=${intent.confidence}")

                if (!isValidIntent(intent)) {
                    Log.w("TaskExecution", "Invalid intent: confidence=${intent.confidence}, type=${intent.taskType}")
                    handleFailure(null, "无法理解您的请求，请重新描述任务")
                    return@launch
                }

                // 步骤 2: 任务规划
                updateState(
                    executionState = TaskExecutionState.Planning("正在规划任务步骤...")
                )

                val plan = taskPlannerRepository.planTask(
                    userInput = userInput,
                    intent = intent,
                    context = emptyList()
                )

                Log.d("TaskExecution", "Task planned: steps=${plan.steps.size}, requiresConfirmation=${plan.requiresConfirmation}")

                if (plan.steps.isEmpty()) {
                    handleFailure(null, "无法生成任务步骤，请重新描述任务")
                    return@launch
                }

                // 创建任务
                val task = createTask(userId, userInput, intent)
                _currentTask.value = task

                // 检查是否需要用户确认
                if (plan.requiresConfirmation) {
                    updateState(
                        executionState = TaskExecutionState.Planned(
                            plan = plan,
                            requiresConfirmation = true
                        ),
                        canConfirm = true,
                        canCancel = true
                    )
                    return@launch
                }

                // 直接执行
                executePlan(plan, task)

            } catch (e: Exception) {
                handleFailure(null, e.message ?: "任务执行失败")
            }
        }
    }

    /**
     * 确认并执行已规划的任务
     */
    fun confirmExecution(plan: TaskPlan) {
        val task = _currentTask.value ?: return

        updateState(
            executionState = TaskExecutionState.Loading,
            canConfirm = false
        )

        executionJob = viewModelScope.launch {
            executePlan(plan, task)
        }
    }

    /**
     * 拒绝执行
     */
    fun rejectExecution() {
        executionJob?.cancel()

        _uiState.value = TaskExecutionUiState(
            executionState = TaskExecutionState.Idle
        )
        _currentTask.value = null
    }

    /**
     * 执行任务规划
     */
    private suspend fun executePlan(plan: TaskPlan, task: Task) {
        startTime = System.currentTimeMillis()

        // 更新任务状态为运行中
        taskRepository.startTask(task.id)

        updateState(
            executionState = TaskExecutionState.Executing(
                currentStep = 0,
                totalSteps = plan.steps.size,
                currentStepDescription = plan.steps.firstOrNull()?.description ?: "准备执行...",
                stepResults = emptyList()
            ),
            progress = TaskExecutionProgress(
                taskId = task.id,
                taskTitle = task.title,
                currentStep = 0,
                totalSteps = plan.steps.size,
                currentStepDescription = plan.steps.firstOrNull()?.description ?: "准备执行...",
                stepResults = emptyList()
            ),
            canCancel = true
        )

        try {
            // 使用 Flow 收集执行结果
            Log.d("TaskExecution", "Starting task execution with ${plan.steps.size} steps")
            taskExecutor.execute(plan, task).collect { result ->
                Log.d("TaskExecution", "Received result: success=${result.success}, stepResults=${result.stepResults.size}")
                handleExecutionResult(result, task)
            }
            Log.d("TaskExecution", "Flow collection completed")
        } catch (e: Exception) {
            Log.e("TaskExecution", "Execution error", e)
            handleFailure(task, e.message ?: "执行过程中发生错误")
        }
    }

    /**
     * 处理执行结果
     */
    private suspend fun handleExecutionResult(result: TaskExecutionResult, task: Task) {
        val currentStep = result.stepResults.size
        val totalSteps = result.plan.steps.size
        val currentStepDesc = result.plan.steps.getOrNull(currentStep - 1)?.description
            ?: if (result.success) "执行完成" else "执行失败"

        val elapsedTime = System.currentTimeMillis() - startTime
        val avgTimePerStep = if (currentStep > 0) elapsedTime / currentStep else 0
        val estimatedRemaining = if (currentStep < totalSteps) {
            avgTimePerStep * (totalSteps - currentStep)
        } else 0

        val progress = TaskExecutionProgress(
            taskId = task.id,
            taskTitle = task.title,
            currentStep = currentStep,
            totalSteps = totalSteps,
            currentStepDescription = currentStepDesc,
            stepResults = result.stepResults,
            elapsedTime = elapsedTime,
            estimatedRemainingTime = estimatedRemaining
        )

        if (result.success) {
            // 任务完成
            Log.d("TaskExecution", "Task completed successfully")
            taskRepository.completeTask(task.id, result.finalResult)

            updateState(
                executionState = TaskExecutionState.Success(
                    task = task.copy(
                        status = TaskStatus.COMPLETED,
                        result = result.finalResult,
                        completedAt = System.currentTimeMillis()
                    ),
                    finalResult = result.finalResult,
                    stepResults = result.stepResults
                ),
                progress = progress,
                canRetry = true,
                canCancel = false
            )
        } else {
            // 任务失败
            val failedStep = result.stepResults.indexOfFirst { !it.success }
            taskRepository.failTask(task.id, result.errorMessage)

            updateState(
                executionState = TaskExecutionState.Failure(
                    task = task.copy(
                        status = TaskStatus.FAILED,
                        errorMessage = result.errorMessage,
                        updatedAt = System.currentTimeMillis()
                    ),
                    errorMessage = result.errorMessage,
                    failedStep = if (failedStep >= 0) failedStep else null,
                    stepResults = result.stepResults
                ),
                progress = progress,
                canRetry = true,
                canCancel = false,
                error = result.errorMessage
            )
        }
    }

    /**
     * 取消正在执行的任务
     */
    fun cancelExecution() {
        executionJob?.cancel()

        val task = _currentTask.value ?: return

        viewModelScope.launch {
            try {
                taskExecutor.cancel(task.id)
                taskRepository.cancelTask(task.id)

                val completedSteps = (_uiState.value.executionState as? TaskExecutionState.Executing)?.stepResults
                    ?: emptyList()

                updateState(
                    executionState = TaskExecutionState.Cancelled(
                        task = task.copy(
                            status = TaskStatus.CANCELLED,
                            updatedAt = System.currentTimeMillis()
                        ),
                        completedSteps = completedSteps
                    ),
                    canCancel = false,
                    canRetry = true
                )
            } catch (e: Exception) {
                updateState(
                    error = "取消任务失败: ${e.message}"
                )
            }
        }
    }

    /**
     * 重试任务执行
     */
    fun retry() {
        val currentState = _uiState.value
        val task = _currentTask.value

        if (task != null && currentState.executionState is TaskExecutionState.Failure) {
            // 从失败状态重试
            viewModelScope.launch {
                // 重置任务状态
                taskRepository.updateTaskStatus(task.id, TaskStatus.PENDING)

                // 重新执行
                startTaskExecution(task.title, task.userId)
            }
        } else if (currentState.executionState is TaskExecutionState.Planned) {
            // 重新确认执行
            val plan = (currentState.executionState as TaskExecutionState.Planned).plan
            confirmExecution(plan)
        }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * 重置状态
     */
    fun reset() {
        executionJob?.cancel()
        _uiState.value = TaskExecutionUiState()
        _currentTask.value = null
    }

    /**
     * 创建任务
     */
    private suspend fun createTask(
        userId: Long,
        userInput: String,
        intent: IntentRecognitionResult
    ): Task {
        // 确保用户存在，如果不存在则创建默认用户
        var currentUserId = userId
        if (userRepository.getCurrentUser() == null) {
            val defaultUser = UserEntity(
                username = "default_user",
                nickname = "默认用户"
            )
            currentUserId = userRepository.insertUser(defaultUser)
            Log.d("TaskExecution", "Created default user with id: $currentUserId")
        }

        val task = Task(
            userId = currentUserId,
            title = userInput.take(50),
            description = userInput,
            type = intent.taskType,
            status = TaskStatus.PENDING,
            priority = TaskPriority.NORMAL,
            inputData = intent.parameters.toString(),
            createdAt = System.currentTimeMillis()
        )

        val id = taskRepository.insertTask(task)
        return task.copy(id = id)
    }

    /**
     * 验证意图识别结果
     */
    private fun isValidIntent(intent: IntentRecognitionResult): Boolean {
        // confidence >= 0.3 表示 API 成功解析了请求
        // 允许 CUSTOM 类型任务执行（作为通用任务）
        return intent.confidence >= 0.3f
    }

    /**
     * 处理失败
     */
    private fun handleFailure(task: Task?, errorMessage: String) {
        task?.let {
            viewModelScope.launch {
                taskRepository.failTask(it.id, errorMessage)
            }
        }

        updateState(
            executionState = TaskExecutionState.Failure(
                task = task ?: Task(
                    userId = 1L,
                    title = "未知任务",
                    description = errorMessage,
                    status = TaskStatus.FAILED,
                    errorMessage = errorMessage
                ),
                errorMessage = errorMessage
            ),
            canRetry = true,
            canCancel = false,
            error = errorMessage
        )
    }

    /**
     * 更新状态
     */
    private fun updateState(
        executionState: TaskExecutionState? = null,
        progress: TaskExecutionProgress? = null,
        canRetry: Boolean? = null,
        canCancel: Boolean? = null,
        canConfirm: Boolean? = null,
        error: String? = null
    ) {
        _uiState.value = _uiState.value.copy(
            executionState = executionState ?: _uiState.value.executionState,
            progress = progress ?: _uiState.value.progress,
            canRetry = canRetry ?: _uiState.value.canRetry,
            canCancel = canCancel ?: _uiState.value.canCancel,
            canConfirm = canConfirm ?: _uiState.value.canConfirm,
            error = error
        )
    }

    override fun onCleared() {
        super.onCleared()
        executionJob?.cancel()
    }
}

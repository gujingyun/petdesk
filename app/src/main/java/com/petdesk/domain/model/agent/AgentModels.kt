package com.petdesk.domain.model.agent

import com.petdesk.domain.model.TaskPriority
import com.petdesk.domain.model.TaskType

/**
 * 意图识别结果
 */
data class IntentRecognitionResult(
    val taskType: TaskType,           // 识别的任务类型
    val confidence: Float,             // 置信度 0-1
    val parameters: Map<String, String>, // 提取的参数
    val rawResponse: String = ""      // 原始 LLM 响应
)

/**
 * 任务步骤
 */
data class TaskStep(
    val stepId: Int,                  // 步骤序号
    val description: String,          // 步骤描述
    val action: String,               // 执行动作
    val parameters: Map<String, String> = emptyMap(), // 步骤参数
    val dependsOn: List<Int> = emptyList(), // 依赖的前置步骤
    val estimatedDuration: Long = 0   // 预估执行时间（毫秒）
)

/**
 * 任务规划结果
 */
data class TaskPlan(
    val userInput: String,            // 用户原始输入
    val intent: IntentRecognitionResult, // 意图识别结果
    val steps: List<TaskStep>,        // 任务步骤列表
    val estimatedTotalTime: Long,     // 预估总执行时间
    val requiresConfirmation: Boolean = false // 是否需要用户确认
)

/**
 * 步骤执行结果
 */
data class StepExecutionResult(
    val stepId: Int,
    val success: Boolean,
    val output: String = "",
    val errorMessage: String = "",
    val duration: Long = 0
)

/**
 * 任务执行结果
 */
data class TaskExecutionResult(
    val plan: TaskPlan,
    val success: Boolean,
    val stepResults: List<StepExecutionResult> = emptyList(),
    val finalResult: String = "",
    val errorMessage: String = "",
    val totalDuration: Long = 0
)

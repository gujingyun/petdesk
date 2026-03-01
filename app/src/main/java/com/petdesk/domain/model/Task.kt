package com.petdesk.domain.model

/**
 * 任务状态枚举
 */
enum class TaskStatus {
    PENDING,    // 待执行
    RUNNING,    // 执行中
    COMPLETED,  // 已完成
    FAILED,     // 失败
    CANCELLED   // 已取消
}

/**
 * 任务类型枚举
 */
enum class TaskType {
    CHAT,            // 聊天任务
    REMINDER,        // 提醒任务
    APP_CONTROL,    // 应用控制任务
    FILE_OPERATION,  // 文件操作任务
    CUSTOM           // 自定义任务
}

/**
 * 任务优先级枚举
 */
enum class TaskPriority(val value: Int) {
    LOW(1),
    NORMAL(2),
    HIGH(3),
    URGENT(4),
    CRITICAL(5)
}

/**
 * Task 领域模型
 * 表示一个 Agent 任务
 */
data class Task(
    val id: Long = 0,
    val userId: Long,
    val title: String,
    val description: String = "",
    val type: TaskType = TaskType.CUSTOM,
    val status: TaskStatus = TaskStatus.PENDING,
    val priority: TaskPriority = TaskPriority.NORMAL,
    val targetApp: String = "",          // 目标应用包名
    val targetFolder: String = "",      // 目标文件夹
    val inputData: String = "",         // 输入数据 (JSON格式)
    val result: String = "",            // 执行结果
    val errorMessage: String = "",       // 错误信息
    val scheduledAt: Long = 0,           // 计划执行时间
    val startedAt: Long = 0,            // 开始执行时间
    val completedAt: Long = 0,          // 完成时间
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

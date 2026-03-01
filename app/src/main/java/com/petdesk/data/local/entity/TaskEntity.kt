package com.petdesk.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.petdesk.domain.model.TaskPriority
import com.petdesk.domain.model.TaskStatus
import com.petdesk.domain.model.TaskType

/**
 * 任务记录表 Entity
 * 存储 Agent 任务记录
 */
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val title: String,                    // 任务标题
    val description: String = "",         // 任务描述
    val taskType: String = "CUSTOM",       // 任务类型: CHAT, REMINDER, APP_CONTROL, FILE_OPERATION, CUSTOM
    val status: String = "PENDING",        // 任务状态: PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
    val priority: Int = 2,                // 优先级: 1-5 (对应 TaskPriority)
    val targetApp: String = "",            // 目标应用包名
    val targetFolder: String = "",         // 目标文件夹
    val inputData: String = "",            // 输入数据 (JSON格式)
    val result: String = "",               // 执行结果
    val errorMessage: String = "",         // 错误信息
    val scheduledAt: Long = 0,             // 计划执行时间
    val startedAt: Long = 0,              // 开始执行时间
    val completedAt: Long = 0,             // 完成时间
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toTaskStatus(): TaskStatus = TaskStatus.valueOf(status)
    fun toTaskType(): TaskType = TaskType.valueOf(taskType)
    fun toTaskPriority(): TaskPriority = TaskPriority.entries.find { it.value == priority } ?: TaskPriority.NORMAL
}

/**
 * Extension function to convert TaskEntity to Task domain model
 */
fun TaskEntity.toDomainModel() = com.petdesk.domain.model.Task(
    id = id,
    userId = userId,
    title = title,
    description = description,
    type = toTaskType(),
    status = toTaskStatus(),
    priority = toTaskPriority(),
    targetApp = targetApp,
    targetFolder = targetFolder,
    inputData = inputData,
    result = result,
    errorMessage = errorMessage,
    scheduledAt = scheduledAt,
    startedAt = startedAt,
    completedAt = completedAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Extension function to convert Task domain model to TaskEntity
 */
fun com.petdesk.domain.model.Task.toEntity() = TaskEntity(
    id = id,
    userId = userId,
    title = title,
    description = description,
    taskType = type.name,
    status = status.name,
    priority = priority.value,
    targetApp = targetApp,
    targetFolder = targetFolder,
    inputData = inputData,
    result = result,
    errorMessage = errorMessage,
    scheduledAt = scheduledAt,
    startedAt = startedAt,
    completedAt = completedAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

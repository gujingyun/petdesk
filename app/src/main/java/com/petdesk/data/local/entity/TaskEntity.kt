package com.petdesk.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 任务记录表 Entity
 * 存储桌面整理任务记录
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
    val title: String, // 任务标题
    val description: String = "", // 任务描述
    val taskType: String = "organize", // 任务类型: organize, cleanup, notify
    val targetApp: String = "", // 目标应用包名
    val targetFolder: String = "", // 目标文件夹
    val status: Int = 0, // 0: 待执行, 1: 执行中, 2: 已完成, 3: 失败
    val priority: Int = 1, // 优先级: 1-5
    val scheduledAt: Long = 0, // 计划执行时间
    val startedAt: Long = 0, // 开始执行时间
    val completedAt: Long = 0, // 完成时间
    val result: String = "", // 执行结果
    val errorMessage: String = "", // 错误信息
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

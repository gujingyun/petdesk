package com.petdesk.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 对话记录表 Entity
 * 存储用户与桌宠的对话历史
 */
@Entity(
    tableName = "conversations",
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
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val message: String, // 用户消息
    val response: String = "", // 桌宠回复
    val messageType: Int = 0, // 0: 用户, 1: 桌宠
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false, // 是否收藏
    val tags: String = "" // 标签，用逗号分隔
)

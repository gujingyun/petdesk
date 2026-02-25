package com.petdesk.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 记忆表 Entity
 * 存储桌宠对用户的记忆信息
 */
@Entity(
    tableName = "memories",
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
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val key: String, // 记忆键，如 "user_name", "user_preference_xxx"
    val value: String, // 记忆值
    val category: String = "general", // 记忆分类: general, preference, habit, relationship
    val importance: Int = 1, // 重要程度: 1-5
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = System.currentTimeMillis(),
    val accessCount: Int = 0
)

package com.petdesk.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 用户配置表 Entity
 * 存储用户个性化配置
 */
@Entity(
    tableName = "user_preferences",
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
data class UserPreferencesEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val key: String, // 配置键
    val value: String, // 配置值
    val type: String = "string", // 类型: string, int, float, boolean, json
    val category: String = "general", // 分类: general, appearance, behavior, notification
    val isSynced: Boolean = false, // 是否已同步到云端
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

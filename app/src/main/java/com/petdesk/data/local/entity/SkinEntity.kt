package com.petdesk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 皮肤表 Entity
 * 存储桌宠皮肤/外观配置
 */
@Entity(tableName = "skins")
data class SkinEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // 皮肤名称
    val description: String = "", // 皮肤描述
    val previewUrl: String = "", // 预览图URL
    val modelUrl: String = "", // 模型文件URL (Lottie/JSON)
    val type: String = "default", // 皮肤类型: default, animated, static
    val category: String = "default", // 分类: default, holiday, special, custom
    val isDefault: Boolean = false, // 是否为默认皮肤
    val isUnlocked: Boolean = true, // 是否已解锁
    val price: Int = 0, // 价格（如果需要购买）
    val author: String = "", // 作者
    val version: String = "1.0",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

package com.petdesk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 技能表 Entity
 * 存储桌宠技能配置
 */
@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // 技能名称
    val description: String = "", // 技能描述
    val icon: String = "", // 技能图标
    val trigger: String = "", // 触发关键词，多个用逗号分隔
    val actionType: String = "chat", // 动作类型: chat, animation, function
    val actionData: String = "", // 动作数据 (JSON格式)
    val cooldown: Long = 0, // 冷却时间（毫秒）
    val isEnabled: Boolean = true, // 是否启用
    val isSystem: Boolean = true, // 是否为系统技能
    val level: Int = 1, // 技能等级
    val experience: Int = 0, // 经验值
    val category: String = "general", // 分类: general, entertainment, productivity
    val usageCount: Int = 0, // 使用次数
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

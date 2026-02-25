package com.petdesk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户表 Entity
 * 存储用户基本信息
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val nickname: String = "",
    val avatar: String = "", // 头像URL
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = 0,
    val status: Int = 1 // 1: 正常, 0: 禁用
)

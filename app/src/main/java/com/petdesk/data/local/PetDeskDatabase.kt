package com.petdesk.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.petdesk.data.local.dao.*
import com.petdesk.data.local.entity.*

/**
 * PetDesk 数据库
 * 包含用户表、对话表、记忆表、皮肤表、技能表、任务表、用户配置表
 */
@Database(
    entities = [
        UserEntity::class,
        ConversationEntity::class,
        MemoryEntity::class,
        SkinEntity::class,
        SkillEntity::class,
        TaskEntity::class,
        UserPreferencesEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PetDeskDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun conversationDao(): ConversationDao
    abstract fun memoryDao(): MemoryDao
    abstract fun skinDao(): SkinDao
    abstract fun skillDao(): SkillDao
    abstract fun taskDao(): TaskDao
    abstract fun userPreferencesDao(): UserPreferencesDao

    companion object {
        const val DATABASE_NAME = "petdesk_database"
    }
}

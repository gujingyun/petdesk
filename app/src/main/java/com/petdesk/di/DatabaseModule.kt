package com.petdesk.di

import android.content.Context
import androidx.room.Room
import com.petdesk.data.local.PetDeskDatabase
import com.petdesk.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger module for providing database dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PetDeskDatabase {
        return Room.databaseBuilder(
            context,
            PetDeskDatabase::class.java,
            PetDeskDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: PetDeskDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideConversationDao(database: PetDeskDatabase): ConversationDao = database.conversationDao()

    @Provides
    @Singleton
    fun provideMemoryDao(database: PetDeskDatabase): MemoryDao = database.memoryDao()

    @Provides
    @Singleton
    fun provideSkinDao(database: PetDeskDatabase): SkinDao = database.skinDao()

    @Provides
    @Singleton
    fun provideSkillDao(database: PetDeskDatabase): SkillDao = database.skillDao()

    @Provides
    @Singleton
    fun provideTaskDao(database: PetDeskDatabase): TaskDao = database.taskDao()

    @Provides
    @Singleton
    fun provideUserPreferencesDao(database: PetDeskDatabase): UserPreferencesDao = database.userPreferencesDao()
}

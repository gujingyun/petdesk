package com.petdesk.di

import com.petdesk.data.repository.*
import com.petdesk.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Dagger module for binding repository interfaces to their implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindPetRepository(petRepositoryImpl: PetRepositoryImpl): PetRepository

    @Binds
    abstract fun bindPermissionRepository(permissionRepositoryImpl: PermissionRepositoryImpl): PermissionRepository

    @Binds
    abstract fun bindUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindConversationRepository(conversationRepositoryImpl: ConversationRepositoryImpl): ConversationRepository

    @Binds
    abstract fun bindMemoryRepository(memoryRepositoryImpl: MemoryRepositoryImpl): MemoryRepository

    @Binds
    abstract fun bindSkinRepository(skinRepositoryImpl: SkinRepositoryImpl): SkinRepository

    @Binds
    abstract fun bindSkillRepository(skillRepositoryImpl: SkillRepositoryImpl): SkillRepository

    @Binds
    abstract fun bindTaskRepository(taskRepositoryImpl: TaskRepositoryImpl): TaskRepository

    @Binds
    abstract fun bindUserPreferencesRepository(userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}

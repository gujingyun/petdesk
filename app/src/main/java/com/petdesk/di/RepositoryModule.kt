package com.petdesk.di

import com.petdesk.data.repository.ConversationRepositoryImpl
import com.petdesk.data.repository.MemoryRepositoryImpl
import com.petdesk.data.repository.PermissionRepositoryImpl
import com.petdesk.data.repository.PetRepositoryImpl
import com.petdesk.data.repository.TaskExecutorImpl
import com.petdesk.data.repository.TaskRepositoryImpl
import com.petdesk.data.repository.UserRepositoryImpl
import com.petdesk.domain.repository.ConversationRepository
import com.petdesk.domain.repository.MemoryRepository
import com.petdesk.domain.repository.PermissionRepository
import com.petdesk.domain.repository.PetRepository
import com.petdesk.domain.repository.TaskExecutor
import com.petdesk.domain.repository.TaskRepository
import com.petdesk.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindConversationRepository(
        impl: ConversationRepositoryImpl
    ): ConversationRepository

    @Binds
    @Singleton
    abstract fun bindMemoryRepository(
        impl: MemoryRepositoryImpl
    ): MemoryRepository

    @Binds
    @Singleton
    abstract fun bindPetRepository(
        impl: PetRepositoryImpl
    ): PetRepository

    @Binds
    @Singleton
    abstract fun bindPermissionRepository(
        impl: PermissionRepositoryImpl
    ): PermissionRepository

    @Binds
    @Singleton
    abstract fun bindTaskExecutor(
        impl: TaskExecutorImpl
    ): TaskExecutor

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        impl: TaskRepositoryImpl
    ): TaskRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository
}

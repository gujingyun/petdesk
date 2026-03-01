package com.petdesk.di

import com.petdesk.data.repository.TaskExecutorImpl
import com.petdesk.domain.repository.TaskExecutor
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
    abstract fun bindTaskExecutor(
        impl: TaskExecutorImpl
    ): TaskExecutor
}

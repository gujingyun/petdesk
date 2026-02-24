package com.petdesk.di

import com.petdesk.data.repository.PetRepositoryImpl
import com.petdesk.domain.repository.PetRepository
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
}
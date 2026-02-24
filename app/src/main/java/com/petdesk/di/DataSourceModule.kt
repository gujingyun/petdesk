package com.petdesk.di

import android.content.Context
import com.petdesk.data.local.PetPreferencesDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

/**
 * Dagger module for providing data sources.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    
    @Provides
    fun providePetPreferencesDataSource(@ApplicationContext context: Context): PetPreferencesDataSource {
        return PetPreferencesDataSource(context)
    }
}
package com.petdesk.di

import android.content.Context
import com.petdesk.presentation.floating.FloatingWindowManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

/**
 * Dagger module for providing service-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    
    @Provides
    fun provideFloatingWindowManager(@ApplicationContext context: Context): FloatingWindowManager {
        return FloatingWindowManager(context)
    }
}
package com.petdesk

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main Application class for PetDesk.
 * Uses Hilt for dependency injection.
 */
@HiltAndroidApp
class PetDeskApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any application-wide components here
    }
}
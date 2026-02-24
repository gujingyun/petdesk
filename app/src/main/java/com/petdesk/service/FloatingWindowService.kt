package com.petdesk.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Service for managing the floating window (desktop pet).
 */
class FloatingWindowService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Initialize floating window here
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clean up floating window resources
    }
}
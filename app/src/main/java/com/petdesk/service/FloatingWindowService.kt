package com.petdesk.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.petdesk.R
import com.petdesk.presentation.floating.FloatingWindowManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Service for managing the floating window (desktop pet).
 */
@AndroidEntryPoint
class FloatingWindowService : Service() {
    
    @Inject
    lateinit var floatingWindowManager: FloatingWindowManager
    
    companion object {
        private const val CHANNEL_ID = "floating_window_service"
        private const val NOTIFICATION_ID = 1
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Show the floating window when service starts
        if (!floatingWindowManager.isShowing()) {
            floatingWindowManager.showFloatingWindow()
        }
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Hide the floating window when service is destroyed
        floatingWindowManager.hideFloatingWindow()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows the desktop pet"
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Desktop pet is running")
            .setSmallIcon(R.drawable.ic_pet_default)
            .build()
    }
}
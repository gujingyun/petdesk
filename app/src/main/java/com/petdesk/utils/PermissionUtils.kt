package com.petdesk.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Utility class for handling permissions.
 */
object PermissionUtils {
    
    /**
     * Checks if the app has the SYSTEM_ALERT_WINDOW permission.
     */
    fun hasFloatingWindowPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true // Permission not required before Android 6.0
        }
    }
    
    /**
     * Creates an intent to request the SYSTEM_ALERT_WINDOW permission.
     */
    fun createFloatingWindowPermissionIntent(context: Context): Intent {
        return Intent(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION
            } else {
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            },
            Uri.parse("package:${context.packageName}")
        )
    }
    
    /**
     * Checks if the app has the specified permission.
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == 
            PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Checks if the app has all the specified permissions.
     */
    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { hasPermission(context, it) }
    }
}
package com.petdesk.presentation.floating

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import com.petdesk.R

/**
 * Manager class for handling the floating window (desktop pet).
 */
class FloatingWindowManager(private val context: Context) {
    private var windowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var floatingView: ComposeView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    
    // Track the size of the floating view
    private var floatingViewSize = IntSize.Zero
    
    /**
     * Shows the floating window.
     */
    fun showFloatingWindow() {
        if (floatingView != null) return
        
        // Create the ComposeView
        val composeView = ComposeView(context).apply {
            setContent {
                FloatingPetView(
                    onSizeChanged = { size ->
                        floatingViewSize = size
                        updateLayoutParams()
                    }
                )
            }
        }
        
        // Create layout parameters
        val params = WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.TRANSLUCENT
            gravity = Gravity.TOP or Gravity.START
            x = 100 // Default X position
            y = 100 // Default Y position
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        }
        
        // Add touch listener for dragging
        composeView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f
            
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(composeView, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        // Check if it should snap to edges
                        snapToEdge(params)
                        windowManager.updateViewLayout(composeView, params)
                        return true
                    }
                }
                return false
            }
        })
        
        // Add the view to the window manager
        windowManager.addView(composeView, params)
        floatingView = composeView
        layoutParams = params
    }
    
    /**
     * Hides the floating window.
     */
    fun hideFloatingWindow() {
        floatingView?.let { view ->
            windowManager.removeView(view)
            floatingView = null
            layoutParams = null
        }
    }
    
    /**
     * Updates the layout parameters based on the current view size.
     */
    private fun updateLayoutParams() {
        layoutParams?.let { params ->
            floatingView?.let { view ->
                windowManager.updateViewLayout(view, params)
            }
        }
    }
    
    /**
     * Snaps the window to the nearest edge if it's close enough.
     */
    private fun snapToEdge(params: WindowManager.LayoutParams) {
        val displayWidth = windowManager.defaultDisplay.width
        val displayHeight = windowManager.defaultDisplay.height
        
        // Define snap threshold (10% of screen dimension)
        val snapThresholdX = (displayWidth * 0.1).toInt()
        val snapThresholdY = (displayHeight * 0.1).toInt()
        
        // Snap to left or right edge
        if (params.x < snapThresholdX) {
            params.x = 0
        } else if (params.x > displayWidth - floatingViewSize.width - snapThresholdX) {
            params.x = displayWidth - floatingViewSize.width
        }
        
        // Snap to top or bottom edge
        if (params.y < snapThresholdY) {
            params.y = 0
        } else if (params.y > displayHeight - floatingViewSize.height - snapThresholdY) {
            params.y = displayHeight - floatingViewSize.height
        }
    }
    
    /**
     * Updates the transparency of the floating window.
     */
    fun updateTransparency(alpha: Float) {
        layoutParams?.let { params ->
            params.alpha = alpha
            floatingView?.let { view ->
                windowManager.updateViewLayout(view, params)
            }
        }
    }
    
    /**
     * Checks if the floating window is currently showing.
     */
    fun isShowing(): Boolean = floatingView != null
}
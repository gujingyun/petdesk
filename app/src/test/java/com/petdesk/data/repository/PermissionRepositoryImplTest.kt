package com.petdesk.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.petdesk.domain.model.PermissionItem
import com.petdesk.domain.model.PermissionState
import com.petdesk.domain.model.PermissionStatus
import com.petdesk.domain.model.PermissionType
import com.petdesk.domain.repository.Manufacturer
import com.petdesk.service.DesktopAccessibilityService
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for PermissionRepositoryImpl.
 * Tests the W3 - Permission Management Module functionality.
 */
class PermissionRepositoryImplTest {

    private lateinit var context: Context
    private lateinit var repository: PermissionRepositoryImpl

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        mockkObject(ContextCompat)
        mockkObject(Settings.Secure)
        mockkObject(Settings.canDrawOverlays)
        mockkObject(Settings.canManageExternalStorage)

        every { context.packageName } returns "com.petdesk"

        repository = PermissionRepositoryImpl(context)
    }

    /**
     * Test: Check permission status for SYSTEM_ALERT_WINDOW - granted
     */
    @Test
    fun `checkPermissionStatus returns GRANTED for overlay permission when allowed`() {
        // Given
        every { Settings.canDrawOverlays(any()) } returns true

        // When
        val result = repository.checkPermissionStatus(PermissionType.SYSTEM_ALERT_WINDOW)

        // Then
        assertEquals(PermissionType.SYSTEM_ALERT_WINDOW, result.type)
        assertEquals(PermissionStatus.GRANTED, result.status)
        assertTrue(result.isGranted)
    }

    /**
     * Test: Check permission status for SYSTEM_ALERT_WINDOW - denied
     */
    @Test
    fun `checkPermissionStatus returns DENIED for overlay permission when not allowed`() {
        // Given
        every { Settings.canDrawOverlays(any()) } returns false

        // When
        val result = repository.checkPermissionStatus(PermissionType.SYSTEM_ALERT_WINDOW)

        // Then
        assertEquals(PermissionStatus.DENIED, result.status)
        assertFalse(result.isGranted)
    }

    /**
     * Test: Check permission status for RECORD_AUDIO - granted
     */
    @Test
    fun `checkPermissionStatus returns GRANTED for RECORD_AUDIO when permitted`() {
        // Given
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_GRANTED

        // When
        val result = repository.checkPermissionStatus(PermissionType.RECORD_AUDIO)

        // Then
        assertEquals(PermissionStatus.GRANTED, result.status)
        assertTrue(result.isGranted)
    }

    /**
     * Test: Check permission status for RECORD_AUDIO - denied
     */
    @Test
    fun `checkPermissionStatus returns DENIED for RECORD_AUDIO when not permitted`() {
        // Given
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_DENIED

        // When
        val result = repository.checkPermissionStatus(PermissionType.RECORD_AUDIO)

        // Then
        assertEquals(PermissionStatus.DENIED, result.status)
        assertFalse(result.isGranted)
    }

    /**
     * Test: Check permission status for QUERY_ALL_PACKAGES - granted
     */
    @Test
    fun `checkPermissionStatus returns GRANTED for QUERY_ALL_PACKAGES when permitted`() {
        // Given
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_GRANTED

        // When
        val result = repository.checkPermissionStatus(PermissionType.QUERY_ALL_PACKAGES)

        // Then
        assertEquals(PermissionStatus.GRANTED, result.status)
        assertTrue(result.isGranted)
    }

    /**
     * Test: Check all permissions state
     */
    @Test
    fun `checkAllPermissionsState returns all permissions with correct count`() {
        // Given
        every { Settings.canDrawOverlays(any()) } returns true
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_GRANTED

        // When
        val result = repository.checkAllPermissionsState()

        // Then
        assertNotNull(result)
        assertEquals(PermissionType.entries.size, result.permissions.size)
    }

    /**
     * Test: Check all permissions state - all required granted
     */
    @Test
    fun `checkAllPermissionsState returns allRequiredGranted true when all required permissions granted`() {
        // Given
        every { Settings.canDrawOverlays(any()) } returns true
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_GRANTED

        // When
        val result = repository.checkAllPermissionsState()

        // Then
        assertTrue(result.allRequiredGranted)
    }

    /**
     * Test: Check all permissions state - all granted
     */
    @Test
    fun `checkAllPermissionsState returns allGranted true when all permissions granted`() {
        // Given
        every { Settings.canDrawOverlays(any()) } returns true
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_GRANTED
        every { Settings.Secure.getString(any(), any()) } returns "com.petdesk/${DesktopAccessibilityService::class.java.name}"

        // When
        val result = repository.checkAllPermissionsState()

        // Then
        assertTrue(result.allGranted)
    }

    /**
     * Test: Create overlay permission intent
     */
    @Test
    fun `createOverlayPermissionIntent creates correct intent`() {
        // When
        val intent = repository.createOverlayPermissionIntent(context)

        // Then
        assertNotNull(intent)
        assertEquals(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, intent.action)
    }

    /**
     * Test: Create usage stats permission intent
     */
    @Test
    fun `createUsageStatsPermissionIntent creates correct intent`() {
        // When
        val intent = repository.createUsageStatsPermissionIntent(context)

        // Then
        assertNotNull(intent)
        assertEquals(Settings.ACTION_USAGE_ACCESS_SETTINGS, intent.action)
    }

    /**
     * Test: Create accessibility service permission intent
     */
    @Test
    fun `createAccessibilityServicePermissionIntent creates correct intent`() {
        // When
        val intent = repository.createAccessibilityServicePermissionIntent(context)

        // Then
        assertNotNull(intent)
        assertEquals(Settings.ACTION_ACCESSIBILITY_SETTINGS, intent.action)
    }

    /**
     * Test: Request runtime permissions filters out special permissions
     */
    @Test
    fun `requestRuntimePermissions returns runtime permissions only`() {
        // Given
        val allPermissions = listOf(
            PermissionType.SYSTEM_ALERT_WINDOW,
            PermissionType.FOREGROUND_SERVICE,
            PermissionType.POST_NOTIFICATIONS,
            PermissionType.STORAGE,
            PermissionType.RECORD_AUDIO
        )

        // When
        val result = repository.requestRuntimePermissions(allPermissions)

        // Then
        // Special permissions should be filtered out
        assertFalse(result.contains("android.permission.SYSTEM_ALERT_WINDOW"))
        // Runtime permissions should be included
        assertTrue(result.any { it.contains("FOREGROUND_SERVICE") })
    }

    /**
     * Test: Get permissions needing manual grant
     */
    @Test
    fun `getPermissionsNeedingManualGrant returns special permissions`() {
        // When
        val result = repository.getPermissionsNeedingManualGrant()

        // Then
        assertTrue(result.contains(PermissionType.SYSTEM_ALERT_WINDOW))
        assertTrue(result.contains(PermissionType.PACKAGE_USAGE_STATS))
        assertTrue(result.contains(PermissionType.BIND_ACCESSIBILITY_SERVICE))
    }

    /**
     * Test: isSpecialPermission returns true for special permissions
     */
    @Test
    fun `isSpecialPermission returns true for special permissions`() {
        // Then
        assertTrue(repository.isSpecialPermission(PermissionType.SYSTEM_ALERT_WINDOW))
        assertTrue(repository.isSpecialPermission(PermissionType.PACKAGE_USAGE_STATS))
        assertTrue(repository.isSpecialPermission(PermissionType.BIND_ACCESSIBILITY_SERVICE))
    }

    /**
     * Test: isSpecialPermission returns false for runtime permissions
     */
    @Test
    fun `isSpecialPermission returns false for runtime permissions`() {
        // Then
        assertFalse(repository.isSpecialPermission(PermissionType.FOREGROUND_SERVICE))
        assertFalse(repository.isSpecialPermission(PermissionType.POST_NOTIFICATIONS))
        assertFalse(repository.isSpecialPermission(PermissionType.STORAGE))
        assertFalse(repository.isSpecialPermission(PermissionType.RECORD_AUDIO))
        assertFalse(repository.isSpecialPermission(PermissionType.QUERY_ALL_PACKAGES))
    }

    /**
     * Test: Check accessibility service permission - enabled
     */
    @Test
    fun `checkPermissionStatus returns GRANTED for accessibility when enabled`() {
        // Given
        val enabledServices = "com.petdesk/${DesktopAccessibilityService::class.java.name}"
        every { Settings.Secure.getString(any(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) } returns enabledServices

        // When
        val result = repository.checkPermissionStatus(PermissionType.BIND_ACCESSIBILITY_SERVICE)

        // Then
        assertEquals(PermissionStatus.GRANTED, result.status)
    }

    /**
     * Test: Check accessibility service permission - disabled
     */
    @Test
    fun `checkPermissionStatus returns DENIED for accessibility when disabled`() {
        // Given
        every { Settings.Secure.getString(any(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) } returns null

        // When
        val result = repository.checkPermissionStatus(PermissionType.BIND_ACCESSIBILITY_SERVICE)

        // Then
        assertEquals(PermissionStatus.DENIED, result.status)
    }

    /**
     * Test: Permission item has correct type and status
     */
    @Test
    fun `PermissionItem has correct type and status mapping`() {
        // Given
        every { Settings.canDrawOverlays(any()) } returns true
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_GRANTED

        // When
        val result = repository.checkPermissionStatus(PermissionType.SYSTEM_ALERT_WINDOW)

        // Then
        assertEquals(PermissionType.SYSTEM_ALERT_WINDOW, result.type)
        assertEquals(PermissionStatus.GRANTED, result.status)
    }

    /**
     * Test: Multiple permission checks return correct results
     */
    @Test
    fun `multiple permission checks return correct individual results`() {
        // Given - mixed states
        every { Settings.canDrawOverlays(any()) } returns true
        every { ContextCompat.checkSelfPermission(any(), any()) } returns PackageManager.PERMISSION_DENIED
        every { Settings.Secure.getString(any(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) } returns null

        // When
        val overlayResult = repository.checkPermissionStatus(PermissionType.SYSTEM_ALERT_WINDOW)
        val audioResult = repository.checkPermissionStatus(PermissionType.RECORD_AUDIO)
        val accessibilityResult = repository.checkPermissionStatus(PermissionType.BIND_ACCESSIBILITY_SERVICE)

        // Then
        assertTrue(overlayResult.isGranted)
        assertFalse(audioResult.isGranted)
        assertFalse(accessibilityResult.isGranted)
    }
}

package com.petdesk.domain.repository

import android.content.Context
import android.content.Intent
import com.petdesk.domain.model.PermissionItem
import com.petdesk.domain.model.PermissionState
import com.petdesk.domain.model.PermissionType

/**
 * 权限仓储接口
 */
interface PermissionRepository {

    /**
     * 检查指定权限的状态
     */
    fun checkPermissionStatus(permissionType: PermissionType): PermissionItem

    /**
     * 检查所有权限的状态
     */
    fun checkAllPermissionsState(): PermissionState

    /**
     * 创建申请悬浮窗权限的Intent
     */
    fun createOverlayPermissionIntent(context: Context): Intent

    /**
     * 创建申请前台服务权限的Intent
     */
    fun createForegroundServicePermissionIntent(context: Context): Intent

    /**
     * 创建申请应用使用统计权限的Intent
     */
    fun createUsageStatsPermissionIntent(context: Context): Intent

    /**
     * 创建申请无障碍服务权限的Intent
     */
    fun createAccessibilityServicePermissionIntent(context: Context): Intent

    /**
     * 创建申请通知权限的Intent (Android 13+)
     */
    fun createNotificationPermissionIntent(context: Context): Intent

    /**
     * 创建申请存储权限的Intent
     */
    fun createStoragePermissionIntent(context: Context): Intent

    /**
     * 申请需要运行时请求的权限
     */
    fun requestRuntimePermissions(permissions: List<PermissionType>): Array<String>

    /**
     * 获取需要用户手动授权的权限列表
     */
    fun getPermissionsNeedingManualGrant(): List<PermissionType>

    /**
     * 判断是否是特殊权限（需要在设置中手动授权）
     */
    fun isSpecialPermission(permissionType: PermissionType): Boolean

    /**
     * 获取设备厂商类型
     */
    fun getManufacturer(): Manufacturer

    /**
     * 创建厂商特定的应用详情页Intent
     */
    fun createManufacturerAppSettingsIntent(context: Context): Intent
}

/**
 * 设备厂商枚举
 */
enum class Manufacturer {
    HUAWEI,    // 华为
    XIAOMI,    // 小米
    OPPO,      // OPPO
    VIVO,      // vivo
    SAMSUNG,   // 三星
    OTHER      // 其他
}

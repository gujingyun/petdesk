package com.petdesk.domain.model

import android.Manifest

/**
 * 权限类型枚举
 */
enum class PermissionType(
    val permission: String,
    val displayName: String,
    val description: String,
    val isRequired: Boolean,
    val androidMinVersion: Int? = null
) {
    // 必选权限
    SYSTEM_ALERT_WINDOW(
        permission = "android.permission.SYSTEM_ALERT_WINDOW",
        displayName = "悬浮窗权限",
        description = "允许应用在其他应用上层显示悬浮窗，这是桌宠显示的必要权限",
        isRequired = true
    ),
    FOREGROUND_SERVICE(
        permission = "android.permission.FOREGROUND_SERVICE",
        displayName = "前台服务权限",
        description = "允许应用运行前台服务，确保桌宠持续运行不被系统杀死",
        isRequired = true,
        androidMinVersion = 28
    ),
    POST_NOTIFICATIONS(
        permission = Manifest.permission.POST_NOTIFICATIONS,
        displayName = "通知权限",
        description = "允许应用发送通知，用于提醒桌宠状态变化",
        isRequired = true,
        androidMinVersion = 33
    ),
    STORAGE(
        permission = "android.permission.STORAGE",
        displayName = "存储权限",
        description = "允许应用读取/写入存储，用于保存和加载桌宠数据",
        isRequired = true,
        androidMinVersion = 29
    ),

    // 可选权限
    PACKAGE_USAGE_STATS(
        permission = "android.permission.PACKAGE_USAGE_STATS",
        displayName = "应用使用统计权限",
        description = "允许应用获取其他应用的使用情况，用于智能提醒功能",
        isRequired = false
    ),
    QUERY_ALL_PACKAGES(
        permission = "android.permission.QUERY_ALL_PACKAGES",
        displayName = "查询所有应用权限",
        description = "允许应用查询设备上安装的所有应用列表",
        isRequired = false,
        androidMinVersion = 30
    ),
    BIND_ACCESSIBILITY_SERVICE(
        permission = "android.permission.BIND_ACCESSIBILITY_SERVICE",
        displayName = "无障碍服务权限",
        description = "允许应用使用无障碍服务，用于桌面整理和自动化功能",
        isRequired = false
    ),
    RECORD_AUDIO(
        permission = Manifest.permission.RECORD_AUDIO,
        displayName = "麦克风权限",
        description = "允许应用使用麦克风，用于语音交互功能",
        isRequired = false
    )
}

/**
 * 权限状态
 */
enum class PermissionStatus {
    GRANTED,      // 已授权
    DENIED,       // 被拒绝
    NOT_REQUESTED, // 未申请
    RESTRICTED    // 受限制
}

/**
 * 权限项
 */
data class PermissionItem(
    val type: PermissionType,
    val status: PermissionStatus,
    val isGranted: Boolean
)

/**
 * 整体权限状态
 */
data class PermissionState(
    val permissions: List<PermissionItem>,
    val allRequiredGranted: Boolean,
    val allGranted: Boolean
)

package com.petdesk.data.repository

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.petdesk.domain.model.PermissionItem
import com.petdesk.domain.model.PermissionState
import com.petdesk.domain.model.PermissionStatus
import com.petdesk.domain.model.PermissionType
import com.petdesk.domain.repository.Manufacturer
import com.petdesk.domain.repository.PermissionRepository
import com.petdesk.service.DesktopAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 权限仓储实现
 */
@Singleton
class PermissionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PermissionRepository {

    override fun checkPermissionStatus(permissionType: PermissionType): PermissionItem {
        val status = when (permissionType) {
            PermissionType.SYSTEM_ALERT_WINDOW -> checkOverlayPermission()
            PermissionType.FOREGROUND_SERVICE -> checkForegroundServicePermission()
            PermissionType.POST_NOTIFICATIONS -> checkNotificationPermission()
            PermissionType.STORAGE -> checkStoragePermission()
            PermissionType.PACKAGE_USAGE_STATS -> checkUsageStatsPermission()
            PermissionType.QUERY_ALL_PACKAGES -> checkQueryAllPackagesPermission()
            PermissionType.BIND_ACCESSIBILITY_SERVICE -> checkAccessibilityServicePermission()
            PermissionType.RECORD_AUDIO -> checkRecordAudioPermission()
        }
        return PermissionItem(
            type = permissionType,
            status = status,
            isGranted = status == PermissionStatus.GRANTED
        )
    }

    override fun checkAllPermissionsState(): PermissionState {
        val permissionItems = PermissionType.entries.map { checkPermissionStatus(it) }
        val requiredPermissions = permissionItems.filter { it.type.isRequired }

        return PermissionState(
            permissions = permissionItems,
            allRequiredGranted = requiredPermissions.all { it.isGranted },
            allGranted = permissionItems.all { it.isGranted }
        )
    }

    override fun createOverlayPermissionIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
    }

    override fun createForegroundServicePermissionIntent(context: Context): Intent {
        // 前台服务权限是运行时权限，需要在代码中动态申请
        // 但某些厂商可能需要特殊处理
        return createAppSettingsIntent(context)
    }

    override fun createUsageStatsPermissionIntent(context: Context): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }

    override fun createAccessibilityServicePermissionIntent(context: Context): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }

    override fun createNotificationPermissionIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            createAppSettingsIntent(context)
        }
    }

    override fun createStoragePermissionIntent(context: Context): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        } else {
            createAppSettingsIntent(context)
        }
    }

    override fun requestRuntimePermissions(permissions: List<PermissionType>): Array<String> {
        return permissions
            .filter { !isSpecialPermission(it) }
            .map { it.permission }
            .toTypedArray()
    }

    override fun getPermissionsNeedingManualGrant(): List<PermissionType> {
        return PermissionType.entries.filter { isSpecialPermission(it) }
    }

    override fun isSpecialPermission(permissionType: PermissionType): Boolean {
        return when (permissionType) {
            PermissionType.SYSTEM_ALERT_WINDOW,
            PermissionType.PACKAGE_USAGE_STATS,
            PermissionType.BIND_ACCESSIBILITY_SERVICE -> true
            PermissionType.FOREGROUND_SERVICE,
            PermissionType.POST_NOTIFICATIONS,
            PermissionType.STORAGE,
            PermissionType.QUERY_ALL_PACKAGES,
            PermissionType.RECORD_AUDIO -> false
        }
    }

    override fun getManufacturer(): Manufacturer {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> Manufacturer.HUAWEI
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") -> Manufacturer.XIAOMI
            manufacturer.contains("oppo") || manufacturer.contains("realme") -> Manufacturer.OPPO
            manufacturer.contains("vivo") || manufacturer.contains("iqoo") -> Manufacturer.VIVO
            manufacturer.contains("samsung") -> Manufacturer.SAMSUNG
            else -> Manufacturer.OTHER
        }
    }

    override fun createManufacturerAppSettingsIntent(context: Context): Intent {
        val manufacturer = getManufacturer()
        val intent = createAppSettingsIntent(context)

        // 尝试添加厂商特定设置
        when (manufacturer) {
            Manufacturer.HUAWEI -> {
                // 华为EMUI可能有特殊的应用启动管理
                try {
                    val huaweiIntent = Intent().apply {
                        component = android.content.ComponentName(
                            "com.huawei.systemmanager",
                            "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                        )
                    }
                    if (huaweiIntent.resolveActivity(context.packageManager) != null) {
                        return huaweiIntent
                    }
                } catch (e: Exception) {
                    // 忽略异常
                }
            }
            Manufacturer.XIAOMI -> {
                // 小米MIUI有应用权限管理
                try {
                    val xiaomiIntent = Intent().apply {
                        component = android.content.ComponentName(
                            "com.miui.securitycenter",
                            "com.miui.permcenter.autostart.AutoStartManagementActivity"
                        )
                    }
                    if (xiaomiIntent.resolveActivity(context.packageManager) != null) {
                        return xiaomiIntent
                    }
                } catch (e: Exception) {
                    // 忽略异常
                }
            }
            Manufacturer.OPPO -> {
                // OPPO ColorOS有应用启动管理
                try {
                    val oppoIntent = Intent().apply {
                        component = android.content.ComponentName(
                            "com.coloros.safecenter",
                            "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                        )
                    }
                    if (oppoIntent.resolveActivity(context.packageManager) != null) {
                        return oppoIntent
                    }
                } catch (e: Exception) {
                    // 忽略异常
                }
            }
            Manufacturer.VIVO -> {
                // vivo FuntouchOS有后台管理
                try {
                    val vivoIntent = Intent().apply {
                        component = android.content.ComponentName(
                            "com.vivo.permissionmanager",
                            "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                        )
                    }
                    if (vivoIntent.resolveActivity(context.packageManager) != null) {
                        return vivoIntent
                    }
                } catch (e: Exception) {
                    // 忽略异常
                }
            }
            Manufacturer.SAMSUNG -> {
                // 三星One UI有电池和后台限制
                try {
                    val samsungIntent = Intent().apply {
                        component = android.content.ComponentName(
                            "com.samsung.android.lool",
                            "com.samsung.android.sm.ui.battery.BatteryActivity"
                        )
                    }
                    if (samsungIntent.resolveActivity(context.packageManager) != null) {
                        return samsungIntent
                    }
                } catch (e: Exception) {
                    // 忽略异常
                }
            }
            Manufacturer.OTHER -> { /* 使用默认设置 */ }
        }

        return intent
    }

    // 私有辅助方法

    private fun checkOverlayPermission(): PermissionStatus {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(context)) {
                PermissionStatus.GRANTED
            } else {
                PermissionStatus.DENIED
            }
        } else {
            PermissionStatus.GRANTED
        }
    }

    private fun checkForegroundServicePermission(): PermissionStatus {
        // Android 9+ 需要 FOREGROUND_SERVICE 权限
        // 但在Android 14之前，这是安装时授予的权限
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permission = Manifest.permission.FOREGROUND_SERVICE
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                PermissionStatus.GRANTED
            } else {
                PermissionStatus.DENIED
            }
        } else {
            PermissionStatus.GRANTED
        }
    }

    private fun checkNotificationPermission(): PermissionStatus {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                PermissionStatus.GRANTED
            } else {
                PermissionStatus.DENIED
            }
        } else {
            PermissionStatus.GRANTED
        }
    }

    private fun checkStoragePermission(): PermissionStatus {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Settings.canManageExternalStorage()) {
                PermissionStatus.GRANTED
            } else {
                // 对于Android 11+，我们使用分区存储，可能不需要完全访问
                PermissionStatus.GRANTED
            }
        } else {
            val readPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_EXTERNAL_STORAGE
            )
            val writePermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (readPermission == PackageManager.PERMISSION_GRANTED &&
                writePermission == PackageManager.PERMISSION_GRANTED
            ) {
                PermissionStatus.GRANTED
            } else {
                PermissionStatus.DENIED
            }
        }
    }

    private fun checkUsageStatsPermission(): PermissionStatus {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return if (mode == AppOpsManager.MODE_ALLOWED) {
            PermissionStatus.GRANTED
        } else {
            PermissionStatus.DENIED
        }
    }

    private fun checkQueryAllPackagesPermission(): PermissionStatus {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val permission = Manifest.permission.QUERY_ALL_PACKAGES
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                PermissionStatus.GRANTED
            } else {
                PermissionStatus.DENIED
            }
        } else {
            PermissionStatus.GRANTED
        }
    }

    private fun checkAccessibilityServicePermission(): PermissionStatus {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return if (!enabledServices.isNullOrEmpty()) {
            val serviceName = "${context.packageName}/${DesktopAccessibilityService::class.java.name}"
            if (enabledServices.contains(serviceName)) {
                PermissionStatus.GRANTED
            } else {
                PermissionStatus.DENIED
            }
        } else {
            PermissionStatus.DENIED
        }
    }

    private fun checkRecordAudioPermission(): PermissionStatus {
        val permission = Manifest.permission.RECORD_AUDIO
        return if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            PermissionStatus.GRANTED
        } else {
            PermissionStatus.DENIED
        }
    }

    private fun createAppSettingsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }
}

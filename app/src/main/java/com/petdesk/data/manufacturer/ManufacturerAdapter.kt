package com.petdesk.data.manufacturer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.petdesk.domain.repository.Manufacturer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 厂商适配器 - 处理不同厂商的特殊权限设置
 */
@Singleton
class ManufacturerAdapter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val detectedManufacturer: Manufacturer by lazy {
        detectManufacturer()
    }

    /**
     * 检测设备厂商
     */
    private fun detectManufacturer(): Manufacturer {
        val manufacturerName = Build.MANUFACTURER.lowercase()
        return when {
            manufacturerName.contains("huawei") || manufacturerName.contains("honor") -> Manufacturer.HUAWEI
            manufacturerName.contains("xiaomi") || manufacturerName.contains("redmi") -> Manufacturer.XIAOMI
            manufacturerName.contains("oppo") || manufacturerName.contains("realme") -> Manufacturer.OPPO
            manufacturerName.contains("vivo") || manufacturerName.contains("iqoo") -> Manufacturer.VIVO
            manufacturerName.contains("samsung") -> Manufacturer.SAMSUNG
            else -> Manufacturer.OTHER
        }
    }

    /**
     * 获取当前厂商
     */
    fun getManufacturer(): Manufacturer = detectedManufacturer

    /**
     * 获取厂商名称（中文）
     */
    fun getManufacturerName(): String {
        return when (detectedManufacturer) {
            Manufacturer.HUAWEI -> "华为"
            Manufacturer.XIAOMI -> "小米"
            Manufacturer.OPPO -> "OPPO"
            Manufacturer.VIVO -> "vivo"
            Manufacturer.SAMSUNG -> "三星"
            Manufacturer.OTHER -> "其他品牌"
        }
    }

    /**
     * 获取厂商特定的应用启动管理设置Intent
     */
    fun getAutoStartSettingsIntent(): Intent? {
        val intent = when (detectedManufacturer) {
            Manufacturer.HUAWEI -> createHuaweiAutoStartIntent()
            Manufacturer.XIAOMI -> createXiaomiAutoStartIntent()
            Manufacturer.OPPO -> createOppoAutoStartIntent()
            Manufacturer.VIVO -> createVivoAutoStartIntent()
            Manufacturer.SAMSUNG -> createSamsungAutoStartIntent()
            Manufacturer.OTHER -> null
        }
        return intent?.takeIf { it.resolveActivity(context.packageManager) != null }
    }

    /**
     * 获取厂商特定的电池优化设置Intent
     */
    fun getBatteryOptimizationIntent(): Intent {
        return Intent().apply {
            action = android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
            data = android.net.Uri.parse("package:${context.packageName}")
        }
    }

    /**
     * 获取厂商特定的权限管理设置Intent
     */
    fun getPermissionSettingsIntent(): Intent {
        val intent = when (detectedManufacturer) {
            Manufacturer.HUAWEI -> createHuaweiPermissionIntent()
            Manufacturer.XIAOMI -> createXiaomiPermissionIntent()
            Manufacturer.OPPO -> createOppoPermissionIntent()
            Manufacturer.VIVO -> createVivoPermissionIntent()
            Manufacturer.SAMSUNG -> createSamsungPermissionIntent()
            Manufacturer.OTHER -> createDefaultPermissionIntent()
        }
        return intent
    }

    /**
     * 获取权限设置引导信息
     */
    fun getPermissionGuideMessage(): String {
        return when (detectedManufacturer) {
            Manufacturer.HUAWEI -> """
                华为/荣耀设备设置指南：
                1. 进入"设置" > "应用" > "应用启动管理"
                2. 找到"桌宠"APP
                3. 关闭"自动管理"，手动开启"允许自启动"、"允许关联启动"、"允许后台运行"
            """.trimIndent()

            Manufacturer.XIAOMI -> """
                小米/红米设备设置指南：
                1. 进入"设置" > "应用" > "应用管理"
                2. 找到"桌宠"APP > "权限"
                3. 开启所有必要权限
                4. 返回应用信息页面，开启"自启动"
                5. 进入"设置" > "电量" > "省电策略"，选择"无限制"
            """.trimIndent()

            Manufacturer.OPPO -> """
                OPPO/真我设备设置指南：
                1. 进入"设置" > "应用" > "应用管理"
                2. 找到"桌宠"APP > "权限"
                3. 开启所有必要权限
                4. 进入"设置" > "电池" > "更多设置" > "应用启动管理"
                5. 找到"桌宠"，手动管理并开启所有开关
            """.trimIndent()

            Manufacturer.VIVO -> """
                vivo/iQOO设备设置指南：
                1. 进入"设置" > "应用" > "应用管理"
                2. 找到"桌宠"APP > "权限"
                3. 开启所有必要权限
                4. 返回"应用管理" > "自启动"
                5. 开启"桌宠"的自启动权限
            """.trimIndent()

            Manufacturer.SAMSUNG -> """
                三星设备设置指南：
                1. 进入"设置" > "应用" > "应用管理"
                2. 找到"桌宠"APP > "权限"
                3. 开启所有必要权限
                4. 进入"设置" > "电池和设备保养" > "电池" > "后台使用限制"
                5. 找到"桌宠"，选择"不限制"
            """.trimIndent()

            Manufacturer.OTHER -> """
                设置指南：
                1. 进入"设置" > "应用" > "应用管理"
                2. 找到"桌宠"APP > "权限"
                3. 开启所有必要权限
                4. 尝试开启自启动权限（如果有）
            """.trimIndent()
        }
    }

    // 私有辅助方法

    private fun createHuaweiAutoStartIntent(): Intent {
        return Intent().apply {
            component = ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
            )
        }
    }

    private fun createXiaomiAutoStartIntent(): Intent {
        return Intent().apply {
            component = ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.autostart.AutoStartManagementActivity"
            )
        }
    }

    private fun createOppoAutoStartIntent(): Intent {
        return Intent().apply {
            component = ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.permission.startup.StartupAppListActivity"
            )
        }
    }

    private fun createVivoAutoStartIntent(): Intent {
        return Intent().apply {
            component = ComponentName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
            )
        }
    }

    private fun createSamsungAutoStartIntent(): Intent {
        return Intent().apply {
            component = ComponentName(
                "com.samsung.android.sm",
                "com.samsung.android.sm.ui.battery.BatteryActivity"
            )
        }
    }

    private fun createHuaweiPermissionIntent(): Intent {
        return Intent().apply {
            component = ComponentName(
                "com.huawei.systemmanager",
                "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
            )
        }
    }

    private fun createXiaomiPermissionIntent(): Intent {
        return Intent().apply {
            component = ComponentName(
                "com.miui.securitycenter",
                "com.miui.permcenter.permissions.AppPermissionsEditorActivity"
            )
        }
    }

    private fun createOppoPermissionIntent(): Intent {
        return Intent().apply {
            component = ComponentName(
                "com.coloros.safecenter",
                "com.coloros.safecenter.startupapp.StartupAppListActivity"
            )
        }
    }

    private fun createVivoPermissionIntent(): Intent {
        return Intent().apply {
            component = ComponentName(
                "com.vivo.permissionmanager",
                "com.vivo.permissionmanager.activity.PermissionManagerActivity"
            )
        }
    }

    private fun createSamsungPermissionIntent(): Intent {
        return Intent().apply {
            component = ComponentName(
                "com.samsung.android.sm",
                "com.samsung.android.sm.ui.permissions.PermissionsActivity"
            )
        }
    }

    private fun createDefaultPermissionIntent(): Intent {
        return Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
        }
    }
}

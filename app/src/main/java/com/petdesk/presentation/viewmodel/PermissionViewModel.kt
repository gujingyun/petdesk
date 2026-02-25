package com.petdesk.presentation.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petdesk.data.manufacturer.ManufacturerAdapter
import com.petdesk.domain.model.PermissionItem
import com.petdesk.domain.model.PermissionState
import com.petdesk.domain.model.PermissionStatus
import com.petdesk.domain.model.PermissionType
import com.petdesk.domain.repository.Manufacturer
import com.petdesk.domain.repository.PermissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 权限管理模块的UI状态
 */
data class PermissionUiState(
    val permissionState: PermissionState = PermissionState(
        permissions = emptyList(),
        allRequiredGranted = false,
        allGranted = false
    ),
    val isLoading: Boolean = true,
    val manufacturer: Manufacturer = Manufacturer.OTHER,
    val manufacturerName: String = "",
    val permissionGuideMessage: String = "",
    val selectedPermission: PermissionType? = null,
    val showTutorial: Boolean = false,
    val currentTutorialStep: Int = 0,
    val errorMessage: String? = null
)

/**
 * 权限管理模块的ViewModel
 */
@HiltViewModel
class PermissionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionRepository: PermissionRepository,
    private val manufacturerAdapter: ManufacturerAdapter
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()

    init {
        loadPermissions()
    }

    /**
     * 加载所有权限状态
     */
    fun loadPermissions() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val permissionState = permissionRepository.checkAllPermissionsState()
                val manufacturer = manufacturerAdapter.getManufacturer()
                val manufacturerName = manufacturerAdapter.getManufacturerName()
                val permissionGuideMessage = manufacturerAdapter.getPermissionGuideMessage()

                _uiState.value = _uiState.value.copy(
                    permissionState = permissionState,
                    isLoading = false,
                    manufacturer = manufacturer,
                    manufacturerName = manufacturerName,
                    permissionGuideMessage = permissionGuideMessage,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    /**
     * 刷新权限状态
     */
    fun refreshPermissionStatus() {
        loadPermissions()
    }

    /**
     * 申请悬浮窗权限
     */
    fun requestOverlayPermission(): Intent {
        return permissionRepository.createOverlayPermissionIntent(context)
    }

    /**
     * 申请前台服务权限
     */
    fun requestForegroundServicePermission(): Intent {
        return permissionRepository.createForegroundServicePermissionIntent(context)
    }

    /**
     * 申请应用使用统计权限
     */
    fun requestUsageStatsPermission(): Intent {
        return permissionRepository.createUsageStatsPermissionIntent(context)
    }

    /**
     * 申请无障碍服务权限
     */
    fun requestAccessibilityServicePermission(): Intent {
        return permissionRepository.createAccessibilityServicePermissionIntent(context)
    }

    /**
     * 申请通知权限
     */
    fun requestNotificationPermission(): Intent {
        return permissionRepository.createNotificationPermissionIntent(context)
    }

    /**
     * 申请存储权限
     */
    fun requestStoragePermission(): Intent {
        return permissionRepository.createStoragePermissionIntent(context)
    }

    /**
     * 申请运行时权限（需要Activity回调的权限）
     */
    fun getRuntimePermissions(): Array<String> {
        val runtimePermissions = listOf(
            PermissionType.FOREGROUND_SERVICE,
            PermissionType.POST_NOTIFICATIONS,
            PermissionType.STORAGE,
            PermissionType.RECORD_AUDIO
        ).filter { permissionRepository.checkPermissionStatus(it).status != PermissionStatus.GRANTED }

        return permissionRepository.requestRuntimePermissions(runtimePermissions)
    }

    /**
     * 获取厂商特定的应用设置Intent
     */
    fun getManufacturerAppSettingsIntent(): Intent {
        return manufacturerAdapter.createManufacturerAppSettingsIntent(context)
    }

    /**
     * 获取厂商自动启动设置Intent
     */
    fun getAutoStartSettingsIntent(): Intent? {
        return manufacturerAdapter.getAutoStartSettingsIntent()
    }

    /**
     * 获取电池优化设置Intent
     */
    fun getBatteryOptimizationIntent(): Intent {
        return manufacturerAdapter.getBatteryOptimizationIntent()
    }

    /**
     * 获取权限设置Intent
     */
    fun getPermissionSettingsIntent(permissionType: PermissionType): Intent {
        return when (permissionType) {
            PermissionType.SYSTEM_ALERT_WINDOW -> requestOverlayPermission()
            PermissionType.PACKAGE_USAGE_STATS -> requestUsageStatsPermission()
            PermissionType.BIND_ACCESSIBILITY_SERVICE -> requestAccessibilityServicePermission()
            else -> permissionRepository.createManufacturerAppSettingsIntent(context)
        }
    }

    /**
     * 选择要查看详情的权限
     */
    fun selectPermission(permissionType: PermissionType) {
        _uiState.value = _uiState.value.copy(selectedPermission = permissionType)
    }

    /**
     * 清除选中的权限
     */
    fun clearSelectedPermission() {
        _uiState.value = _uiState.value.copy(selectedPermission = null)
    }

    /**
     * 显示权限引导教程
     */
    fun showTutorial() {
        _uiState.value = _uiState.value.copy(
            showTutorial = true,
            currentTutorialStep = 0
        )
    }

    /**
     * 隐藏权限引导教程
     */
    fun hideTutorial() {
        _uiState.value = _uiState.value.copy(
            showTutorial = false,
            currentTutorialStep = 0
        )
    }

    /**
     * 教程下一步
     */
    fun nextTutorialStep() {
        val currentStep = _uiState.value.currentTutorialStep
        val maxStep = getTutorialStepCount() - 1
        if (currentStep < maxStep) {
            _uiState.value = _uiState.value.copy(currentTutorialStep = currentStep + 1)
        }
    }

    /**
     * 教程上一步
     */
    fun previousTutorialStep() {
        val currentStep = _uiState.value.currentTutorialStep
        if (currentStep > 0) {
            _uiState.value = _uiState.value.copy(currentTutorialStep = currentStep - 1)
        }
    }

    /**
     * 获取教程步骤数量
     */
    fun getTutorialStepCount(): Int = 5

    /**
     * 获取教程步骤标题
     */
    fun getTutorialStepTitle(step: Int): String {
        return when (step) {
            0 -> "欢迎使用"
            1 -> "悬浮窗权限"
            2 -> "前台服务权限"
            3 -> "厂商适配设置"
            4 -> "完成设置"
            else -> ""
        }
    }

    /**
     * 获取教程步骤描述
     */
    fun getTutorialStepDescription(step: Int): String {
        return when (step) {
            0 -> "桌宠APP需要一些必要的权限才能正常运行，让我们一起来设置吧！"
            1 -> "悬浮窗权限允许桌宠显示在屏幕最上层，这是桌宠显示的必要条件。请点击\"去设置\"并开启悬浮窗权限。"
            2 -> "前台服务权限确保桌宠能够在后台持续运行，不会被系统杀死。开启后桌宠将作为通知持续运行。"
            3 -> "由于您的设备是${_uiState.value.manufacturerName}，需要额外设置自启动和后台运行权限，以确保桌宠稳定运行。"
            4 -> "恭喜您！已完成所有必要权限的设置，现在可以开始使用桌宠了。"
            else -> ""
        }
    }

    /**
     * 获取需要显示的一键申请权限列表
     */
    fun getPermissionsForOneClickRequest(): List<PermissionItem> {
        return _uiState.value.permissionState.permissions
    }

    /**
     * 检查是否所有必选权限都已授予
     */
    fun areAllRequiredPermissionsGranted(): Boolean {
        return _uiState.value.permissionState.allRequiredGranted
    }

    /**
     * 获取权限说明信息
     */
    fun getPermissionDescription(permissionType: PermissionType): String {
        return permissionType.description
    }

    /**
     * 获取权限必要性说明
     */
    fun getPermissionNecessity(permissionType: PermissionType): String {
        return if (permissionType.isRequired) {
            "必选 - 该权限是应用运行的必要条件"
        } else {
            "可选 - 该权限用于扩展功能，可以不授权"
        }
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

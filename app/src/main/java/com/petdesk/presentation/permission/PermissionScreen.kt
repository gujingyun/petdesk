package com.petdesk.presentation.permission

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petdesk.domain.model.PermissionItem
import com.petdesk.domain.model.PermissionStatus
import com.petdesk.domain.model.PermissionType
import com.petdesk.presentation.viewmodel.PermissionUiState
import com.petdesk.presentation.viewmodel.PermissionViewModel

/**
 * 权限管理主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionScreen(
    viewModel: PermissionViewModel = hiltViewModel(),
    onPermissionGranted: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    // 运行时权限请求Launcher
    val runtimePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.refreshPermissionStatus()
    }

    // 特殊权限请求Launcher
    val specialPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshPermissionStatus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("权限管理") },
                actions = {
                    IconButton(onClick = { viewModel.showTutorial() }) {
                        Icon(Icons.Default.Help, contentDescription = "权限教程")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                PermissionContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    onRequestPermission = { permissionType ->
                        handlePermissionRequest(
                            permissionType = permissionType,
                            viewModel = viewModel,
                            activity = activity,
                            runtimePermissionLauncher = { permissions ->
                                runtimePermissionLauncher.launch(permissions)
                            },
                            specialPermissionLauncher = { intent ->
                                specialPermissionLauncher.launch(intent)
                            }
                        )
                    },
                    onOneClickRequest = {
                        handleOneClickRequest(
                            uiState = uiState,
                            viewModel = viewModel,
                            activity = activity,
                            runtimePermissionLauncher = { permissions ->
                                runtimePermissionLauncher.launch(permissions)
                            },
                            specialPermissionLauncher = { intent ->
                                specialPermissionLauncher.launch(intent)
                            }
                        )
                    },
                    onNavigateToManufacturerSettings = {
                        viewModel.getAutoStartSettingsIntent()?.let { intent ->
                            specialPermissionLauncher.launch(intent)
                        } ?: run {
                            specialPermissionLauncher.launch(viewModel.getManufacturerAppSettingsIntent())
                        }
                    }
                )
            }
        }
    }

    // 权限详情对话框
    if (uiState.selectedPermission != null) {
        PermissionDetailDialog(
            permissionType = uiState.selectedPermission!!,
            permissionItem = uiState.permissionState.permissions.find {
                it.type == uiState.selectedPermission
            },
            onDismiss = { viewModel.clearSelectedPermission() },
            onRequestPermission = { permissionType ->
                handlePermissionRequest(
                    permissionType = permissionType,
                    viewModel = viewModel,
                    activity = activity,
                    runtimePermissionLauncher = { permissions ->
                        runtimePermissionLauncher.launch(permissions)
                    },
                    specialPermissionLauncher = { intent ->
                        specialPermissionLauncher.launch(intent)
                    }
                )
            }
        )
    }

    // 权限教程对话框
    if (uiState.showTutorial) {
        TutorialDialog(
            currentStep = uiState.currentTutorialStep,
            totalSteps = viewModel.getTutorialStepCount(),
            stepTitle = viewModel.getTutorialStepTitle(uiState.currentTutorialStep),
            stepDescription = viewModel.getTutorialStepDescription(uiState.currentTutorialStep),
            manufacturerName = uiState.manufacturerName,
            onNext = { viewModel.nextTutorialStep() },
            onPrevious = { viewModel.previousTutorialStep() },
            onDismiss = { viewModel.hideTutorial() }
        )
    }
}

/**
 * 权限内容区域
 */
@Composable
private fun PermissionContent(
    uiState: PermissionUiState,
    viewModel: PermissionViewModel,
    onRequestPermission: (PermissionType) -> Unit,
    onOneClickRequest: () -> Unit,
    onNavigateToManufacturerSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 权限状态概览
        item {
            PermissionOverviewCard(uiState = uiState)
        }

        // 一键申请按钮
        item {
            OneClickRequestCard(
                permissions = uiState.permissionState.permissions,
                onOneClickRequest = onOneClickRequest
            )
        }

        // 权限列表标题
        item {
            Text(
                text = "权限列表",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // 必选权限
        item {
            Text(
                text = "必选权限",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        items(
            items = uiState.permissionState.permissions.filter { it.type.isRequired },
            key = { it.type.name }
        ) { permissionItem ->
            PermissionItemCard(
                permissionItem = permissionItem,
                onClick = { viewModel.selectPermission(permissionItem.type) },
                onRequestPermission = { onRequestPermission(permissionItem.type) }
            )
        }

        // 可选权限
        if (uiState.permissionState.permissions.any { !it.type.isRequired }) {
            item {
                Text(
                    text = "可选权限",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(
                items = uiState.permissionState.permissions.filter { !it.type.isRequired },
                key = { it.type.name }
            ) { permissionItem ->
                PermissionItemCard(
                    permissionItem = permissionItem,
                    onClick = { viewModel.selectPermission(permissionItem.type) },
                    onRequestPermission = { onRequestPermission(permissionItem.type) }
                )
            }
        }

        // 厂商适配设置
        if (uiState.manufacturer.name != "OTHER") {
            item {
                ManufacturerSettingsCard(
                    manufacturerName = uiState.manufacturerName,
                    guideMessage = uiState.permissionGuideMessage,
                    onNavigate = onNavigateToManufacturerSettings
                )
            }
        }

        // 底部间距
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 权限状态概览卡片
 */
@Composable
private fun PermissionOverviewCard(uiState: PermissionUiState) {
    val grantedCount = uiState.permissionState.permissions.count { it.isGranted }
    val totalCount = uiState.permissionState.permissions.size
    val requiredGrantedCount = uiState.permissionState.permissions.count {
        it.type.isRequired && it.isGranted
    }
    val requiredTotalCount = uiState.permissionState.permissions.count { it.type.isRequired }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (uiState.permissionState.allRequiredGranted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (uiState.permissionState.allRequiredGranted) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.Warning
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = if (uiState.permissionState.allRequiredGranted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (uiState.permissionState.allRequiredGranted) {
                        "所有必选权限已授予"
                    } else {
                        "还需授予必选权限"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "必选权限: $requiredGrantedCount/$requiredTotalCount | 全部: $grantedCount/$totalCount",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * 一键申请卡片
 */
@Composable
private fun OneClickRequestCard(
    permissions: List<PermissionItem>,
    onOneClickRequest: () -> Unit
) {
    val hasUngrantedPermissions = permissions.any { !it.isGranted }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "一键申请权限",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击下方按钮一次性申请所有未授予的权限",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onOneClickRequest,
                modifier = Modifier.fillMaxWidth(),
                enabled = hasUngrantedPermissions
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (hasUngrantedPermissions) "一键申请所有权限" else "所有权限已授予")
            }
        }
    }
}

/**
 * 权限项卡片
 */
@Composable
private fun PermissionItemCard(
    permissionItem: PermissionItem,
    onClick: () -> Unit,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 权限图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (permissionItem.isGranted) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (permissionItem.isGranted) {
                        Icons.Default.Check
                    } else {
                        Icons.Default.Close
                    },
                    contentDescription = null,
                    tint = if (permissionItem.isGranted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 权限信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = permissionItem.type.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (permissionItem.isGranted) "已授权" else "未授权",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (permissionItem.isGranted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            // 申请按钮
            if (!permissionItem.isGranted) {
                Button(
                    onClick = onRequestPermission,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("去设置", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

/**
 * 厂商设置卡片
 */
@Composable
private fun ManufacturerSettingsCard(
    manufacturerName: String,
    guideMessage: String,
    onNavigate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$manufacturerName 设备专属设置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = guideMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("前往厂商设置")
            }
        }
    }
}

/**
 * 权限详情对话框
 */
@Composable
private fun PermissionDetailDialog(
    permissionType: PermissionType,
    permissionItem: PermissionItem?,
    onDismiss: () -> Unit,
    onRequestPermission: (PermissionType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = permissionType.displayName)
        },
        text = {
            Column {
                Text(
                    text = permissionType.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 必要性说明
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (permissionType.isRequired) {
                            Icons.Default.Warning
                        } else {
                            Icons.Default.Info
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (permissionType.isRequired) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (permissionType.isRequired) {
                            "必选权限 - 应用运行的必要条件"
                        } else {
                            "可选权限 - 用于扩展功能"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (permissionType.isRequired) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 状态说明
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (permissionItem?.isGranted == true) {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Default.Cancel
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (permissionItem?.isGranted == true) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (permissionItem?.isGranted == true) {
                            "状态: 已授权"
                        } else {
                            "状态: 未授权"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            if (permissionItem?.isGranted != true) {
                Button(onClick = { onRequestPermission(permissionType) }) {
                    Text("去设置")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

/**
 * 权限引导教程对话框
 */
@Composable
private fun TutorialDialog(
    currentStep: Int,
    totalSteps: Int,
    stepTitle: String,
    stepDescription: String,
    manufacturerName: String,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = stepTitle)
                // 进度指示器
                LinearProgressIndicator(
                    progress = { (currentStep + 1).toFloat() / totalSteps },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                )
            }
        },
        text = {
            Column {
                Text(
                    text = stepDescription.replace("{manufacturerName}", manufacturerName),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Row {
                if (currentStep > 0) {
                    TextButton(onClick = onPrevious) {
                        Text("上一步")
                    }
                }
                if (currentStep < totalSteps - 1) {
                    Button(onClick = onNext) {
                        Text("下一步")
                    }
                } else {
                    Button(onClick = onDismiss) {
                        Text("完成")
                    }
                }
            }
        },
        dismissButton = {
            if (currentStep < totalSteps - 1) {
                TextButton(onClick = onDismiss) {
                    Text("跳过")
                }
            }
        }
    )
}

/**
 * 处理单个权限申请
 */
private fun handlePermissionRequest(
    permissionType: PermissionType,
    viewModel: PermissionViewModel,
    activity: Activity?,
    runtimePermissionLauncher: (Array<String>) -> Unit,
    specialPermissionLauncher: (Intent) -> Unit
) {
    if (activity == null) return

    // 检查是否是特殊权限
    val isSpecialPermission = when (permissionType) {
        PermissionType.SYSTEM_ALERT_WINDOW,
        PermissionType.PACKAGE_USAGE_STATS,
        PermissionType.BIND_ACCESSIBILITY_SERVICE -> true
        else -> false
    }

    if (isSpecialPermission) {
        val intent = viewModel.getPermissionSettingsIntent(permissionType)
        specialPermissionLauncher(intent)
    } else {
        val runtimePermissions = viewModel.getRuntimePermissions()
        if (runtimePermissions.isNotEmpty()) {
            runtimePermissionLauncher(runtimePermissions)
        }
    }
}

/**
 * 处理一键申请所有权限
 */
private fun handleOneClickRequest(
    uiState: PermissionUiState,
    viewModel: PermissionViewModel,
    activity: Activity?,
    runtimePermissionLauncher: (Array<String>) -> Unit,
    specialPermissionLauncher: (Intent) -> Unit
) {
    if (activity == null) return

    // 先处理特殊权限
    val specialPermissions = uiState.permissionState.permissions.filter {
        !it.isGranted && when (it.type) {
            PermissionType.SYSTEM_ALERT_WINDOW,
            PermissionType.PACKAGE_USAGE_STATS,
            PermissionType.BIND_ACCESSIBILITY_SERVICE -> true
            else -> false
        }
    }

    if (specialPermissions.isNotEmpty()) {
        val intent = viewModel.getPermissionSettingsIntent(specialPermissions.first().type)
        specialPermissionLauncher(intent)
        return
    }

    // 处理运行时权限
    val runtimePermissions = viewModel.getRuntimePermissions()
    if (runtimePermissions.isNotEmpty()) {
        runtimePermissionLauncher(runtimePermissions)
    }
}

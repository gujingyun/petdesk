package com.petdesk.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petdesk.domain.repository.PermissionRepository
import com.petdesk.presentation.permission.PermissionScreen
import com.petdesk.presentation.viewmodel.PermissionViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 应用导航目标
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Permission : Screen("permission")
}

/**
 * 主应用入口
 */
@Composable
fun PetDeskApp(
    permissionViewModel: PermissionViewModel = hiltViewModel()
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    val permissionState by permissionViewModel.uiState.collectAsState()

    // 检查是否需要显示权限引导
    LaunchedEffect(permissionState.permissionState) {
        if (!permissionState.isLoading && !permissionState.permissionState.allRequiredGranted) {
            currentScreen = Screen.Permission
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Pets, contentDescription = "桌宠") },
                    label = { Text("桌宠") },
                    selected = currentScreen == Screen.Home,
                    onClick = { currentScreen = Screen.Home }
                )
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "权限",
                            tint = if (!permissionState.permissionState.allRequiredGranted) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    },
                    label = {
                        Text(
                            text = "权限",
                            color = if (!permissionState.permissionState.allRequiredGranted) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    },
                    selected = currentScreen == Screen.Permission,
                    onClick = { currentScreen = Screen.Permission }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.Home -> HomeScreen(
                    allRequiredPermissionsGranted = permissionState.permissionState.allRequiredGranted,
                    onNavigateToPermission = { currentScreen = Screen.Permission }
                )
                Screen.Permission -> PermissionScreen(
                    viewModel = permissionViewModel,
                    onPermissionGranted = {
                        permissionViewModel.refreshPermissionStatus()
                        if (permissionViewModel.areAllRequiredPermissionsGranted()) {
                            currentScreen = Screen.Home
                        }
                    }
                )
            }
        }
    }
}

/**
 * 首页占位符
 */
@Composable
private fun HomeScreen(
    allRequiredPermissionsGranted: Boolean,
    onNavigateToPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Pets,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "桌宠管理",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (!allRequiredPermissionsGranted) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "需要授予必要权限才能使用",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onNavigateToPermission) {
                Text("前往设置权限")
            }
        } else {
            Text(
                text = "权限已全部授予，可以正常使用",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

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
import com.petdesk.presentation.chat.ChatScreen
import com.petdesk.presentation.home.HomeScreen
import com.petdesk.presentation.permission.PermissionScreen
import com.petdesk.presentation.skills.SkillsScreen
import com.petdesk.presentation.viewmodel.FloatingWindowViewModel
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
sealed class Screen(val route: String, val title: String, val icon: @Composable () -> Unit = {}) {
    object Home : Screen("home", "桌宠", { Icon(Icons.Default.Pets, contentDescription = "桌宠") })
    object Chat : Screen("chat", "对话", { Icon(Icons.Default.Chat, contentDescription = "对话") })
    object Skills : Screen("skills", "技能", { Icon(Icons.Default.Psychology, contentDescription = "技能") })
    object Permission : Screen("permission", "权限", { Icon(Icons.Default.Security, contentDescription = "权限") })
}

/**
 * 主应用入口
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetDeskApp(
    permissionViewModel: PermissionViewModel = hiltViewModel(),
    floatingWindowViewModel: FloatingWindowViewModel = hiltViewModel()
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

    val permissionState by permissionViewModel.uiState.collectAsState()
    val petState by floatingWindowViewModel.petState.collectAsState()

    // 检查是否需要显示权限引导
    LaunchedEffect(permissionState.permissionState) {
        if (!permissionState.isLoading && !permissionState.permissionState.allRequiredGranted) {
            currentScreen = Screen.Permission
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("桌宠管理") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = Screen.Home.icon,
                    label = { Text(Screen.Home.title) },
                    selected = currentScreen == Screen.Home,
                    onClick = { currentScreen = Screen.Home }
                )
                NavigationBarItem(
                    icon = Screen.Chat.icon,
                    label = { Text(Screen.Chat.title) },
                    selected = currentScreen == Screen.Chat,
                    onClick = { currentScreen = Screen.Chat }
                )
                NavigationBarItem(
                    icon = Screen.Skills.icon,
                    label = { Text(Screen.Skills.title) },
                    selected = currentScreen == Screen.Skills,
                    onClick = { currentScreen = Screen.Skills }
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
                            text = Screen.Permission.title,
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
                    isPetVisible = petState.isVisible,
                    petTransparency = petState.transparency,
                    petSize = petState.size.name.lowercase(),
                    onToggleVisibility = { floatingWindowViewModel.updateVisibility(!petState.isVisible) },
                    onAdjustTransparency = { floatingWindowViewModel.updateTransparency(it) },
                    onAdjustSize = { floatingWindowViewModel.updateSize(it) },
                    onNavigateToChat = { currentScreen = Screen.Chat },
                    onNavigateToSkills = { currentScreen = Screen.Skills }
                )
                Screen.Chat -> ChatScreen()
                Screen.Skills -> SkillsScreen()
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

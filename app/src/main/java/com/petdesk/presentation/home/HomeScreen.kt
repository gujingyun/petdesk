package com.petdesk.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 首页 - 显示桌宠状态和控制
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    isPetVisible: Boolean,
    petTransparency: Float,
    petSize: String,
    onToggleVisibility: () -> Unit,
    onAdjustTransparency: (Float) -> Unit,
    onAdjustSize: (String) -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToSkills: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTransparencySlider by remember { mutableStateOf(false) }
    var showSizeSelector by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
            // 桌宠状态卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets,
                        contentDescription = "桌宠",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "桌宠状态",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isPetVisible) "显示中" else "已隐藏",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isPetVisible) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                }
            }

            // 控制按钮区域
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "快捷控制",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // 显示/隐藏按钮
                    Button(
                        onClick = onToggleVisibility,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPetVisible) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (isPetVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isPetVisible) "隐藏桌宠" else "显示桌宠")
                    }

                    // 透明度调节
                    OutlinedButton(
                        onClick = { showTransparencySlider = !showTransparencySlider },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Opacity,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("透明度: ${(petTransparency * 100).toInt()}%")
                    }

                    if (showTransparencySlider) {
                        Slider(
                            value = petTransparency,
                            onValueChange = onAdjustTransparency,
                            valueRange = 0.2f..1.0f,
                            steps = 7
                        )
                    }

                    // 大小调节
                    OutlinedButton(
                        onClick = { showSizeSelector = !showSizeSelector },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AspectRatio,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("大小: $petSize")
                    }

                    if (showSizeSelector) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("小" to "small", "中" to "medium", "大" to "large").forEach { (label, value) ->
                                FilterChip(
                                    selected = petSize == value,
                                    onClick = {
                                        onAdjustSize(value)
                                        showSizeSelector = false
                                    },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
            }

            // 功能入口
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "功能入口",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToChat,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("对话")
                        }

                        OutlinedButton(
                            onClick = onNavigateToSkills,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("技能")
                        }
                    }
                }
            }
        }
    }
}

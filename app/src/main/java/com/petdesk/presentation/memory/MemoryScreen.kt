package com.petdesk.presentation.memory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petdesk.data.local.entity.MemoryEntity
import com.petdesk.presentation.viewmodel.MemoryViewModel

/**
 * 记忆界面
 * 展示和管理桌宠的记忆信息
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    viewModel: MemoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf(
        null to "全部",
        "general" to "通用",
        "preference" to "偏好",
        "habit" to "习惯",
        "relationship" to "关系"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "记忆中心",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Row {
                // 添加记忆按钮
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加记忆"
                    )
                }

                // 清空记忆按钮
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "清空记忆"
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 搜索栏
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.searchMemories(it)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索记忆...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        viewModel.searchMemories("")
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "清除")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 分类筛选
        ScrollableTabRow(
            selectedTabIndex = categories.indexOfFirst { it.first == state.selectedCategory }.coerceAtLeast(0),
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 0.dp
        ) {
            categories.forEachIndexed { index, (category, label) ->
                Tab(
                    selected = state.selectedCategory == category,
                    onClick = { viewModel.loadMemoriesByCategory(category) },
                    text = { Text(label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 记忆列表
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.memories.isEmpty()) {
            // 空状态
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "暂无记忆",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "与桌宠对话时会自动学习",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.memories) { memory ->
                    MemoryItem(
                        memory = memory,
                        onDelete = { viewModel.deleteMemory(memory) },
                        onUpdate = { updatedMemory -> viewModel.updateMemory(updatedMemory) }
                    )
                }
            }
        }
    }

    // 添加记忆对话框
    if (showAddDialog) {
        AddMemoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { key, value, category, importance ->
                viewModel.addMemory(key, value, category, importance)
                showAddDialog = false
            }
        )
    }

    // 清空记忆对话框
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空记忆") },
            text = { Text("确定要清空所有记忆吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllMemories()
                        showClearDialog = false
                    }
                ) {
                    Text("确定清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun MemoryItem(
    memory: MemoryEntity,
    onDelete: () -> Unit,
    onUpdate: (MemoryEntity) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 分类图标
            Icon(
                imageVector = when (memory.category) {
                    "preference" -> Icons.Default.Favorite
                    "habit" -> Icons.Default.Repeat
                    "relationship" -> Icons.Default.People
                    else -> Icons.Default.Memory
                },
                contentDescription = null,
                tint = when (memory.category) {
                    "preference" -> MaterialTheme.colorScheme.primary
                    "habit" -> MaterialTheme.colorScheme.secondary
                    "relationship" -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 记忆内容
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = memory.key,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = memory.value,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    // 重要程度
                    repeat(memory.importance) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // 访问次数
                    Text(
                        text = "访问 ${memory.accessCount} 次",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 更多选项
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("编辑") },
                        onClick = {
                            showEditDialog = true
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("删除") },
                        onClick = {
                            onDelete()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    )
                }
            }
        }
    }

    // 编辑对话框
    if (showEditDialog) {
        EditMemoryDialog(
            memory = memory,
            onDismiss = { showEditDialog = false },
            onConfirm = { updatedMemory ->
                onUpdate(updatedMemory)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun AddMemoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (key: String, value: String, category: String, importance: Int) -> Unit
) {
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("general") }
    var importance by remember { mutableIntStateOf(1) }

    val categoryOptions = listOf(
        "general" to "通用",
        "preference" to "偏好",
        "habit" to "习惯",
        "relationship" to "关系"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加记忆") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("记忆键") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("记忆值") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // 分类选择
                Text("分类", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoryOptions.forEach { (cat, label) ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(label) }
                        )
                    }
                }

                // 重要程度
                Text("重要程度", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..5).forEach { level ->
                        IconButton(
                            onClick = { importance = level },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (level <= importance) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (level <= importance)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(key, value, category, importance) },
                enabled = key.isNotBlank() && value.isNotBlank()
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun EditMemoryDialog(
    memory: MemoryEntity,
    onDismiss: () -> Unit,
    onConfirm: (MemoryEntity) -> Unit
) {
    var key by remember { mutableStateOf(memory.key) }
    var value by remember { mutableStateOf(memory.value) }
    var importance by remember { mutableIntStateOf(memory.importance) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑记忆") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("记忆键") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("记忆值") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // 重要程度
                Text("重要程度", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..5).forEach { level ->
                        IconButton(
                            onClick = { importance = level },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (level <= importance) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (level <= importance)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(memory.copy(key = key, value = value, importance = importance))
                },
                enabled = key.isNotBlank() && value.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

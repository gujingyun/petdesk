package com.petdesk.presentation.task

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
import com.petdesk.domain.model.Task
import com.petdesk.domain.model.TaskStatus
import com.petdesk.presentation.components.TaskProgressCard
import com.petdesk.presentation.viewmodel.ScheduledTaskViewModel
import com.petdesk.presentation.viewmodel.TaskExecutionState
import com.petdesk.presentation.viewmodel.TaskExecutionViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * 任务界面
 * 展示任务列表和任务执行状态
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    taskExecutionViewModel: TaskExecutionViewModel = hiltViewModel(),
    scheduledTaskViewModel: ScheduledTaskViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("即时任务", "定时任务")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题
        Text(
            text = "任务中心",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Tab 行
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                    icon = {
                        Icon(
                            imageVector = if (index == 0) Icons.Default.FlashOn else Icons.Default.Schedule,
                            contentDescription = null
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 根据 Tab 显示不同内容
        when (selectedTab) {
            0 -> InstantTaskTab(taskExecutionViewModel)
            1 -> ScheduledTaskTab(scheduledTaskViewModel)
        }
    }
}

@Composable
private fun InstantTaskTab(viewModel: TaskExecutionViewModel) {
    var userInput by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 任务输入区域
        item {
            TaskInputSection(
                userInput = userInput,
                onUserInputChange = { userInput = it },
                onSubmit = {
                    if (userInput.isNotBlank()) {
                        viewModel.startTaskExecution(userInput)
                        userInput = ""
                    }
                },
                isLoading = uiState.executionState is TaskExecutionState.Loading ||
                        uiState.executionState is TaskExecutionState.Planning
            )
        }

        // 任务执行状态卡片
        if (uiState.executionState !is TaskExecutionState.Idle) {
            item {
                TaskProgressCard(
                    uiState = uiState,
                    onConfirm = {
                        val plan = (uiState.executionState as? TaskExecutionState.Planned)?.plan
                        plan?.let { viewModel.confirmExecution(it) }
                    },
                    onCancel = { viewModel.cancelExecution() },
                    onRetry = { viewModel.retry() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 任务历史标题
        item {
            Text(
                text = "任务历史",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // 任务历史列表（示例数据）
        items(sampleTasks) { task ->
            TaskHistoryItem(task = task)
        }
    }
}

@Composable
private fun ScheduledTaskTab(viewModel: ScheduledTaskViewModel) {
    val state by viewModel.state.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 创建定时任务按钮
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                onClick = { showCreateDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAlarm,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "创建定时任务",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "设置任务在指定时间自动执行",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // 定时任务列表标题
        item {
            Text(
                text = "定时任务列表",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // 定时任务列表
        if (state.scheduledTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "暂无定时任务",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(state.scheduledTasks) { task ->
                ScheduledTaskItem(
                    task = task,
                    onExecuteNow = { viewModel.executeNow(task.id) },
                    onCancel = { viewModel.cancelScheduledTask(task.id) },
                    onDelete = { viewModel.deleteScheduledTask(task.id) }
                )
            }
        }
    }

    // 创建定时任务对话框
    if (showCreateDialog) {
        CreateScheduledTaskDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, description, scheduledTime ->
                viewModel.createScheduledTask(
                    title = title,
                    description = description,
                    scheduledAt = scheduledTime
                )
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun ScheduledTaskItem(
    task: Task,
    onExecuteNow: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 定时图标
            Icon(
                imageVector = Icons.Default.Alarm,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 任务信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "执行时间: ${dateFormat.format(Date(task.scheduledAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 立即执行按钮
            IconButton(onClick = onExecuteNow) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "立即执行",
                    tint = MaterialTheme.colorScheme.primary
                )
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
                        text = { Text("取消定时") },
                        onClick = {
                            onCancel()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Cancel, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("删除任务") },
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateScheduledTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, scheduledTime: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedHour by remember { mutableIntStateOf(9) }
    var selectedMinute by remember { mutableIntStateOf(0) }
    var selectedDateOffset by remember { mutableIntStateOf(0) } // 0=今天, 1=明天, 2=后天

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建定时任务") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("任务标题") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("任务描述（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                // 日期选择
                Text(
                    text = "执行日期",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("今天", "明天", "后天").forEachIndexed { index, label ->
                        FilterChip(
                            selected = selectedDateOffset == index,
                            onClick = { selectedDateOffset = index },
                            label = { Text(label) }
                        )
                    }
                }

                // 时间选择
                Text(
                    text = "执行时间",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 小时选择
                    OutlinedTextField(
                        value = String.format("%02d", selectedHour),
                        onValueChange = { value ->
                            val hour = value.toIntOrNull()
                            if (hour != null && hour in 0..23) {
                                selectedHour = hour
                            }
                        },
                        modifier = Modifier.width(64.dp),
                        singleLine = true
                    )
                    Text(":")

                    // 分钟选择
                    OutlinedTextField(
                        value = String.format("%02d", selectedMinute),
                        onValueChange = { value ->
                            val minute = value.toIntOrNull()
                            if (minute != null && minute in 0..59) {
                                selectedMinute = minute
                            }
                        },
                        modifier = Modifier.width(64.dp),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.DAY_OF_YEAR, selectedDateOffset)
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                        calendar.set(Calendar.MINUTE, selectedMinute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        onConfirm(title, description, calendar.timeInMillis)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("创建")
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
private fun TaskInputSection(
    userInput: String,
    onUserInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "创建即时任务",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            OutlinedTextField(
                value = userInput,
                onValueChange = onUserInputChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("描述您想要执行的任务...") },
                enabled = !isLoading,
                minLines = 2,
                maxLines = 4,
                trailingIcon = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 快捷短语按钮
                AssistChip(
                    onClick = { onUserInputChange("帮我设置一个明天早上9点的闹钟") },
                    label = { Text("设闹钟") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Alarm,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    enabled = !isLoading
                )

                AssistChip(
                    onClick = { onUserInputChange("打开微信") },
                    label = { Text("打开应用") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    enabled = !isLoading
                )

                AssistChip(
                    onClick = { onUserInputChange("帮我查一下明天天气") },
                    label = { Text("查天气") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Cloud,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    enabled = !isLoading
                )
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier.align(Alignment.End),
                enabled = userInput.isNotBlank() && !isLoading
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("执行任务")
            }
        }
    }
}

private val sampleTasks = listOf(
    TaskSampleItem(
        id = 1L,
        title = "打开微信",
        status = TaskStatus.COMPLETED,
        time = "10:30",
        description = "已成功打开微信"
    ),
    TaskSampleItem(
        id = 2L,
        title = "设置闹钟",
        status = TaskStatus.COMPLETED,
        time = "09:15",
        description = "已设置为明天早上7点"
    ),
    TaskSampleItem(
        id = 3L,
        title = "查询天气",
        status = TaskStatus.FAILED,
        time = "昨天",
        description = "网络连接失败"
    )
)

private data class TaskSampleItem(
    val id: Long,
    val title: String,
    val status: TaskStatus,
    val time: String,
    val description: String
)

@Composable
private fun TaskHistoryList() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sampleTasks) { task ->
            TaskHistoryItem(task = task)
        }
    }
}

@Composable
private fun TaskHistoryItem(task: TaskSampleItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 状态图标
            Icon(
                imageVector = when (task.status) {
                    TaskStatus.COMPLETED -> Icons.Default.CheckCircle
                    TaskStatus.FAILED -> Icons.Default.Error
                    TaskStatus.CANCELLED -> Icons.Default.Cancel
                    else -> Icons.Default.Schedule
                },
                contentDescription = null,
                tint = when (task.status) {
                    TaskStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                    TaskStatus.FAILED -> MaterialTheme.colorScheme.error
                    TaskStatus.CANCELLED -> MaterialTheme.colorScheme.onSurfaceVariant
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 任务信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 时间
            Text(
                text = task.time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

package com.petdesk.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petdesk.presentation.viewmodel.TaskExecutionProgress
import com.petdesk.presentation.viewmodel.TaskExecutionState
import com.petdesk.presentation.viewmodel.TaskExecutionUiState

/**
 * 任务执行进度卡片
 */
@Composable
fun TaskProgressCard(
    uiState: TaskExecutionUiState,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 状态标题
            StatusHeader(uiState.executionState)

            // 进度条
            if (uiState.executionState is TaskExecutionState.Executing) {
                ProgressSection(uiState.progress)
            }

            // 步骤列表
            if (uiState.executionState is TaskExecutionState.Executing ||
                uiState.executionState is TaskExecutionState.Success ||
                uiState.executionState is TaskExecutionState.Failure
            ) {
                StepsList(uiState)
            }

            // 操作按钮
            ActionButtons(
                state = uiState.executionState,
                canConfirm = uiState.canConfirm,
                canCancel = uiState.canCancel,
                canRetry = uiState.canRetry,
                onConfirm = onConfirm,
                onCancel = onCancel,
                onRetry = onRetry
            )

            // 错误信息
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun StatusHeader(state: TaskExecutionState) {
    val (title, subtitle, color) = when (state) {
        is TaskExecutionState.Idle ->
            Triple("准备就绪", "等待任务开始", MaterialTheme.colorScheme.onSurface)

        is TaskExecutionState.Loading ->
            Triple("加载中", "正在初始化...", MaterialTheme.colorScheme.primary)

        is TaskExecutionState.Planning ->
            Triple("分析中", state.message, MaterialTheme.colorScheme.primary)

        is TaskExecutionState.Planned ->
            Triple("待确认", "请确认是否执行此任务", MaterialTheme.colorScheme.secondary)

        is TaskExecutionState.Executing ->
            Triple("执行中", "正在执行任务步骤", MaterialTheme.colorScheme.primary)

        is TaskExecutionState.Success ->
            Triple("完成", "任务执行成功", MaterialTheme.colorScheme.onPrimary)

        is TaskExecutionState.Failure ->
            Triple("失败", state.errorMessage, MaterialTheme.colorScheme.error)

        is TaskExecutionState.Cancelled ->
            Triple("已取消", "任务已取消执行", MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = color
    )

    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ProgressSection(progress: TaskExecutionProgress) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 进度条
        LinearProgressIndicator(
            progress = progress.progressPercentage,
            modifier = Modifier.fillMaxWidth()
        )

        // 进度信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "步骤 ${progress.currentStep}/${progress.totalSteps}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "${(progress.progressPercentage * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // 当前步骤描述
        if (progress.currentStepDescription.isNotEmpty()) {
            Text(
                text = progress.currentStepDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 时间信息
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "已用：${formatTime(progress.elapsedTime)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (progress.estimatedRemainingTime > 0) {
                Text(
                    text = "预计剩余：${formatTime(progress.estimatedRemainingTime)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StepsList(uiState: TaskExecutionUiState) {
    val state = uiState.executionState
    val stepResults = when (state) {
        is TaskExecutionState.Executing -> state.stepResults
        is TaskExecutionState.Success -> state.stepResults
        is TaskExecutionState.Failure -> state.stepResults
        else -> emptyList()
    }

    if (stepResults.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "执行步骤",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )

        stepResults.forEachIndexed { index, result ->
            StepItem(
                stepNumber = index + 1,
                description = result.output.take(50),
                isSuccess = result.success,
                isCurrent = uiState.executionState is TaskExecutionState.Executing &&
                        index == uiState.progress.currentStep - 1
            )
        }
    }
}

@Composable
private fun StepItem(
    stepNumber: Int,
    description: String,
    isSuccess: Boolean,
    isCurrent: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 步骤编号/状态图标
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isCurrent) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = if (isSuccess) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isSuccess) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // 步骤描述
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = if (isCurrent) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionButtons(
    state: TaskExecutionState,
    canConfirm: Boolean,
    canCancel: Boolean,
    canRetry: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 确认按钮
        if (canConfirm && state is TaskExecutionState.Planned) {
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f)
            ) {
                Text("确认执行")
            }
        }

        // 重试按钮
        if (canRetry && (state is TaskExecutionState.Failure ||
                    state is TaskExecutionState.Cancelled)
        ) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.weight(1f)
            ) {
                Text("重试")
            }
        }

        // 取消按钮
        if (canCancel && (state is TaskExecutionState.Executing ||
                    state is TaskExecutionState.Planned)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("取消")
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val seconds = millis / 1000
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds / 3600}h ${seconds % 3600 / 60}m"
    }
}

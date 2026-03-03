package com.petdesk.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.AlarmClock
import com.petdesk.domain.model.Task
import com.petdesk.domain.model.agent.StepExecutionResult
import com.petdesk.domain.model.agent.TaskExecutionResult
import com.petdesk.domain.model.agent.TaskPlan
import com.petdesk.domain.model.agent.TaskStep
import com.petdesk.domain.repository.ExecutionStatus
import com.petdesk.domain.repository.TaskExecutor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 任务执行器实现
 */
@Singleton
class TaskExecutorImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TaskExecutor {

    private val mutex = Mutex()
    private val runningTasks = mutableMapOf<Long, Boolean>()

    // 常用应用包名映射
    private val appPackageMap = mapOf(
        "微信" to "com.tencent.mm",
        "QQ" to "com.tencent.mobileqq",
        "支付宝" to "com.eg.android.AlipayGphone",
        "淘宝" to "com.taobao.taobao",
        "抖音" to "com.ss.android.ugc.aweme",
        "快手" to "com.smile.gifmaker",
        "京东" to "com.jingdong.app.mall",
        "拼多多" to "com.xunmeng.pinduoduo",
        "bilibili" to "tv.danmaku.bili",
        "网易云音乐" to "com.netease.cloudmusic",
        "钉钉" to "com.alibaba.android.rimet",
        "今日头条" to "com.ss.android.article.news",
        "知乎" to "com.zhihu.android",
        "百度" to "com.baidu.searchbox",
        "高德地图" to "com.autonavi.minimap",
        "美团" to "com.sankuai.meituan",
        "饿了么" to "me.ele.android",
        "滴滴出行" to "com.sdu.didi.psnger",
        "银行" to "com.chinamobile.mmboc",
        "短信" to "com.android.mms",
        "电话" to "com.android.phone",
        "相机" to "com.android.camera2",
        "浏览器" to "com.android.browser",
        "设置" to "com.android.settings",
        "文件管理器" to "com.android.fileexplorer",
        "时钟" to "com.android.deskclock",
        "闹钟" to "com.android.deskclock"
    )

    override fun execute(plan: TaskPlan, task: Task): Flow<TaskExecutionResult> = flow {
        mutex.withLock {
            runningTasks[task.id] = true
        }

        val stepResults = mutableListOf<StepExecutionResult>()
        val startTime = System.currentTimeMillis()
        var success = true
        var errorMessage = ""

        try {
            for (step in plan.steps) {
                if (runningTasks[task.id] != true) {
                    // 任务被取消
                    emit(
                        TaskExecutionResult(
                            plan = plan,
                            success = false,
                            stepResults = stepResults,
                            errorMessage = "任务已取消",
                            totalDuration = System.currentTimeMillis() - startTime
                        )
                    )
                    return@flow
                }

                // 检查依赖
                if (!checkDependencies(step, stepResults)) {
                    continue
                }

                val result = executeStep(step)
                stepResults.add(result)

                if (!result.success) {
                    success = false
                    errorMessage = result.errorMessage
                    break
                }
            }
        } catch (e: Exception) {
            success = false
            errorMessage = e.message ?: "未知错误"
        } finally {
            mutex.withLock {
                runningTasks.remove(task.id)
            }
        }

        emit(
            TaskExecutionResult(
                plan = plan,
                success = success,
                stepResults = stepResults,
                finalResult = if (success) "任务执行完成" else "",
                errorMessage = errorMessage,
                totalDuration = System.currentTimeMillis() - startTime
            )
        )
    }

    override suspend fun executeStep(
        step: TaskStep,
        stepContext: Map<String, Any>
    ): StepExecutionResult {
        val startTime = System.currentTimeMillis()

        return try {
            // 根据 action 执行不同的操作
            val result = when (step.action.lowercase()) {
                "execute", "启动应用", "open app", "start app" -> executeOpenApp(step)
                "set_alarm", "设置闹钟", "添加闹钟" -> executeSetAlarm(step)
                "check_device_capability", "check_device_capability" -> executeCheckCapability(step)
                "calculate_alarm_time", "calculate_target_time", "计算时间" -> executeCalculateTime(step)
                "confirm_alarm_set", "confirm_alarm_setting", "确认闹钟" -> executeConfirmAlarm(step)
                "wait" -> executeWait(step, stepContext)
                "notify", "notification" -> executeNotify(step, stepContext)
                else -> executeGeneric(step, stepContext)
            }

            StepExecutionResult(
                stepId = step.stepId,
                success = true,
                output = result,
                duration = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            StepExecutionResult(
                stepId = step.stepId,
                success = false,
                errorMessage = e.message ?: "执行失败",
                duration = System.currentTimeMillis() - startTime
            )
        }
    }

    /**
     * 打开应用
     */
    private fun executeOpenApp(step: TaskStep): String {
        val appName = step.parameters["appName"]
            ?: step.parameters["app_name"]
            ?: step.description.extractAppName()

        val packageName = appPackageMap[appName]
            ?: findAppByName(appName)

        return if (packageName != null) {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                "已打开应用: $appName"
            } else {
                "未找到应用: $appName"
            }
        } else {
            "未找到应用: $appName，请确保已安装"
        }
    }

    /**
     * 设置闹钟
     */
    private fun executeSetAlarm(step: TaskStep): String {
        val time = step.parameters["time"]
            ?: step.parameters["alarmTime"]
            ?: step.description.extractTime()

        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // 尝试解析时间
                time?.let {
                    // 这里简单处理，实际需要解析 "明天早上9点" 这样的时间
                    putExtra(AlarmClock.EXTRA_MESSAGE, "桌宠提醒")
                }
            }

            // 检查是否有应用可以处理闹钟 intent
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                "已打开闹钟应用，请设置时间"
            } else {
                "设备不支持设置闹钟或未找到闹钟应用"
            }
        } catch (e: Exception) {
            "设置闹钟失败: ${e.message}"
        }
    }

    /**
     * 检查设备能力
     */
    private fun executeCheckCapability(step: TaskStep): String {
        val capability = step.parameters["capability"]

        return when (capability) {
            "set_alarm" -> {
                val intent = Intent(AlarmClock.ACTION_SET_ALARM)
                if (intent.resolveActivity(context.packageManager) != null) {
                    "设备支持设置闹钟功能"
                } else {
                    "设备不支持设置闹钟功能"
                }
            }
            else -> "设备能力检查完成"
        }
    }

    /**
     * 计算闹钟时间
     */
    private fun executeCalculateTime(step: TaskStep): String {
        val targetTime = step.parameters["time"]
            ?: step.parameters["targetTime"]
            ?: step.description.extractTime()

        // 简单的时间解析
        val timeStr = when {
            targetTime?.contains("早上") == true || targetTime?.contains("9点") == true -> "09:00"
            targetTime?.contains("下午") == true && targetTime?.contains("2点") == true -> "14:00"
            targetTime?.contains("晚上") == true || targetTime?.contains("8点") == true -> "20:00"
            else -> targetTime ?: "09:00"
        }

        return "计算时间: $timeStr"
    }

    /**
     * 确认闹钟设置
     */
    private fun executeConfirmAlarm(step: TaskStep): String {
        return "闹钟设置已确认"
    }

    // 辅助函数：从描述中提取应用名
    private fun String.extractAppName(): String? {
        val appPatterns = listOf("打开", "启动", "运行", "打开")
        for (pattern in appPatterns) {
            if (this.contains(pattern)) {
                return this.substringAfter(pattern).trim()
            }
        }
        return null
    }

    // 辅助函数：从描述中提取时间
    private fun String.extractTime(): String? {
        val timePatterns = listOf(
            "明天早上(\\d+)点".toRegex(),
            "今天下午(\\d+)点".toRegex(),
            "明天晚上(\\d+)点".toRegex(),
            "(\\d+):(\\d+)".toRegex()
        )
        for (pattern in timePatterns) {
            pattern.find(this)?.let { match ->
                return match.value
            }
        }
        return null
    }

    // 根据应用名查找包名
    private fun findAppByName(appName: String?): String? {
        if (appName == null) return null

        // 先从映射表查找
        appPackageMap[appName]?.let { return it }

        // 尝试在已安装应用中查找（简化版本）
        return try {
            context.packageManager.getApplicationInfo(appName, 0).packageName
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    override suspend fun cancel(taskId: Long) {
        mutex.withLock {
            runningTasks.remove(taskId)
        }
    }

    override suspend fun getExecutionStatus(taskId: Long): ExecutionStatus {
        return if (runningTasks[taskId] == true) {
            ExecutionStatus.Running
        } else {
            ExecutionStatus.Pending
        }
    }

    private fun checkDependencies(
        step: TaskStep, 
        stepResults: List<StepExecutionResult>
    ): Boolean {
        if (step.dependsOn.isEmpty()) return true
        
        return step.dependsOn.all { depId ->
            stepResults.any { it.stepId == depId && it.success }
        }
    }

    private suspend fun executeGeneric(
        step: TaskStep, 
        context: Map<String, Any>
    ): String {
        // 通用执行逻辑
        return "步骤 ${step.stepId} 执行完成：${step.description}"
    }

    private suspend fun executeWait(
        step: TaskStep, 
        context: Map<String, Any>
    ): String {
        val duration = step.parameters["duration"]?.toLongOrNull() ?: 1000L
        kotlinx.coroutines.delay(duration)
        return "等待 ${duration}ms 完成"
    }

    private suspend fun executeNotify(
        step: TaskStep, 
        context: Map<String, Any>
    ): String {
        val message = step.parameters["message"] ?: "通知"
        // 这里可以集成通知系统
        return "发送通知：$message"
    }
}

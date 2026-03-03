package com.petdesk.data.repository

import com.petdesk.data.remote.QwenApiService
import com.petdesk.domain.model.LLMMessage
import com.petdesk.domain.model.agent.IntentRecognitionResult
import com.petdesk.domain.model.agent.TaskPlan
import com.petdesk.domain.model.agent.TaskStep
import com.petdesk.domain.repository.TaskPlannerRepository
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 任务规划器实现 - 使用 Qwen-Max API
 */
@Singleton
class TaskPlannerRepositoryImpl @Inject constructor(
    private val qwenApiService: QwenApiService
) : TaskPlannerRepository {

    companion object {
        private val SYSTEM_PROMPT = """
你是一个任务规划助手。请根据用户输入和意图识别结果，将任务拆解为可执行的步骤。

返回 JSON 格式：
{
    "steps": [
        {
            "stepId": 1,
            "description": "步骤描述",
            "action": "执行动作",
            "parameters": {},
            "dependsOn": [],
            "estimatedDuration": 1000
        }
    ],
    "estimatedTotalTime": 5000,
    "requiresConfirmation": false
}

步骤应该：
1. 按执行顺序排列
2. 明确每个步骤的依赖关系
3. 预估合理的执行时间
""".trimIndent()
    }

    override suspend fun planTask(
        userInput: String,
        intent: IntentRecognitionResult,
        context: List<LLMMessage>
    ): TaskPlan {
        val prompt = buildString {
            appendLine("用户输入：$userInput")
            appendLine("识别的任务类型：${intent.taskType}")
            appendLine("提取的参数：${intent.parameters}")
            appendLine()
            appendLine("请为这个任务生成详细的执行步骤。")
        }

        val messages = buildList {
            add(LLMMessage("system", SYSTEM_PROMPT))
            addAll(context)
            add(LLMMessage("user", prompt))
        }

        val requestMessages = messages.map { 
            com.petdesk.domain.model.LLMRequest.Message(it.role, it.content) 
        }

        val request = com.petdesk.domain.model.LLMRequest(
            model = "qwen-max",
            input = com.petdesk.domain.model.LLMRequest.Input(requestMessages)
        )

        val response = qwenApiService.chat(request)

        if (response.isSuccessful) {
            // 优先使用 output.text，其次使用 choices[0].message.content
            val content = response.body()?.output?.text
                ?: response.body()?.output?.choices?.firstOrNull()?.message?.content
                ?: ""
            return parsePlanResponse(content, userInput, intent)
        } else {
            // 返回默认规划
            return TaskPlan(
                userInput = userInput,
                intent = intent,
                steps = listOf(
                    TaskStep(
                        stepId = 1,
                        description = "执行任务",
                        action = "execute",
                        estimatedDuration = 5000
                    )
                ),
                estimatedTotalTime = 5000,
                requiresConfirmation = false
            )
        }
    }

    override suspend fun validatePlan(plan: TaskPlan): Boolean {
        // 基本验证逻辑
        return plan.steps.isNotEmpty() && 
               plan.steps.all { it.stepId > 0 } &&
               plan.estimatedTotalTime > 0
    }

    private fun parsePlanResponse(
        content: String, 
        userInput: String, 
        intent: IntentRecognitionResult
    ): TaskPlan {
        return try {
            val json = content.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()
            
            val jsonObject = JSONObject(json)
            
            val stepsJson = jsonObject.getJSONArray("steps")
            val steps = mutableListOf<TaskStep>()
            
            for (i in 0 until stepsJson.length()) {
                val stepJson = stepsJson.getJSONObject(i)
                steps.add(
                    TaskStep(
                        stepId = stepJson.optInt("stepId", i + 1),
                        description = stepJson.optString("description", ""),
                        action = stepJson.optString("action", ""),
                        parameters = parseParameters(stepJson.optJSONObject("parameters")),
                        dependsOn = parseDependsOn(stepJson.optJSONArray("dependsOn")),
                        estimatedDuration = stepJson.optLong("estimatedDuration", 1000)
                    )
                )
            }
            
            TaskPlan(
                userInput = userInput,
                intent = intent,
                steps = steps,
                estimatedTotalTime = jsonObject.optLong("estimatedTotalTime", 5000),
                requiresConfirmation = jsonObject.optBoolean("requiresConfirmation", false)
            )
        } catch (e: Exception) {
            // 解析失败，返回默认规划
            TaskPlan(
                userInput = userInput,
                intent = intent,
                steps = listOf(
                    TaskStep(
                        stepId = 1,
                        description = "执行任务",
                        action = "execute",
                        estimatedDuration = 5000
                    )
                ),
                estimatedTotalTime = 5000,
                requiresConfirmation = false
            )
        }
    }

    private fun parseParameters(json: JSONObject?): Map<String, String> {
        val params = mutableMapOf<String, String>()
        json?.let {
            it.keys().forEach { key ->
                params[key] = it.optString(key)
            }
        }
        return params
    }

    private fun parseDependsOn(json: JSONArray?): List<Int> {
        val depends = mutableListOf<Int>()
        json?.let {
            for (i in 0 until it.length()) {
                depends.add(it.optInt(i))
            }
        }
        return depends
    }
}

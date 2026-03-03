package com.petdesk.data.repository

import com.petdesk.data.remote.QwenApiService
import com.petdesk.domain.model.LLMMessage
import com.petdesk.domain.model.TaskType
import com.petdesk.domain.model.agent.IntentRecognitionResult
import com.petdesk.domain.repository.IntentRecognizerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 意图识别器实现 - 使用 Qwen-Max API
 */
@Singleton
class IntentRecognizerRepositoryImpl @Inject constructor(
    private val qwenApiService: QwenApiService
) : IntentRecognizerRepository {

    companion object {
        private val SYSTEM_PROMPT = """
你是一个任务意图识别助手。请分析用户输入，识别其意图并返回 JSON 格式的结果。

可用的任务类型：
- CHAT: 聊天对话
- REMINDER: 提醒/定时任务
- APP_CONTROL: 应用控制（打开/关闭应用）
- FILE_OPERATION: 文件操作（创建/删除/移动文件）
- CUSTOM: 自定义任务

请返回以下 JSON 格式：
{
    "taskType": "任务类型",
    "confidence": 0.95,
    "parameters": {
        "key": "value"
    }
}

只返回 JSON，不要有其他内容。
""".trimIndent()
    }

    override suspend fun recognizeIntent(
        userInput: String,
        context: List<LLMMessage>
    ): IntentRecognitionResult {
        val messages = buildList {
            add(LLMMessage("system", SYSTEM_PROMPT))
            addAll(context)
            add(LLMMessage("user", userInput))
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
            return parseIntentResponse(content, userInput)
        } else {
            // 返回默认结果
            return IntentRecognitionResult(
                taskType = TaskType.CUSTOM,
                confidence = 0.5f,
                parameters = emptyMap(),
                rawResponse = "Error: ${response.code()}"
            )
        }
    }

    override fun recognizeIntentStream(
        userInput: String,
        context: List<LLMMessage>
    ): Flow<String> = flow {
        val messages = buildList {
            add(LLMMessage("system", SYSTEM_PROMPT))
            addAll(context)
            add(LLMMessage("user", userInput))
        }

        val requestMessages = messages.map { 
            com.petdesk.domain.model.LLMRequest.Message(it.role, it.content) 
        }

        val request = com.petdesk.domain.model.LLMRequest(
            model = "qwen-max",
            input = com.petdesk.domain.model.LLMRequest.Input(requestMessages)
        )

        val response = qwenApiService.chatStream(request)
        
        if (response.isSuccessful) {
            response.body()?.let { body ->
                val source = body.source()
                while (!source.exhausted()) {
                    val line = source.readUtf8Line()
                    if (line != null && line.startsWith("data: ")) {
                        val json = line.removePrefix("data: ").trim()
                        if (json.isNotEmpty() && json != "[DONE]") {
                            emit(parseStreamContent(json))
                        }
                    }
                }
            }
        }
    }

    private fun parseIntentResponse(content: String, userInput: String): IntentRecognitionResult {
        return try {
            val json = content.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()
            
            val jsonObject = JSONObject(json)
            val taskTypeStr = jsonObject.optString("taskType", "CUSTOM")
            val taskType = try {
                TaskType.valueOf(taskTypeStr.uppercase())
            } catch (e: Exception) {
                TaskType.CUSTOM
            }
            
            val confidence = jsonObject.optDouble("confidence", 0.5).toFloat()
            
            val parameters = mutableMapOf<String, String>()
            jsonObject.optJSONObject("parameters")?.let { params ->
                params.keys().forEach { key ->
                    parameters[key] = params.optString(key)
                }
            }
            
            IntentRecognitionResult(
                taskType = taskType,
                confidence = confidence,
                parameters = parameters,
                rawResponse = content
            )
        } catch (e: Exception) {
            // 解析失败，返回默认结果
            IntentRecognitionResult(
                taskType = TaskType.CUSTOM,
                confidence = 0.3f,
                parameters = emptyMap(),
                rawResponse = content
            )
        }
    }

    private fun parseStreamContent(json: String): String {
        return try {
            val contentStart = json.indexOf("\"content\":\"")
            if (contentStart != -1) {
                val contentEnd = json.indexOf("\"", contentStart + 11)
                if (contentEnd != -1) {
                    json.substring(contentStart + 11, contentEnd)
                } else ""
            } else ""
        } catch (e: Exception) {
            ""
        }
    }
}

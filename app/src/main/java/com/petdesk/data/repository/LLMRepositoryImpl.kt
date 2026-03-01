package com.petdesk.data.remote

import android.util.Log
import com.petdesk.domain.model.LLMMessage
import com.petdesk.domain.model.LLMRequest
import com.petdesk.domain.repository.LLMRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException

private const val TAG = "LLMRepository"

/**
 * LLM 仓库实现 - 通义千问
 */
class LLMRepositoryImpl(
    private val qwenApiService: QwenApiService,
    private val apiKey: String
) : LLMRepository {
    
    companion object {
        private const val BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/"
    }
    
    override fun chatStream(
        messages: List<LLMMessage>,
        systemPrompt: String?
    ): Flow<String> = flow {
        try {
            Log.d(TAG, "chatStream called with ${messages.size} messages")

            val requestMessages = buildList {
                systemPrompt?.let {
                    add(LLMRequest.Message("system", it))
                }
                addAll(messages.map { LLMRequest.Message(it.role, it.content) })
            }

            Log.d(TAG, "Sending request to Qwen API...")

            val request = LLMRequest(
                model = "qwen-max",
                input = LLMRequest.Input(requestMessages),
                stream = true
            )

            val response = qwenApiService.chatStream(request)

            Log.d(TAG, "Response code: ${response.code()}")

            if (response.isSuccessful) {
                response.body()?.let { body ->
                    Log.d(TAG, "Response body received, reading stream...")
                    emitAll(readStream(body))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Error: ${response.code()} - ${response.message()}, body: $errorBody")
                throw HttpException(response)
            }
        } catch (e: IOException) {
            throw e
        } catch (e: Exception) {
            throw e
        }
    }
    
    private suspend fun readStream(body: ResponseBody): Flow<String> = flow {
        body.source().use { source ->
            val buffer = StringBuilder()
            var lineCount = 0
            while (!source.exhausted()) {
                val chunk = source.readUtf8Line()
                lineCount++
                if (chunk != null && chunk.isNotBlank()) {
                    Log.d(TAG, "Chunk $lineCount: ${chunk.take(100)}")
                    // 通义千问可能返回 data: 前缀或直接返回 JSON
                    val json = if (chunk.startsWith("data: ")) {
                        chunk.removePrefix("data: ").trim()
                    } else {
                        chunk.trim()
                    }
                    if (json.isNotEmpty() && json != "[DONE]") {
                        // 解析 JSON 数据，提取 content
                        val content = parseContent(json)
                        Log.d(TAG, "Parsed content: '$content'")
                        if (content.isNotEmpty()) {
                            emit(content)
                        }
                    }
                }
            }
            Log.d(TAG, "Total lines read: $lineCount")
        }
    }
    
    private fun parseContent(json: String): String {
        // 通义千问流式响应格式: {"output": {"text": "..."}} 或 {"output": {"choices": [{"message": {"content": "..."}}]}}
        return try {
            // 先尝试查找 "text":"
            var content = extractJsonValue(json, "text")
            // 如果没找到，尝试 "content":"
            if (content.isEmpty()) {
                content = extractJsonValue(json, "content")
            }
            content
        } catch (e: Exception) {
            ""
        }
    }

    private fun extractJsonValue(json: String, key: String): String {
        val keyWithQuote = "\"$key\":\""
        val contentStart = json.indexOf(keyWithQuote)
        if (contentStart != -1) {
            val contentBegin = contentStart + keyWithQuote.length
            // 找到结束引号（需要处理转义字符）
            var contentEnd = contentBegin
            while (contentEnd < json.length) {
                val c = json[contentEnd]
                if (c == '"' && json.getOrNull(contentEnd - 1) != '\\') {
                    break
                }
                contentEnd++
            }
            var content = json.substring(contentBegin, contentEnd)
            // 处理转义字符
            content = content.replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\\\", "\\")
            return content
        }
        return ""
    }
    
    override suspend fun chat(
        messages: List<LLMMessage>,
        systemPrompt: String?
    ): String {
        val requestMessages = buildList {
            systemPrompt?.let {
                add(LLMRequest.Message("system", it))
            }
            addAll(messages.map { LLMRequest.Message(it.role, it.content) })
        }
        
        val request = LLMRequest(
            model = "qwen-max",
            input = LLMRequest.Input(requestMessages)
        )
        
        val response = qwenApiService.chat(request)
        
        if (response.isSuccessful) {
            return response.body()?.output?.choices?.firstOrNull()?.message?.content ?: ""
        } else {
            throw HttpException(response)
        }
    }
}

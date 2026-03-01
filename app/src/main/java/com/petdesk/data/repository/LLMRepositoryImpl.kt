package com.petdesk.data.remote

import com.petdesk.domain.model.LLMMessage
import com.petdesk.domain.model.LLMRequest
import com.petdesk.domain.repository.LLMRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.IOException

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
            val requestMessages = buildList {
                systemPrompt?.let {
                    add(LLMRequest.Message("system", it))
                }
                addAll(messages.map { LLMRequest.Message(it.role, it.content) })
            }
            
            val request = LLMRequest(
                model = "qwen-max",
                input = LLMRequest.Input(requestMessages),
                parameters = LLMRequest.Parameters(
                    incremental_output = true
                )
            )
            
            val response = qwenApiService.chatStream(request)
            
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    emitAll(readStream(body))
                }
            } else {
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
            while (!source.exhausted()) {
                val chunk = source.readUtf8Line()
                if (chunk != null && chunk.startsWith("data: ")) {
                    val json = chunk.removePrefix("data: ").trim()
                    if (json.isNotEmpty() && json != "[DONE]") {
                        // 解析 SSE 数据，提取 content
                        emit(parseContent(json))
                    }
                }
            }
        }
    }
    
    private fun parseContent(json: String): String {
        // 简化的 JSON 解析，实际应该使用 JSON 库
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

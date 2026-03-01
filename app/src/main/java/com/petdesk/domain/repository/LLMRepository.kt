package com.petdesk.domain.repository

import com.petdesk.domain.model.LLMMessage
import kotlinx.coroutines.flow.Flow

/**
 * LLM 仓库接口
 */
interface LLMRepository {
    
    /**
     * 流式对话
     * @param messages 对话历史
     * @param systemPrompt 系统提示词
     * @return 流式返回的文本片段
     */
    fun chatStream(
        messages: List<LLMMessage>,
        systemPrompt: String? = null
    ): Flow<String>
    
    /**
     * 普通对话（一次性返回）
     * @param messages 对话历史
     * @param systemPrompt 系统提示词
     * @return 完整的回复文本
     */
    suspend fun chat(
        messages: List<LLMMessage>,
        systemPrompt: String? = null
    ): String
}

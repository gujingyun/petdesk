package com.petdesk.domain.repository

import com.petdesk.domain.model.LLMMessage
import com.petdesk.domain.model.agent.IntentRecognitionResult
import kotlinx.coroutines.flow.Flow

/**
 * 意图识别仓库接口
 */
interface IntentRecognizerRepository {
    
    /**
     * 识别用户意图
     * @param userInput 用户输入
     * @param context 上下文信息（可选）
     * @return 意图识别结果
     */
    suspend fun recognizeIntent(
        userInput: String,
        context: List<LLMMessage> = emptyList()
    ): IntentRecognitionResult
    
    /**
     * 流式意图识别
     */
    fun recognizeIntentStream(
        userInput: String,
        context: List<LLMMessage> = emptyList()
    ): Flow<String>
}

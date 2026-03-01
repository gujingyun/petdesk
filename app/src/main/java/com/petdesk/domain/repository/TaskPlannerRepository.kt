package com.petdesk.domain.repository

import com.petdesk.domain.model.LLMMessage
import com.petdesk.domain.model.agent.TaskPlan

/**
 * 任务规划器仓库接口
 */
interface TaskPlannerRepository {
    
    /**
     * 规划任务步骤
     * @param userInput 用户输入
     * @param intent 意图识别结果
     * @param context 上下文信息
     * @return 任务规划结果
     */
    suspend fun planTask(
        userInput: String,
        intent: com.petdesk.domain.model.agent.IntentRecognitionResult,
        context: List<LLMMessage> = emptyList()
    ): TaskPlan
    
    /**
     * 验证任务规划
     */
    suspend fun validatePlan(plan: TaskPlan): Boolean
}

package com.petdesk.domain.model

/**
 * LLM 请求模型
 */
data class LLMRequest(
    val model: String = "qwen-max",
    val input: Input,
    val parameters: Parameters? = null
) {
    data class Input(
        val messages: List<Message>
    )
    
    data class Message(
        val role: String,
        val content: String
    )
    
    data class Parameters(
        val result_format: String = "message",
        val incremental_output: Boolean = true,
        val temperature: Float = 0.7f,
        val top_p: Float = 0.8f,
        val max_tokens: Int = 2000,
        val stop: List<String>? = null
    )
}

/**
 * LLM 响应模型
 */
data class LLMResponse(
    val output: Output?,
    val usage: Usage?,
    val request_id: String?
) {
    data class Output(
        val text: String? = null,
        val finish_reason: String? = null,
        val choices: List<Choice>? = null
    ) {
        data class Choice(
            val finish_reason: String?,
            val message: Message?
        ) {
            data class Message(
                val role: String?,
                val content: String?
            )
        }
    }
    
    data class Usage(
        val input_tokens: Int?,
        val output_tokens: Int?,
        val total_tokens: Int?
    )
}

/**
 * 对话消息模型（本地使用）
 */
data class LLMMessage(
    val role: String, // "user" | "assistant" | "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

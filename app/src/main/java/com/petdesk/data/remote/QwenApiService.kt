package com.petdesk.data.remote

import com.petdesk.domain.model.LLMMessage
import com.petdesk.domain.model.LLMRequest
import com.petdesk.domain.model.LLMResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Streaming

/**
 * 通义千问 API 服务接口
 */
interface QwenApiService {
    
    /**
     * 发送对话请求（流式）
     */
    @Streaming
    @POST("api/v1/services/aigc/text-generation/generation")
    suspend fun chatStream(
        @Body request: LLMRequest
    ): Response<okhttp3.ResponseBody>
    
    /**
     * 发送对话请求（非流式）
     */
    @POST("api/v1/services/aigc/text-generation/generation")
    suspend fun chat(
        @Body request: LLMRequest
    ): Response<LLMResponse>
}

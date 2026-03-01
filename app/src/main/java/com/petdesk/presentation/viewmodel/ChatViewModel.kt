package com.petdesk.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petdesk.domain.model.LLMMessage
import com.petdesk.domain.repository.ConversationRepository
import com.petdesk.domain.repository.LLMRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 聊天界面 ViewModel
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val llmRepository: LLMRepository,
    private val conversationRepository: ConversationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val conversationId = System.currentTimeMillis().toString()
    
    init {
        loadConversationHistory()
    }
    
    private fun loadConversationHistory() {
        viewModelScope.launch {
            // 从数据库加载历史对话
            // conversationRepository.getConversation(conversationId)?.let { ... }
        }
    }
    
    fun sendMessage(content: String) {
        if (content.isBlank()) return
        
        val userMessage = ChatMessage(
            id = System.currentTimeMillis().toString(),
            role = "user",
            content = content,
            timestamp = System.currentTimeMillis()
        )
        
        _messages.value = _messages.value + userMessage
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                val messages = _messages.value.map { 
                    LLMMessage(role = it.role, content = it.content) 
                }
                
                val assistantMessage = ChatMessage(
                    id = (System.currentTimeMillis() + 1).toString(),
                    role = "assistant",
                    content = "",
                    timestamp = System.currentTimeMillis(),
                    isStreaming = true
                )
                
                _messages.value = _messages.value + assistantMessage
                
                llmRepository.chatStream(messages).collect { chunk ->
                    val currentMessages = _messages.value
                    val lastMessage = currentMessages.lastOrNull()
                    if (lastMessage?.isStreaming == true) {
                        _messages.value = currentMessages.dropLast(1) + lastMessage.copy(
                            content = lastMessage.content + chunk
                        )
                    }
                }
                
                // 流式传输完成
                val finalMessages = _messages.value
                val lastMessage = finalMessages.lastOrNull()
                if (lastMessage?.isStreaming == true) {
                    _messages.value = finalMessages.dropLast(1) + lastMessage.copy(
                        isStreaming = false
                    )
                }
                
                // 保存到数据库
                // conversationRepository.saveConversation(...)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "发送失败"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearConversation() {
        _messages.value = emptyList()
        viewModelScope.launch {
            // conversationRepository.clearConversation(conversationId)
        }
    }
}

data class ChatUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ChatMessage(
    val id: String,
    val role: String,
    val content: String,
    val timestamp: Long,
    val isStreaming: Boolean = false
)

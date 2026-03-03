package com.petdesk.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petdesk.data.local.entity.ConversationEntity
import com.petdesk.data.local.entity.MemoryEntity
import com.petdesk.domain.model.LLMMessage
import com.petdesk.domain.repository.ConversationRepository
import com.petdesk.domain.repository.LLMRepository
import com.petdesk.domain.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
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
    private val conversationRepository: ConversationRepository,
    private val memoryRepository: MemoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val userId = 1L
    private var conversationId: Long = System.currentTimeMillis()

    init {
        loadConversationHistory()
    }

    private fun loadConversationHistory() {
        viewModelScope.launch {
            // 从数据库加载最近的历史对话
            try {
                val recentConversations = conversationRepository.getRecentConversations(userId, 50).first()
                if (recentConversations.isNotEmpty()) {
                    // 按时间正序排列
                    val sortedConversations = recentConversations.sortedBy { it.timestamp }

                    val loadedMessages = sortedConversations.mapNotNull { conv ->
                        when (conv.messageType) {
                            0 -> if (conv.message.isNotBlank()) ChatMessage(
                                id = conv.id.toString(),
                                role = "user",
                                content = conv.message,
                                timestamp = conv.timestamp
                            ) else null
                            1 -> if (conv.response.isNotBlank()) ChatMessage(
                                id = conv.id.toString(),
                                role = "assistant",
                                content = conv.response,
                                timestamp = conv.timestamp
                            ) else null
                            else -> null
                        }
                    }
                    _messages.value = loadedMessages
                }
            } catch (e: Exception) {
                // 加载失败，从头开始
            }
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
                // 获取用户记忆作为上下文
                val userMemories = getUserMemories()
                val systemPrompt = if (userMemories.isNotEmpty()) {
                    "你是一个可爱的桌面宠物叫小猪佩奇。请根据以下用户记忆来提供更个性化的回复：\n$userMemories"
                } else {
                    "你是一个可爱的桌面宠物叫小猪佩奇，善于和用户聊天，回复亲切有趣。"
                }

                val messages = buildList {
                    add(LLMMessage(role = "system", content = systemPrompt))
                    addAll(_messages.value.map { LLMMessage(role = it.role, content = it.content) })
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
                
                // 保存用户消息到数据库
                conversationRepository.insertConversation(
                    ConversationEntity(
                        userId = userId,
                        message = userMessage.content,
                        response = "",
                        messageType = 0,
                        timestamp = userMessage.timestamp
                    )
                )

                // 获取助手回复内容
                val assistantMessages = _messages.value.filter { it.role == "assistant" }
                val lastAssistantMessage = assistantMessages.lastOrNull()

                // 保存助手回复到数据库
                lastAssistantMessage?.let { assistantMsg ->
                    conversationRepository.insertConversation(
                        ConversationEntity(
                            userId = userId,
                            message = "",
                            response = assistantMsg.content,
                            messageType = 1,
                            timestamp = assistantMsg.timestamp
                        )
                    )
                }

                // 学习用户输入中的信息
                learnFromChat(userMessage.content)
                
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
            // 清空当前用户的所有对话
            conversationRepository.deleteAllConversationsByUserId(userId)
        }
    }

    /**
     * 从对话中学习用户信息
     */
    private suspend fun learnFromChat(userInput: String) {
        // 提取用户名称
        val namePatterns = listOf(
            "我叫(.*?)(?:，|。|$)".toRegex(),
            "我叫的是(.*?)(?:，|。|$)".toRegex(),
            "我是(.*?)(?:，|。|$)".toRegex(),
            "名字叫(.*?)(?:，|。|$)".toRegex()
        )

        for (pattern in namePatterns) {
            pattern.find(userInput)?.let { match ->
                val name = match.groupValues[1].trim()
                // 检查是否已存在
                val existing = memoryRepository.getMemoryByKey(userId, "user_name")
                if (existing == null) {
                    memoryRepository.insertMemory(
                        MemoryEntity(
                            userId = userId,
                            key = "user_name",
                            value = name,
                            category = "preference",
                            importance = 5
                        )
                    )
                }
            }
        }

        // 提取用户偏好
        val preferencePatterns = listOf(
            "我喜欢(.*?)(?:，|。|$)".toRegex(),
            "我喜欢(.*?)(?:，|。|$)".toRegex(),
            "我爱(.*?)(?:，|。|$)".toRegex(),
            "我讨厌(.*?)(?:，|。|$)".toRegex()
        )

        for (pattern in preferencePatterns) {
            pattern.findAll(userInput).forEach { match ->
                val preference = match.groupValues[1].trim()
                memoryRepository.insertMemory(
                    MemoryEntity(
                        userId = userId,
                        key = "user_preference_${System.currentTimeMillis()}",
                        value = preference,
                        category = "preference",
                        importance = 3
                    )
                )
            }
        }

        // 提取习惯
        val habitPatterns = listOf(
            "我每天(.*?)(?:，|。|$)".toRegex(),
            "我通常(.*?)(?:，|。|$)".toRegex(),
            "我习惯(.*?)(?:，|。|$)".toRegex(),
            "我一般在(.*?)(?:，|。|$)".toRegex(),
            "我经常(.*?)(?:，|。|$)".toRegex(),
            "我早上(.*?)(?:，|。|$)".toRegex(),
            "我晚上(.*?)(?:，|。|$)".toRegex()
        )

        for (pattern in habitPatterns) {
            pattern.findAll(userInput).forEach { match ->
                val habit = match.groupValues[1].trim()
                memoryRepository.insertMemory(
                    MemoryEntity(
                        userId = userId,
                        key = "user_habit_${System.currentTimeMillis()}",
                        value = habit,
                        category = "habit",
                        importance = 2
                    )
                )
            }
        }

        // 提取关系
        val relationshipPatterns = listOf(
            "我(.*?)是(.*?)(?:的|)(?:爸爸|妈妈|父亲|母亲|老公|老婆|丈夫|妻子|男朋友|女朋友|朋友|同学|同事)".toRegex(),
            "我(.*?)在(.*?)(?:工作|上学|读书)".toRegex(),
            "我是(.*?)(?:大学生|高中生|初中生|小学生|研究生|博士生)".toRegex(),
            "我在(.*?)(?:公司|单位|学校|医院|银行)工作".toRegex(),
            "我家里有(.*?)".toRegex()
        )

        for (pattern in relationshipPatterns) {
            pattern.findAll(userInput).forEach { match ->
                val relationship = match.value.trim()
                memoryRepository.insertMemory(
                    MemoryEntity(
                        userId = userId,
                        key = "user_relationship_${System.currentTimeMillis()}",
                        value = relationship,
                        category = "relationship",
                        importance = 3
                    )
                )
            }
        }

        // 提取通用信息
        val generalPatterns = listOf(
            "我(.*?)岁".toRegex(),
            "我住在(.*?)(?:，|。|$)".toRegex(),
            "我是(.*?)(?:人|的)".toRegex(),
            "我在(.*?)(?:城市|地方)".toRegex()
        )

        for (pattern in generalPatterns) {
            pattern.findAll(userInput).forEach { match ->
                val info = match.groupValues[1].trim()
                if (info.length > 2) { // 避免太短的信息
                    memoryRepository.insertMemory(
                        MemoryEntity(
                            userId = userId,
                            key = "user_general_${System.currentTimeMillis()}",
                            value = info,
                            category = "general",
                            importance = 2
                        )
                    )
                }
            }
        }
    }

    /**
     * 获取用户记忆用于上下文
     */
    private suspend fun getUserMemories(): String {
        return try {
            val memories = memoryRepository.getMemoriesByUserId(userId).first()
            if (memories.isEmpty()) {
                ""
            } else {
                val memoryText = memories.take(5).joinToString("\n") { "- ${it.key}: ${it.value}" }
                "用户记忆：\n$memoryText"
            }
        } catch (e: Exception) {
            ""
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

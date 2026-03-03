package com.petdesk.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petdesk.data.local.entity.MemoryEntity
import com.petdesk.domain.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 记忆状态
 */
data class MemoryState(
    val memories: List<MemoryEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val searchQuery: String = ""
)

/**
 * 记忆 ViewModel
 */
@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MemoryState())
    val state: StateFlow<MemoryState> = _state.asStateFlow()

    private val _userId = MutableStateFlow(1L)

    init {
        loadMemories()
    }

    /**
     * 加载记忆列表
     */
    private fun loadMemories() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            _userId.flatMapLatest { userId ->
                memoryRepository.getMemoriesByUserId(userId)
            }.collect { memories ->
                _state.update {
                    it.copy(
                        memories = memories,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * 按分类加载记忆
     */
    fun loadMemoriesByCategory(category: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, selectedCategory = category) }

            val flow = if (category == null) {
                memoryRepository.getMemoriesByUserId(_userId.value)
            } else {
                memoryRepository.getMemoriesByCategory(_userId.value, category)
            }

            flow.collect { memories ->
                _state.update {
                    it.copy(
                        memories = memories,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * 搜索记忆
     */
    fun searchMemories(query: String) {
        _state.update { it.copy(searchQuery = query) }

        viewModelScope.launch {
            if (query.isBlank()) {
                loadMemories()
            } else {
                memoryRepository.getMemoriesByKeyPattern(_userId.value, query).collect { memories ->
                    _state.update { it.copy(memories = memories) }
                }
            }
        }
    }

    /**
     * 添加记忆
     */
    fun addMemory(
        key: String,
        value: String,
        category: String = "general",
        importance: Int = 1
    ) {
        viewModelScope.launch {
            try {
                val memory = MemoryEntity(
                    userId = _userId.value,
                    key = key,
                    value = value,
                    category = category,
                    importance = importance
                )
                memoryRepository.insertMemory(memory)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * 更新记忆
     */
    fun updateMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            try {
                memoryRepository.updateMemory(
                    memory.copy(updatedAt = System.currentTimeMillis())
                )
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * 删除记忆
     */
    fun deleteMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            try {
                memoryRepository.deleteMemory(memory)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * 清空所有记忆
     */
    fun clearAllMemories() {
        viewModelScope.launch {
            try {
                memoryRepository.deleteAllMemoriesByUserId(_userId.value)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * 清空指定分类的记忆
     */
    fun clearMemoriesByCategory(category: String) {
        viewModelScope.launch {
            try {
                memoryRepository.deleteMemoriesByCategory(_userId.value, category)
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * 获取记忆数量
     */
    fun getMemoryCount() {
        viewModelScope.launch {
            try {
                val count = memoryRepository.getMemoryCount(_userId.value)
                // 可以用于显示在 UI 上
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * 从对话中学习记忆
     */
    fun learnFromConversation(userInput: String, assistantResponse: String) {
        viewModelScope.launch {
            try {
                // 简单的记忆学习逻辑
                // 实际应用中可以使用 LLM 来提取关键信息

                // 提取用户名称（简单示例）
                val namePatterns = listOf(
                    "我叫(.*)".toRegex(),
                    "我叫的是(.*)".toRegex(),
                    "我是(.*)".toRegex()
                )

                for (pattern in namePatterns) {
                    pattern.find(userInput)?.let { match ->
                        val name = match.groupValues[1].trim()
                        addMemory(
                            key = "user_name",
                            value = name,
                            category = "preference",
                            importance = 5
                        )
                    }
                }

                // 提取用户偏好
                val preferencePatterns = listOf(
                    "我喜欢(.*)".toRegex(),
                    "我喜欢(.*)".toRegex(),
                    "我喜欢(.*)".toRegex()
                )

                for (pattern in preferencePatterns) {
                    pattern.find(userInput)?.let { match ->
                        val preference = match.groupValues[1].trim()
                        addMemory(
                            key = "user_preference_${System.currentTimeMillis()}",
                            value = preference,
                            category = "preference",
                            importance = 3
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

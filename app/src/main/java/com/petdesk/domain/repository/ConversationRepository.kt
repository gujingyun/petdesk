package com.petdesk.domain.repository

import com.petdesk.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for conversation management
 */
interface ConversationRepository {
    fun getConversationsByUserId(userId: Long): Flow<List<ConversationEntity>>
    fun getRecentConversations(userId: Long, limit: Int): Flow<List<ConversationEntity>>
    fun getFavoriteConversations(userId: Long): Flow<List<ConversationEntity>>
    fun searchConversations(userId: Long, keyword: String): Flow<List<ConversationEntity>>
    suspend fun getConversationById(id: Long): ConversationEntity?
    suspend fun insertConversation(conversation: ConversationEntity): Long
    suspend fun updateConversation(conversation: ConversationEntity)
    suspend fun deleteConversation(conversation: ConversationEntity)
    suspend fun deleteAllConversationsByUserId(userId: Long)
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)
    suspend fun getConversationCount(userId: Long): Int
}

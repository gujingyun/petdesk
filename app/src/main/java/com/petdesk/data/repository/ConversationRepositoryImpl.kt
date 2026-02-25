package com.petdesk.data.repository

import com.petdesk.data.local.dao.ConversationDao
import com.petdesk.data.local.entity.ConversationEntity
import com.petdesk.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Conversation Repository Implementation
 */
@Singleton
class ConversationRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao
) : ConversationRepository {

    override fun getConversationsByUserId(userId: Long): Flow<List<ConversationEntity>> =
        conversationDao.getConversationsByUserId(userId)

    override fun getRecentConversations(userId: Long, limit: Int): Flow<List<ConversationEntity>> =
        conversationDao.getRecentConversations(userId, limit)

    override fun getFavoriteConversations(userId: Long): Flow<List<ConversationEntity>> =
        conversationDao.getFavoriteConversations(userId)

    override fun searchConversations(userId: Long, keyword: String): Flow<List<ConversationEntity>> =
        conversationDao.searchConversations(userId, keyword)

    override suspend fun getConversationById(id: Long): ConversationEntity? =
        conversationDao.getConversationById(id)

    override suspend fun insertConversation(conversation: ConversationEntity): Long =
        conversationDao.insertConversation(conversation)

    override suspend fun updateConversation(conversation: ConversationEntity) =
        conversationDao.updateConversation(conversation)

    override suspend fun deleteConversation(conversation: ConversationEntity) =
        conversationDao.deleteConversation(conversation)

    override suspend fun deleteAllConversationsByUserId(userId: Long) =
        conversationDao.deleteAllConversationsByUserId(userId)

    override suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean) =
        conversationDao.updateFavoriteStatus(id, isFavorite)

    override suspend fun getConversationCount(userId: Long): Int =
        conversationDao.getConversationCount(userId)
}

package com.petdesk.data.local.dao

import androidx.room.*
import com.petdesk.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

/**
 * 对话记录表 DAO
 */
@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE userId = :userId ORDER BY timestamp DESC")
    fun getConversationsByUserId(userId: Long): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentConversations(userId: Long, limit: Int): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE userId = :userId AND isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteConversations(userId: Long): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversationById(id: Long): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE userId = :userId AND message LIKE '%' || :keyword || '%' ORDER BY timestamp DESC")
    fun searchConversations(userId: Long, keyword: String): Flow<List<ConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Delete
    suspend fun deleteConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE userId = :userId")
    suspend fun deleteAllConversationsByUserId(userId: Long)

    @Query("UPDATE conversations SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM conversations WHERE userId = :userId")
    suspend fun getConversationCount(userId: Long): Int
}

package com.petdesk.domain.repository

import com.petdesk.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user management
 */
interface UserRepository {
    fun getAllUsers(): Flow<List<UserEntity>>
    fun getUserById(id: Long): Flow<UserEntity?>
    suspend fun getCurrentUser(): UserEntity?
    suspend fun getUserByUsername(username: String): UserEntity?
    suspend fun insertUser(user: UserEntity): Long
    suspend fun updateUser(user: UserEntity)
    suspend fun deleteUser(user: UserEntity)
    suspend fun updateLastLoginTime(id: Long)
}

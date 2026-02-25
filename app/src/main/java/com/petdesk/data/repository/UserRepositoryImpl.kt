package com.petdesk.data.repository

import com.petdesk.data.local.dao.UserDao
import com.petdesk.data.local.entity.UserEntity
import com.petdesk.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * User Repository Implementation
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    override fun getUserById(id: Long): Flow<UserEntity?> = userDao.getUserByIdFlow(id)

    override suspend fun getCurrentUser(): UserEntity? = userDao.getCurrentUser()

    override suspend fun getUserByUsername(username: String): UserEntity? =
        userDao.getUserByUsername(username)

    override suspend fun insertUser(user: UserEntity): Long = userDao.insertUser(user)

    override suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    override suspend fun deleteUser(user: UserEntity) = userDao.deleteUser(user)

    override suspend fun updateLastLoginTime(id: Long) {
        userDao.updateLastLoginTime(id, System.currentTimeMillis())
    }
}

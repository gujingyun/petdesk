package com.petdesk.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.petdesk.data.local.PetDeskDatabase
import com.petdesk.data.local.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UserDao 单元测试
 * 测试用户表的 CRUD 操作
 */
@RunWith(AndroidJUnit4::class)
class UserDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: PetDeskDatabase
    private lateinit var userDao: UserDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, PetDeskDatabase::class.java).build()
        userDao = database.userDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==================== Create Tests ====================

    @Test
    fun `insertUser - should insert user successfully`() = runBlocking {
        // Given
        val user = UserEntity(
            username = "testuser",
            nickname = "Test User",
            email = "test@example.com"
        )

        // When
        val userId = userDao.insertUser(user)

        // Then
        assertTrue(userId > 0)
    }

    @Test
    fun `insertUser - should replace on conflict`() = runBlocking {
        // Given
        val user1 = UserEntity(id = 1, username = "testuser", nickname = "User 1")
        userDao.insertUser(user1)

        // When - insert with same id should replace
        val user2 = UserEntity(id = 1, username = "testuser", nickname = "User 2 Updated")
        userDao.insertUser(user2)

        // Then
        val result = userDao.getUserById(1)
        assertEquals("User 2 Updated", result?.nickname)
    }

    // ==================== Read Tests ====================

    @Test
    fun `getUserById - should return user when exists`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser", nickname = "Test User")
        val userId = userDao.insertUser(user)

        // When
        val result = userDao.getUserById(userId)

        // Then
        assertNotNull(result)
        assertEquals("testuser", result?.username)
        assertEquals("Test User", result?.nickname)
    }

    @Test
    fun `getUserById - should return null when not exists`() = runBlocking {
        // When
        val result = userDao.getUserById(999)

        // Then
        assertNull(result)
    }

    @Test
    fun `getUserByIdFlow - should return Flow of user`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)

        // When
        val flow = userDao.getUserByIdFlow(userId)
        val result = flow.first()

        // Then
        assertNotNull(result)
        assertEquals(userId, result?.id)
    }

    @Test
    fun `getUserByUsername - should return user when exists`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser", nickname = "Test")
        userDao.insertUser(user)

        // When
        val result = userDao.getUserByUsername("testuser")

        // Then
        assertNotNull(result)
        assertEquals("testuser", result?.username)
    }

    @Test
    fun `getUserByUsername - should return null when not exists`() = runBlocking {
        // When
        val result = userDao.getUserByUsername("nonexistent")

        // Then
        assertNull(result)
    }

    @Test
    fun `getCurrentUser - should return user with latest login time`() = runBlocking {
        // Given
        val user1 = UserEntity(username = "user1", status = 1, lastLoginAt = 1000)
        val user2 = UserEntity(username = "user2", status = 1, lastLoginAt = 2000)
        userDao.insertUser(user1)
        userDao.insertUser(user2)

        // When
        val result = userDao.getCurrentUser()

        // Then - should return user2 with latest lastLoginAt
        assertNotNull(result)
        assertEquals("user2", result?.username)
    }

    @Test
    fun `getCurrentUser - should return null when no active user`() = runBlocking {
        // Given
        val user = UserEntity(username = "user1", status = 0)
        userDao.insertUser(user)

        // When
        val result = userDao.getCurrentUser()

        // Then
        assertNull(result)
    }

    @Test
    fun `getAllUsers - should return all users as Flow`() = runBlocking {
        // Given
        userDao.insertUser(UserEntity(username = "user1"))
        userDao.insertUser(UserEntity(username = "user2"))

        // When
        val result = userDao.getAllUsers().first()

        // Then
        assertEquals(2, result.size)
    }

    // ==================== Update Tests ====================

    @Test
    fun `updateUser - should update user successfully`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser", nickname = "Original")
        val userId = userDao.insertUser(user)

        // When
        val updatedUser = user.copy(id = userId, nickname = "Updated")
        userDao.updateUser(updatedUser)

        // Then
        val result = userDao.getUserById(userId)
        assertEquals("Updated", result?.nickname)
    }

    @Test
    fun `updateLastLoginTime - should update last login time`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser", lastLoginAt = 0)
        val userId = userDao.insertUser(user)
        val newTimestamp = System.currentTimeMillis()

        // When
        userDao.updateLastLoginTime(userId, newTimestamp)

        // Then
        val result = userDao.getUserById(userId)
        assertEquals(newTimestamp, result?.lastLoginAt)
    }

    // ==================== Delete Tests ====================

    @Test
    fun `deleteUser - should delete user successfully`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)

        // When
        val userToDelete = userDao.getUserById(userId)
        userDao.deleteUser(userToDelete!!)

        // Then
        val result = userDao.getUserById(userId)
        assertNull(result)
    }

    @Test
    fun `deleteUserById - should delete user by id`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)

        // When
        userDao.deleteUserById(userId)

        // Then
        val result = userDao.getUserById(userId)
        assertNull(result)
    }
}

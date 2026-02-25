package com.petdesk.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.petdesk.data.local.PetDeskDatabase
import com.petdesk.data.local.entity.ConversationEntity
import com.petdesk.data.local.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * ConversationDao 单元测试
 * 测试对话记录表的 CRUD 操作
 */
@RunWith(AndroidJUnit4::class)
class ConversationDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: PetDeskDatabase
    private lateinit var conversationDao: ConversationDao
    private lateinit var userDao: UserDao

    private var testUserId: Long = 0

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, PetDeskDatabase::class.java).build()
        conversationDao = database.conversationDao()
        userDao = database.userDao()

        // Create test user
        runBlocking {
            val user = UserEntity(username = "testuser")
            testUserId = userDao.insertUser(user)
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==================== Create Tests ====================

    @Test
    fun `insertConversation - should insert conversation successfully`() = runBlocking {
        // Given
        val conversation = ConversationEntity(
            userId = testUserId,
            message = "Hello",
            response = "Hi there!"
        )

        // When
        val conversationId = conversationDao.insertConversation(conversation)

        // Then
        assertTrue(conversationId > 0)
    }

    @Test
    fun `insertConversation - should replace on conflict`() = runBlocking {
        // Given
        val conversation1 = ConversationEntity(
            id = 1,
            userId = testUserId,
            message = "Hello"
        )
        conversationDao.insertConversation(conversation1)

        // When - insert with same id should replace
        val conversation2 = ConversationEntity(
            id = 1,
            userId = testUserId,
            message = "Hello Updated"
        )
        conversationDao.insertConversation(conversation2)

        // Then
        val result = conversationDao.getConversationById(1)
        assertEquals("Hello Updated", result?.message)
    }

    // ==================== Read Tests ====================

    @Test
    fun `getConversationById - should return conversation when exists`() = runBlocking {
        // Given
        val conversation = ConversationEntity(
            userId = testUserId,
            message = "Hello",
            response = "Hi there!"
        )
        val conversationId = conversationDao.insertConversation(conversation)

        // When
        val result = conversationDao.getConversationById(conversationId)

        // Then
        assertNotNull(result)
        assertEquals("Hello", result?.message)
        assertEquals("Hi there!", result?.response)
    }

    @Test
    fun `getConversationById - should return null when not exists`() = runBlocking {
        // When
        val result = conversationDao.getConversationById(999)

        // Then
        assertNull(result)
    }

    @Test
    fun `getConversationsByUserId - should return all conversations for user`() = runBlocking {
        // Given
        conversationDao.insertConversation(ConversationEntity(userId = testUserId, message = "Message 1"))
        conversationDao.insertConversation(ConversationEntity(userId = testUserId, message = "Message 2"))
        // Create another user
        val otherUserId = userDao.insertUser(UserEntity(username = "otheruser"))
        conversationDao.insertConversation(ConversationEntity(userId = otherUserId, message = "Other message"))

        // When
        val result = conversationDao.getConversationsByUserId(testUserId).first()

        // Then - only 2 conversations for testUserId
        assertEquals(2, result.size)
    }

    @Test
    fun `getRecentConversations - should return limited conversations`() = runBlocking {
        // Given
        conversationDao.insertConversation(ConversationEntity(userId = testUserId, message = "Message 1"))
        conversationDao.insertConversation(ConversationEntity(userId = testUserId, message = "Message 2"))
        conversationDao.insertConversation(ConversationEntity(userId = testUserId, message = "Message 3"))

        // When
        val result = conversationDao.getRecentConversations(testUserId, 2).first()

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun `getFavoriteConversations - should return only favorite conversations`() = runBlocking {
        // Given
        conversationDao.insertConversation(
            ConversationEntity(userId = testUserId, message = "Favorite", isFavorite = true)
        )
        conversationDao.insertConversation(
            ConversationEntity(userId = testUserId, message = "Not Favorite", isFavorite = false)
        )

        // When
        val result = conversationDao.getFavoriteConversations(testUserId).first()

        // Then
        assertEquals(1, result.size)
        assertTrue(result.first().isFavorite)
    }

    @Test
    fun `searchConversations - should return matching conversations`() = runBlocking {
        // Given
        conversationDao.insertConversation(ConversationEntity(userId = testUserId, message = "Hello world"))
        conversationDao.insertConversation(ConversationEntity(userId = testUserId, message = "Goodbye"))
        conversationDao.insertConversation(ConversationEntity(userId = testUserId, message = "Hello friend"))

        // When
        val result = conversationDao.searchConversations(testUserId, "Hello").first()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.message.contains("Hello") })
    }

    @Test
    fun `getConversationCount - should return correct count`() = runBlocking {
        // Given
        conversationDao.insertConversation(ConversationEntity(userId = testUserId, message = "Message 1"))
        conversationDao.insertConversation(ConversationEntity(userId = testUserId, message = "Message 2"))

        // When
        val count = conversationDao.getConversationCount(testUserId)

        // Then
        assertEquals(2, count)
    }

    // ==================== Update Tests ====================

    @Test
    fun `updateConversation - should update conversation successfully`() = runBlocking {
        // Given
        val conversation = ConversationEntity(
            userId = testUserId,
            message = "Original",
            response = "Original Response"
        )
        val conversationId = conversationDao.insertConversation(conversation)

        // When
        val updatedConversation = conversation.copy(
            id = conversationId,
            message = "Updated",
            response = "Updated Response"
        )
        conversationDao.updateConversation(updatedConversation)

        // Then
        val result = conversationDao.getConversationById(conversationId)
        assertEquals("Updated", result?.message)
        assertEquals("Updated Response", result?.response)
    }

    @Test
    fun `updateFavoriteStatus - should update favorite status`() = runBlocking {
        // Given
        val conversationId = conversationDao.insertConversation(
            ConversationEntity(userId = testUserId, message = "Test", isFavorite = false)
        )

        // When
        conversationDao.updateFavoriteStatus(conversationId, true)

        // Then
        val result = conversationDao.getConversationById(conversationId)
        assertTrue(result?.isFavorite == true)
    }

    // ==================== Delete Tests ====================

    @Test
    fun `deleteConversation - should delete conversation successfully`() = runBlocking {
        // Given
        val conversationId = conversationDao.insertConversation(
            ConversationEntity(userId = testUserId, message = "Test")
        )

        // When
        val conversation = conversationDao.getConversationById(conversationId)
        conversationDao.deleteConversation(conversation!!)

        // Then
        val result = conversationDao.getConversationById(conversationId)
        assertNull(result)
    }

    @Test
    fun `deleteAllConversationsByUserId - should delete all conversations for user`() = runBlocking {
        // Given
        conversationDao.insertConversation(ConversationEntity(userId = testUserId, message = "Message 1"))
        conversationDao.insertConversation(ConversationEntity(userId = testUserId, message = "Message 2"))

        // When
        conversationDao.deleteAllConversationsByUserId(testUserId)

        // Then
        val count = conversationDao.getConversationCount(testUserId)
        assertEquals(0, count)
    }
}

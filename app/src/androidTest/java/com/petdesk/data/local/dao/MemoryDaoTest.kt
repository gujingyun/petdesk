package com.petdesk.data.local.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.petdesk.data.local.PetDeskDatabase
import com.petdesk.data.local.entity.MemoryEntity
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
 * MemoryDao 单元测试
 * 测试记忆表的 CRUD 操作
 */
@RunWith(AndroidJUnit4::class)
class MemoryDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: PetDeskDatabase
    private lateinit var memoryDao: MemoryDao
    private lateinit var userDao: UserDao

    private var testUserId: Long = 0

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, PetDeskDatabase::class.java).build()
        memoryDao = database.memoryDao()
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
    fun `insertMemory - should insert memory successfully`() = runBlocking {
        // Given
        val memory = MemoryEntity(
            userId = testUserId,
            key = "user_name",
            value = "John"
        )

        // When
        val memoryId = memoryDao.insertMemory(memory)

        // Then
        assertTrue(memoryId > 0)
    }

    @Test
    fun `insertMemory - should replace on conflict`() = runBlocking {
        // Given
        val memory1 = MemoryEntity(
            userId = testUserId,
            key = "user_name",
            value = "John"
        )
        memoryDao.insertMemory(memory1)

        // When - insert with same key should replace
        val memory2 = MemoryEntity(
            userId = testUserId,
            key = "user_name",
            value = "Jane"
        )
        memoryDao.insertMemory(memory2)

        // Then
        val result = memoryDao.getMemoryByKey(testUserId, "user_name")
        assertEquals("Jane", result?.value)
    }

    @Test
    fun `insertMemories - should insert multiple memories successfully`() = runBlocking {
        // Given
        val memories = listOf(
            MemoryEntity(userId = testUserId, key = "key1", value = "value1"),
            MemoryEntity(userId = testUserId, key = "key2", value = "value2")
        )

        // When
        memoryDao.insertMemories(memories)

        // Then
        val result = memoryDao.getMemoriesByUserId(testUserId).first()
        assertEquals(2, result.size)
    }

    // ==================== Read Tests ====================

    @Test
    fun `getMemoryByKey - should return memory when exists`() = runBlocking {
        // Given
        val memory = MemoryEntity(
            userId = testUserId,
            key = "user_name",
            value = "John"
        )
        memoryDao.insertMemory(memory)

        // When
        val result = memoryDao.getMemoryByKey(testUserId, "user_name")

        // Then
        assertNotNull(result)
        assertEquals("user_name", result?.key)
        assertEquals("John", result?.value)
    }

    @Test
    fun `getMemoryByKey - should return null when not exists`() = runBlocking {
        // When
        val result = memoryDao.getMemoryByKey(testUserId, "nonexistent")

        // Then
        assertNull(result)
    }

    @Test
    fun `getMemoriesByUserId - should return all memories for user`() = runBlocking {
        // Given
        memoryDao.insertMemory(MemoryEntity(userId = testUserId, key = "key1"))
        memoryDao.insertMemory(MemoryEntity(userId = testUserId, key = "key2"))
        // Create another user
        val otherUserId = userDao.insertUser(UserEntity(username = "otheruser"))
        memoryDao.insertMemory(MemoryEntity(userId = otherUserId, key = "other_key"))

        // When
        val result = memoryDao.getMemoriesByUserId(testUserId).first()

        // Then - only 2 memories for testUserId
        assertEquals(2, result.size)
    }

    @Test
    fun `getMemoriesByCategory - should return memories by category`() = runBlocking {
        // Given
        memoryDao.insertMemory(MemoryEntity(userId = testUserId, key = "key1", category = "preference"))
        memoryDao.insertMemory(MemoryEntity(userId = testUserId, key = "key2", category = "preference"))
        memoryDao.insertMemory(MemoryEntity(userId = testUserId, key = "key3", category = "habit"))

        // When
        val result = memoryDao.getMemoriesByCategory(testUserId, "preference").first()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.category == "preference" })
    }

    @Test
    fun `getMemoriesByKeyPattern - should return memories matching key pattern`() = runBlocking {
        // Given
        memoryDao.insertMemory(MemoryEntity(userId = testUserId, key = "user_name"))
        memoryDao.insertMemory(MemoryEntity(userId = testUserId, key = "user_age"))
        memoryDao.insertMemory(MemoryEntity(userId = testUserId, key = "pet_name"))

        // When
        val result = memoryDao.getMemoriesByKeyPattern(testUserId, "user_").first()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.key.startsWith("user_") })
    }

    @Test
    fun `getRecentMemories - should return limited recent memories`() = runBlocking {
        // Given
        memoryDao.insertMemory(MemoryEntity(userId = testUserId, key = "recent1", lastAccessedAt = 1000))
        memoryDao.insertMemory(MemoryEntity(userId = testUserId, key = "recent2", lastAccessedAt = 2000))
        memoryDao.insertMemory(MemoryEntity(userId = testUserId, key = "recent3", lastAccessedAt = 3000))

        // When
        val result = memoryDao.getRecentMemories(testUserId, 2).first()

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun `getMemoryCount - should return correct count`() = runBlocking {
        // Given
        memoryDao.insertMemory(MemoryEntity(userId = testUserId, key = "key1"))
        memoryDao.insertMemory(MemoryEntity(userId = testUserId, key = "key2"))

        // When
        val count = memoryDao.getMemoryCount(testUserId)

        // Then
        assertEquals(2, count)
    }

    // ==================== Update Tests ====================

    @Test
    fun `updateMemory - should update memory successfully`() = runBlocking {
        // Given
        val memoryId = memoryDao.insertMemory(
            MemoryEntity(userId = testUserId, key = "user_name", value = "John")
        )

        // When
        val updatedMemory = memoryDao.getMemoryByKey(testUserId, "user_name")!!.copy(value = "Jane")
        memoryDao.updateMemory(updatedMemory)

        // Then
        val result = memoryDao.getMemoryByKey(testUserId, "user_name")
        assertEquals("Jane", result?.value)
    }

    @Test
    fun `updateAccessInfo - should update access count and timestamp`() = runBlocking {
        // Given
        val memoryId = memoryDao.insertMemory(
            MemoryEntity(userId = testUserId, key = "test", accessCount = 0)
        )

        // When
        val newTimestamp = System.currentTimeMillis()
        memoryDao.updateAccessInfo(memoryId, newTimestamp)

        // Then
        val result = memoryDao.getMemoryByKey(testUserId, "test")
        assertTrue((result?.accessCount ?: 0) > 0)
        assertTrue(result?.lastAccessedAt!! >= newTimestamp - 1000)
    }

    // ==================== Delete Tests ====================

    @Test
    fun `deleteMemory - should delete memory successfully`() = runBlocking {
        // Given
        memoryDao.insertMemory(MemoryEntity(userId = testUserId, key = "test"))

        // When
        val memory = memoryDao.getMemoryByKey(testUserId, "test")
        memoryDao.deleteMemory(memory!!)

        // Then
        val result = memoryDao.getMemoryByKey(testUserId, "test")
        assertNull(result)
    }

    @Test
    fun `deleteAllMemoriesByUserId - should delete all memories for user`() = runBlocking {
        // Given
        memoryDao.insertMemory(MemoryEntity(userId = testUserId, key = "key1"))
        memoryDao.insertMemory(MemoryEntity(userId = testUserId, key = "key2"))

        // When
        memoryDao.deleteAllMemoriesByUserId(testUserId)

        // Then
        val count = memoryDao.getMemoryCount(testUserId)
        assertEquals(0, count)
    }

    @Test
    fun `deleteMemoriesByCategory - should delete memories by category`() = runBlocking {
        // Given
        memoryDao.insertMemory(MemoryEntity(userId = testUserId, key = "key1", category = "preference"))
        memoryDao.insertMemory(MemoryEntity(userId = testUserId, key = "key2", category = "preference"))
        memoryDao.insertMemory(MemoryEntity(userId = testUserId, key = "key3", category = "habit"))

        // When
        memoryDao.deleteMemoriesByCategory(testUserId, "preference")

        // Then
        val preferenceMemories = memoryDao.getMemoriesByCategory(testUserId, "preference").first()
        val habitMemories = memoryDao.getMemoriesByCategory(testUserId, "habit").first()

        assertEquals(0, preferenceMemories.size)
        assertEquals(1, habitMemories.size)
    }
}

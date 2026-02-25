package com.petdesk.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.petdesk.data.local.dao.ConversationDao
import com.petdesk.data.local.dao.MemoryDao
import com.petdesk.data.local.dao.SkinDao
import com.petdesk.data.local.dao.SkillDao
import com.petdesk.data.local.dao.TaskDao
import com.petdesk.data.local.dao.UserDao
import com.petdesk.data.local.dao.UserPreferencesDao
import com.petdesk.data.local.entity.ConversationEntity
import com.petdesk.data.local.entity.MemoryEntity
import com.petdesk.data.local.entity.SkinEntity
import com.petdesk.data.local.entity.SkillEntity
import com.petdesk.data.local.entity.TaskEntity
import com.petdesk.data.local.entity.UserEntity
import com.petdesk.data.local.entity.UserPreferencesEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Database tests for W4 - Database Module.
 * Tests all 7 DAOs with in-memory database.
 */
@RunWith(AndroidJUnit4::class)
class PetDeskDatabaseTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: PetDeskDatabase
    private lateinit var userDao: UserDao
    private lateinit var conversationDao: ConversationDao
    private lateinit var memoryDao: MemoryDao
    private lateinit var skinDao: SkinDao
    private lateinit var skillDao: SkillDao
    private lateinit var taskDao: TaskDao
    private lateinit var userPreferencesDao: UserPreferencesDao

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, PetDeskDatabase::class.java).build()
        userDao = database.userDao()
        conversationDao = database.conversationDao()
        memoryDao = database.memoryDao()
        skinDao = database.skinDao()
        skillDao = database.skillDao()
        taskDao = database.taskDao()
        userPreferencesDao = database.userPreferencesDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ==================== User DAO Tests ====================

    @Test
    fun `UserDao - Insert and get user by id`() = runBlocking {
        // Given
        val user = UserEntity(
            username = "testuser",
            nickname = "Test User",
            email = "test@example.com"
        )
        val userId = userDao.insertUser(user)

        // When
        val result = userDao.getUserById(userId)

        // Then
        assertNotNull(result)
        assertEquals("testuser", result?.username)
        assertEquals("Test User", result?.nickname)
    }

    @Test
    fun `UserDao - Get user by username`() = runBlocking {
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
    fun `UserDao - Get current user`() = runBlocking {
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
    fun `UserDao - Update user`() = runBlocking {
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
    fun `UserDao - Delete user`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)

        // When
        userDao.deleteUserById(userId)

        // Then
        val result = userDao.getUserById(userId)
        assertNull(result)
    }

    @Test
    fun `UserDao - Update last login time`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        val newTimestamp = System.currentTimeMillis()

        // When
        userDao.updateLastLoginTime(userId, newTimestamp)

        // Then
        val result = userDao.getUserById(userId)
        assertEquals(newTimestamp, result?.lastLoginAt)
    }

    // ==================== Conversation DAO Tests ====================

    @Test
    fun `ConversationDao - Insert and get conversation by id`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        val conversation = ConversationEntity(
            userId = userId,
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
    fun `ConversationDao - Get conversations by user id`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        conversationDao.insertConversation(ConversationEntity(userId = userId, message = "Message 1"))
        conversationDao.insertConversation(ConversationEntity(userId = userId, message = "Message 2"))

        // When
        val result = conversationDao.getConversationsByUserId(userId).first()

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun `ConversationDao - Get favorite conversations`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        conversationDao.insertConversation(
            ConversationEntity(userId = userId, message = "Favorite", isFavorite = true)
        )
        conversationDao.insertConversation(
            ConversationEntity(userId = userId, message = "Not Favorite", isFavorite = false)
        )

        // When
        val result = conversationDao.getFavoriteConversations(userId).first()

        // Then
        assertEquals(1, result.size)
        assertTrue(result.first().isFavorite)
    }

    @Test
    fun `ConversationDao - Search conversations`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        conversationDao.insertConversation(ConversationEntity(userId = userId, message = "Hello world"))
        conversationDao.insertConversation(ConversationEntity(userId = userId, message = "Goodbye"))

        // When
        val result = conversationDao.searchConversations(userId, "Hello").first()

        // Then
        assertEquals(1, result.size)
        assertTrue(result.first().message.contains("Hello"))
    }

    @Test
    fun `ConversationDao - Update favorite status`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        val conversationId = conversationDao.insertConversation(
            ConversationEntity(userId = userId, message = "Test", isFavorite = false)
        )

        // When
        conversationDao.updateFavoriteStatus(conversationId, true)

        // Then
        val result = conversationDao.getConversationById(conversationId)
        assertTrue(result?.isFavorite == true)
    }

    @Test
    fun `ConversationDao - Delete conversation`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        val conversationId = conversationDao.insertConversation(
            ConversationEntity(userId = userId, message = "Test")
        )

        // When
        val conversation = conversationDao.getConversationById(conversationId)
        conversationDao.deleteConversation(conversation!!)

        // Then
        val result = conversationDao.getConversationById(conversationId)
        assertNull(result)
    }

    // ==================== Memory DAO Tests ====================

    @Test
    fun `MemoryDao - Insert and get memory by id`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        val memory = MemoryEntity(
            userId = userId,
            key = "user_name",
            value = "John"
        )
        val memoryId = memoryDao.insertMemory(memory)

        // When
        val result = memoryDao.getMemoryByKey(userId, "user_name")

        // Then
        assertNotNull(result)
        assertEquals("user_name", result?.key)
        assertEquals("John", result?.value)
    }

    @Test
    fun `MemoryDao - Get memories by category`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        memoryDao.insertMemory(MemoryEntity(userId = userId, key = "key1", category = "preference"))
        memoryDao.insertMemory(MemoryEntity(userId = userId, key = "key2", category = "preference"))
        memoryDao.insertMemory(MemoryEntity(userId = userId, key = "key3", category = "habit"))

        // When
        val result = memoryDao.getMemoriesByCategory(userId, "preference").first()

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun `MemoryDao - Get recent memories`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        memoryDao.insertMemory(MemoryEntity(userId = userId, key = "recent1", lastAccessedAt = 1000))
        memoryDao.insertMemory(MemoryEntity(userId = userId, key = "recent2", lastAccessedAt = 2000))

        // When
        val result = memoryDao.getRecentMemories(userId, 1).first()

        // Then
        assertEquals(1, result.size)
    }

    @Test
    fun `MemoryDao - Update memory access info`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        val memoryId = memoryDao.insertMemory(
            MemoryEntity(userId = userId, key = "test", accessCount = 0)
        )

        // When
        memoryDao.updateAccessInfo(memoryId, System.currentTimeMillis())

        // Then
        val result = memoryDao.getMemoryByKey(userId, "test")
        assertTrue((result?.accessCount ?: 0) > 0)
    }

    @Test
    fun `MemoryDao - Insert multiple memories`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        val memories = listOf(
            MemoryEntity(userId = userId, key = "key1", value = "value1"),
            MemoryEntity(userId = userId, key = "key2", value = "value2")
        )

        // When
        memoryDao.insertMemories(memories)

        // Then
        val result = memoryDao.getMemoriesByUserId(userId).first()
        assertEquals(2, result.size)
    }

    @Test
    fun `MemoryDao - Delete memory`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        val memoryId = memoryDao.insertMemory(MemoryEntity(userId = userId, key = "test"))

        // When
        memoryDao.deleteMemory(memoryId)

        // Then
        val result = memoryDao.getMemoryByKey(userId, "test")
        assertNull(result)
    }

    // ==================== Skin DAO Tests ====================

    @Test
    fun `SkinDao - Insert and get skin by id`() = runBlocking {
        // Given
        val skin = SkinEntity(name = "Default Skin", isDefault = true)
        val skinId = skinDao.insertSkin(skin)

        // When
        val result = skinDao.getSkinById(skinId)

        // Then
        assertNotNull(result)
        assertEquals("Default Skin", result?.name)
    }

    @Test
    fun `SkinDao - Get all skins`() = runBlocking {
        // Given
        skinDao.insertSkin(SkinEntity(name = "Skin 1"))
        skinDao.insertSkin(SkinEntity(name = "Skin 2"))

        // When
        val result = skinDao.getAllSkins().first()

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun `SkinDao - Get unlocked skins`() = runBlocking {
        // Given
        skinDao.insertSkin(SkinEntity(name = "Unlocked", isUnlocked = true))
        skinDao.insertSkin(SkinEntity(name = "Locked", isUnlocked = false))

        // When
        val result = skinDao.getUnlockedSkins().first()

        // Then
        assertEquals(1, result.size)
        assertTrue(result.first().isUnlocked)
    }

    @Test
    fun `SkinDao - Get skins by category`() = runBlocking {
        // Given
        skinDao.insertSkin(SkinEntity(name = "Holiday 1", category = "holiday"))
        skinDao.insertSkin(SkinEntity(name = "Holiday 2", category = "holiday"))
        skinDao.insertSkin(SkinEntity(name = "Default", category = "default"))

        // When
        val result = skinDao.getSkinsByCategory("holiday").first()

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun `SkinDao - Get default skin`() = runBlocking {
        // Given
        skinDao.insertSkin(SkinEntity(name = "Non-default", isDefault = false))
        skinDao.insertSkin(SkinEntity(name = "Default", isDefault = true))

        // When
        val result = skinDao.getDefaultSkin()

        // Then
        assertNotNull(result)
        assertTrue(result?.isDefault == true)
    }

    @Test
    fun `SkinDao - Update unlock status`() = runBlocking {
        // Given
        val skinId = skinDao.insertSkin(SkinEntity(name = "Locked Skin", isUnlocked = false))

        // When
        skinDao.updateUnlockStatus(skinId, true)

        // Then
        val result = skinDao.getSkinById(skinId)
        assertTrue(result?.isUnlocked == true)
    }

    @Test
    fun `SkinDao - Search skins`() = runBlocking {
        // Given
        skinDao.insertSkin(SkinEntity(name = "Cute Cat"))
        skinDao.insertSkin(SkinEntity(name = "Cute Dog"))
        skinDao.insertSkin(SkinEntity(name = "Cool Robot"))

        // When
        val result = skinDao.searchSkins("Cute").first()

        // Then
        assertEquals(2, result.size)
    }

    // ==================== Skill DAO Tests ====================

    @Test
    fun `SkillDao - Insert and get skill by id`() = runBlocking {
        // Given
        val skill = SkillEntity(name = "Greeting", trigger = "hello,hi")
        val skillId = skillDao.insertSkill(skill)

        // When
        val result = skillDao.getSkillById(skillId)

        // Then
        assertNotNull(result)
        assertEquals("Greeting", result?.name)
    }

    @Test
    fun `SkillDao - Get all skills`() = runBlocking {
        // Given
        skillDao.insertSkill(SkillEntity(name = "Skill 1"))
        skillDao.insertSkill(SkillEntity(name = "Skill 2"))

        // When
        val result = skillDao.getAllSkills().first()

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun `SkillDao - Get enabled skills`() = runBlocking {
        // Given
        skillDao.insertSkill(SkillEntity(name = "Enabled", isEnabled = true))
        skillDao.insertSkill(SkillEntity(name = "Disabled", isEnabled = false))

        // When
        val result = skillDao.getEnabledSkills().first()

        // Then
        assertEquals(1, result.size)
        assertTrue(result.first().isEnabled)
    }

    @Test
    fun `SkillDao - Get skills by category`() = runBlocking {
        // Given
        skillDao.insertSkill(SkillEntity(name = "Entertainment 1", category = "entertainment"))
        skillDao.insertSkill(SkillEntity(name = "Entertainment 2", category = "entertainment"))
        skillDao.insertSkill(SkillEntity(name = "General", category = "general"))

        // When
        val result = skillDao.getSkillsByCategory("entertainment").first()

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun `SkillDao - Get system skills`() = runBlocking {
        // Given
        skillDao.insertSkill(SkillEntity(name = "System Skill", isSystem = true))
        skillDao.insertSkill(SkillEntity(name = "Custom Skill", isSystem = false))

        // When
        val result = skillDao.getSystemSkills().first()

        // Then
        assertEquals(1, result.size)
        assertTrue(result.first().isSystem)
    }

    @Test
    fun `SkillDao - Get skill by trigger`() = runBlocking {
        // Given
        skillDao.insertSkill(SkillEntity(name = "Greeting", trigger = "hello,hi"))

        // When
        val result = skillDao.getSkillByTrigger("hello")

        // Then
        assertNotNull(result)
        assertEquals("Greeting", result?.name)
    }

    @Test
    fun `SkillDao - Update enabled status`() = runBlocking {
        // Given
        val skillId = skillDao.insertSkill(SkillEntity(name = "Test", isEnabled = false))

        // When
        skillDao.updateEnabledStatus(skillId, true)

        // Then
        val result = skillDao.getSkillById(skillId)
        assertTrue(result?.isEnabled == true)
    }

    @Test
    fun `SkillDao - Increment usage count`() = runBlocking {
        // Given
        val skillId = skillDao.insertSkill(SkillEntity(name = "Test", usageCount = 0))

        // When
        skillDao.incrementUsageCount(skillId)

        // Then
        val result = skillDao.getSkillById(skillId)
        assertEquals(1, result?.usageCount)
    }

    // ==================== Task DAO Tests ====================

    @Test
    fun `TaskDao - Insert and get task by id`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        val task = TaskEntity(userId = userId, title = "Organize Desktop")
        val taskId = taskDao.insertTask(task)

        // When
        val result = taskDao.getTaskById(taskId)

        // Then
        assertNotNull(result)
        assertEquals("Organize Desktop", result?.title)
    }

    @Test
    fun `TaskDao - Get tasks by user id`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        taskDao.insertTask(TaskEntity(userId = userId, title = "Task 1"))
        taskDao.insertTask(TaskEntity(userId = userId, title = "Task 2"))

        // When
        val result = taskDao.getTasksByUserId(userId).first()

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun `TaskDao - Get tasks by status`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        taskDao.insertTask(TaskEntity(userId = userId, title = "Pending", status = 0))
        taskDao.insertTask(TaskEntity(userId = userId, title = "Completed", status = 2))

        // When
        val result = taskDao.getTasksByStatus(userId, 0).first()

        // Then
        assertEquals(1, result.size)
        assertEquals(0, result.first().status)
    }

    @Test
    fun `TaskDao - Get scheduled tasks`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        val scheduledTime = System.currentTimeMillis() + 10000
        taskDao.insertTask(TaskEntity(userId = userId, title = "Scheduled", scheduledAt = scheduledTime))

        // When
        val result = taskDao.getScheduledTasks(userId).first()

        // Then
        assertEquals(1, result.size)
    }

    @Test
    fun `TaskDao - Update task status`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        val taskId = taskDao.insertTask(TaskEntity(userId = userId, title = "Test", status = 0))

        // When
        taskDao.updateTaskStatus(taskId, 2)

        // Then
        val result = taskDao.getTaskById(taskId)
        assertEquals(2, result?.status)
    }

    @Test
    fun `TaskDao - Complete task`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        val taskId = taskDao.insertTask(TaskEntity(userId = userId, title = "Test", status = 1))
        val completionTime = System.currentTimeMillis()

        // When
        taskDao.completeTask(taskId, completionTime, "Success")

        // Then
        val result = taskDao.getTaskById(taskId)
        assertEquals(2, result?.status)
        assertEquals("Success", result?.result)
    }

    // ==================== UserPreferences DAO Tests ====================

    @Test
    fun `UserPreferencesDao - Insert and get preference by key`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        val preference = UserPreferencesEntity(
            userId = userId,
            key = "theme",
            value = "dark"
        )
        userPreferencesDao.insertPreference(preference)

        // When
        val result = userPreferencesDao.getPreferenceByKey(userId, "theme")

        // Then
        assertNotNull(result)
        assertEquals("theme", result?.key)
        assertEquals("dark", result?.value)
    }

    @Test
    fun `UserPreferencesDao - Get preferences by category`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        userPreferencesDao.insertPreference(
            UserPreferencesEntity(userId = userId, key = "key1", category = "appearance")
        )
        userPreferencesDao.insertPreference(
            UserPreferencesEntity(userId = userId, key = "key2", category = "appearance")
        )
        userPreferencesDao.insertPreference(
            UserPreferencesEntity(userId = userId, key = "key3", category = "behavior")
        )

        // When
        val result = userPreferencesDao.getPreferencesByCategory(userId, "appearance").first()

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun `UserPreferencesDao - Get preferences by user id`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        userPreferencesDao.insertPreference(UserPreferencesEntity(userId = userId, key = "pref1"))
        userPreferencesDao.insertPreference(UserPreferencesEntity(userId = userId, key = "pref2"))

        // When
        val result = userPreferencesDao.getPreferencesByUserId(userId).first()

        // Then
        assertEquals(2, result.size)
    }

    @Test
    fun `UserPreferencesDao - Update preference value`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        userPreferencesDao.insertPreference(
            UserPreferencesEntity(userId = userId, key = "theme", value = "light")
        )

        // When
        userPreferencesDao.updateValue(userId, "theme", "dark")

        // Then
        val result = userPreferencesDao.getPreferenceByKey(userId, "theme")
        assertEquals("dark", result?.value)
    }

    @Test
    fun `UserPreferencesDao - Mark as synced`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        val prefId = userPreferencesDao.insertPreference(
            UserPreferencesEntity(userId = userId, key = "test", isSynced = false)
        )

        // When
        userPreferencesDao.markAsSynced(prefId)

        // Then
        val result = userPreferencesDao.getPreferenceByKey(userId, "test")
        assertTrue(result?.isSynced == true)
    }

    @Test
    fun `UserPreferencesDao - Delete preference`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        val prefId = userPreferencesDao.insertPreference(
            UserPreferencesEntity(userId = userId, key = "test")
        )

        // When
        userPreferencesDao.deletePreference(prefId)

        // Then
        val result = userPreferencesDao.getPreferenceByKey(userId, "test")
        assertNull(result)
    }

    @Test
    fun `UserPreferencesDao - Get unsynced preferences`() = runBlocking {
        // Given
        val user = UserEntity(username = "testuser")
        val userId = userDao.insertUser(user)
        userPreferencesDao.insertPreference(
            UserPreferencesEntity(userId = userId, key = "synced", isSynced = true)
        )
        userPreferencesDao.insertPreference(
            UserPreferencesEntity(userId = userId, key = "unsynced", isSynced = false)
        )

        // When
        val result = userPreferencesDao.getUnsyncedPreferences(userId).first()

        // Then
        assertEquals(1, result.size)
        assertFalse(result.first().isSynced)
    }
}

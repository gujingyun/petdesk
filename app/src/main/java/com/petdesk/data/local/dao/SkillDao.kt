package com.petdesk.data.local.dao

import androidx.room.*
import com.petdesk.data.local.entity.SkillEntity
import kotlinx.coroutines.flow.Flow

/**
 * 技能表 DAO
 */
@Dao
interface SkillDao {
    @Query("SELECT * FROM skills ORDER BY isSystem DESC, usageCount DESC")
    fun getAllSkills(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE isEnabled = 1 ORDER BY isSystem DESC, usageCount DESC")
    fun getEnabledSkills(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE category = :category ORDER BY usageCount DESC")
    fun getSkillsByCategory(category: String): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE isSystem = 1 ORDER BY usageCount DESC")
    fun getSystemSkills(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE isSystem = 0 ORDER BY usageCount DESC")
    fun getCustomSkills(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE id = :id")
    suspend fun getSkillById(id: Long): SkillEntity?

    @Query("SELECT * FROM skills WHERE `trigger` LIKE '%' || :trigger || '%' AND isEnabled = 1")
    suspend fun getSkillByTrigger(trigger: String): SkillEntity?

    @Query("SELECT * FROM skills WHERE name LIKE '%' || :keyword || '%'")
    fun searchSkills(keyword: String): Flow<List<SkillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: SkillEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<SkillEntity>)

    @Update
    suspend fun updateSkill(skill: SkillEntity)

    @Delete
    suspend fun deleteSkill(skill: SkillEntity)

    @Query("UPDATE skills SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateEnabledStatus(id: Long, isEnabled: Boolean)

    @Query("UPDATE skills SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsageCount(id: Long)

    @Query("SELECT COUNT(*) FROM skills")
    suspend fun getSkillCount(): Int
}

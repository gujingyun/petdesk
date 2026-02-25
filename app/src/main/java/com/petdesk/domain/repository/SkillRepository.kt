package com.petdesk.domain.repository

import com.petdesk.data.local.entity.SkillEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for skill management
 */
interface SkillRepository {
    fun getAllSkills(): Flow<List<SkillEntity>>
    fun getEnabledSkills(): Flow<List<SkillEntity>>
    fun getSkillsByCategory(category: String): Flow<List<SkillEntity>>
    fun getSystemSkills(): Flow<List<SkillEntity>>
    fun getCustomSkills(): Flow<List<SkillEntity>>
    fun searchSkills(keyword: String): Flow<List<SkillEntity>>
    suspend fun getSkillById(id: Long): SkillEntity?
    suspend fun getSkillByTrigger(trigger: String): SkillEntity?
    suspend fun insertSkill(skill: SkillEntity): Long
    suspend fun insertSkills(skills: List<SkillEntity>)
    suspend fun updateSkill(skill: SkillEntity)
    suspend fun deleteSkill(skill: SkillEntity)
    suspend fun updateEnabledStatus(id: Long, isEnabled: Boolean)
    suspend fun incrementUsageCount(id: Long)
    suspend fun getSkillCount(): Int
}

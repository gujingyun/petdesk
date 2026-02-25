package com.petdesk.data.repository

import com.petdesk.data.local.dao.SkillDao
import com.petdesk.data.local.entity.SkillEntity
import com.petdesk.domain.repository.SkillRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Skill Repository Implementation
 */
@Singleton
class SkillRepositoryImpl @Inject constructor(
    private val skillDao: SkillDao
) : SkillRepository {

    override fun getAllSkills(): Flow<List<SkillEntity>> = skillDao.getAllSkills()

    override fun getEnabledSkills(): Flow<List<SkillEntity>> = skillDao.getEnabledSkills()

    override fun getSkillsByCategory(category: String): Flow<List<SkillEntity>> =
        skillDao.getSkillsByCategory(category)

    override fun getSystemSkills(): Flow<List<SkillEntity>> = skillDao.getSystemSkills()

    override fun getCustomSkills(): Flow<List<SkillEntity>> = skillDao.getCustomSkills()

    override fun searchSkills(keyword: String): Flow<List<SkillEntity>> =
        skillDao.searchSkills(keyword)

    override suspend fun getSkillById(id: Long): SkillEntity? = skillDao.getSkillById(id)

    override suspend fun getSkillByTrigger(trigger: String): SkillEntity? =
        skillDao.getSkillByTrigger(trigger)

    override suspend fun insertSkill(skill: SkillEntity): Long = skillDao.insertSkill(skill)

    override suspend fun insertSkills(skills: List<SkillEntity>) = skillDao.insertSkills(skills)

    override suspend fun updateSkill(skill: SkillEntity) = skillDao.updateSkill(skill)

    override suspend fun deleteSkill(skill: SkillEntity) = skillDao.deleteSkill(skill)

    override suspend fun updateEnabledStatus(id: Long, isEnabled: Boolean) =
        skillDao.updateEnabledStatus(id, isEnabled)

    override suspend fun incrementUsageCount(id: Long) = skillDao.incrementUsageCount(id)

    override suspend fun getSkillCount(): Int = skillDao.getSkillCount()
}

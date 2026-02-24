package com.petdesk.data.repository

import com.petdesk.data.local.PetPreferencesDataSource
import com.petdesk.domain.model.PetState
import com.petdesk.domain.repository.PetRepository
import javax.inject.Inject

/**
 * Implementation of the PetRepository interface.
 */
class PetRepositoryImpl @Inject constructor(
    private val petPreferencesDataSource: PetPreferencesDataSource
) : PetRepository {
    
    override suspend fun getPetState(): PetState = petPreferencesDataSource.getPetState()
    
    override suspend fun savePetState(state: PetState) = petPreferencesDataSource.savePetState(state)
    
    override suspend fun updateVisibility(isVisible: Boolean) {
        val currentState = petPreferencesDataSource.getPetState()
        val newState = currentState.copy(isVisible = isVisible)
        petPreferencesDataSource.savePetState(newState)
    }
    
    override suspend fun updatePosition(position: Pair<Float, Float>) {
        val currentState = petPreferencesDataSource.getPetState()
        val newPosition = com.petdesk.domain.model.PetPosition(position.first, position.second)
        val newState = currentState.copy(position = newPosition)
        petPreferencesDataSource.savePetState(newState)
    }
    
    override suspend fun updateSize(size: String) {
        val currentState = petPreferencesDataSource.getPetState()
        val newSize = when (size.lowercase()) {
            "small" -> com.petdesk.domain.model.PetSize.SMALL
            "large" -> com.petdesk.domain.model.PetSize.LARGE
            else -> com.petdesk.domain.model.PetSize.MEDIUM
        }
        val newState = currentState.copy(size = newSize)
        petPreferencesDataSource.savePetState(newState)
    }
    
    override suspend fun updateTransparency(transparency: Float) {
        val currentState = petPreferencesDataSource.getPetState()
        // Ensure transparency is within valid range [0.2, 1.0]
        val clampedTransparency = transparency.coerceIn(0.2f, 1.0f)
        val newState = currentState.copy(transparency = clampedTransparency)
        petPreferencesDataSource.savePetState(newState)
    }
}
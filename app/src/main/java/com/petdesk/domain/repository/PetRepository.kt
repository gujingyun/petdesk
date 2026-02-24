package com.petdesk.domain.repository

import com.petdesk.domain.model.PetState

/**
 * Repository interface for managing the desktop pet state.
 */
interface PetRepository {
    suspend fun getPetState(): PetState
    suspend fun savePetState(state: PetState)
    suspend fun updateVisibility(isVisible: Boolean)
    suspend fun updatePosition(position: Pair<Float, Float>)
    suspend fun updateSize(size: String)
    suspend fun updateTransparency(transparency: Float)
}
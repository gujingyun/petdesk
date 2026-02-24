package com.petdesk.domain.usecase

import com.petdesk.domain.repository.PetRepository
import javax.inject.Inject

/**
 * Use case for updating the pet position.
 */
class UpdatePetPositionUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(x: Float, y: Float) = petRepository.updatePosition(Pair(x, y))
}
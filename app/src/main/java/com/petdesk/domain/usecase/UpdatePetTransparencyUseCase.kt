package com.petdesk.domain.usecase

import com.petdesk.domain.repository.PetRepository
import javax.inject.Inject

/**
 * Use case for updating the pet transparency.
 */
class UpdatePetTransparencyUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(transparency: Float) = petRepository.updateTransparency(transparency)
}
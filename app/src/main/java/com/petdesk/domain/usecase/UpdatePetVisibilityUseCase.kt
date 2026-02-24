package com.petdesk.domain.usecase

import com.petdesk.domain.repository.PetRepository
import javax.inject.Inject

/**
 * Use case for updating the pet visibility.
 */
class UpdatePetVisibilityUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(isVisible: Boolean) = petRepository.updateVisibility(isVisible)
}
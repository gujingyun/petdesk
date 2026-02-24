package com.petdesk.domain.usecase

import com.petdesk.domain.repository.PetRepository
import javax.inject.Inject

/**
 * Use case for updating the pet size.
 */
class UpdatePetSizeUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(size: String) = petRepository.updateSize(size)
}
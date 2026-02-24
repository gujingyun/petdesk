package com.petdesk.domain.usecase

import com.petdesk.domain.model.PetState
import com.petdesk.domain.repository.PetRepository
import javax.inject.Inject

/**
 * Use case for getting the current pet state.
 */
class GetPetStateUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend operator fun invoke(): PetState = petRepository.getPetState()
}
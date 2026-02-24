package com.petdesk.domain.model

/**
 * Data class representing the state of the desktop pet.
 */
data class PetState(
    val isVisible: Boolean = true,
    val position: PetPosition = PetPosition(0f, 0f),
    val size: PetSize = PetSize.MEDIUM,
    val transparency: Float = 1.0f // 1.0 = fully opaque, 0.2 = most transparent
)
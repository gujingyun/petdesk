package com.petdesk.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petdesk.domain.model.PetState
import com.petdesk.domain.usecase.GetPetStateUseCase
import com.petdesk.domain.usecase.UpdatePetPositionUseCase
import com.petdesk.domain.usecase.UpdatePetSizeUseCase
import com.petdesk.domain.usecase.UpdatePetTransparencyUseCase
import com.petdesk.domain.usecase.UpdatePetVisibilityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing the floating window state.
 */
@HiltViewModel
class FloatingWindowViewModel @Inject constructor(
    private val getPetStateUseCase: GetPetStateUseCase,
    private val updatePetVisibilityUseCase: UpdatePetVisibilityUseCase,
    private val updatePetPositionUseCase: UpdatePetPositionUseCase,
    private val updatePetSizeUseCase: UpdatePetSizeUseCase,
    private val updatePetTransparencyUseCase: UpdatePetTransparencyUseCase
) : ViewModel() {
    
    private val _petState = MutableStateFlow(PetState())
    val petState: StateFlow<PetState> = _petState
    
    init {
        loadPetState()
    }
    
    private fun loadPetState() {
        viewModelScope.launch {
            _petState.value = getPetStateUseCase()
        }
    }
    
    fun updateVisibility(isVisible: Boolean) {
        viewModelScope.launch {
            updatePetVisibilityUseCase(isVisible)
            _petState.value = _petState.value.copy(isVisible = isVisible)
        }
    }
    
    fun updatePosition(x: Float, y: Float) {
        viewModelScope.launch {
            updatePetPositionUseCase(x, y)
            _petState.value = _petState.value.copy(position = com.petdesk.domain.model.PetPosition(x, y))
        }
    }
    
    fun updateSize(size: String) {
        viewModelScope.launch {
            updatePetSizeUseCase(size)
            val newSize = when (size.lowercase()) {
                "small" -> com.petdesk.domain.model.PetSize.SMALL
                "large" -> com.petdesk.domain.model.PetSize.LARGE
                else -> com.petdesk.domain.model.PetSize.MEDIUM
            }
            _petState.value = _petState.value.copy(size = newSize)
        }
    }
    
    fun updateTransparency(transparency: Float) {
        viewModelScope.launch {
            updatePetTransparencyUseCase(transparency)
            _petState.value = _petState.value.copy(transparency = transparency.coerceIn(0.2f, 1.0f))
        }
    }
}
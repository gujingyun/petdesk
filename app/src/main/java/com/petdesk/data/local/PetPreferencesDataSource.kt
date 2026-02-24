package com.petdesk.data.local

import android.content.Context
import androidx.core.content.edit
import com.petdesk.domain.model.PetPosition
import com.petdesk.domain.model.PetSize
import com.petdesk.domain.model.PetState
import javax.inject.Inject

/**
 * Data source for pet preferences using SharedPreferences.
 */
class PetPreferencesDataSource @Inject constructor(
    context: Context
) {
    private val prefs = context.getSharedPreferences("pet_preferences", Context.MODE_PRIVATE)
    
    fun getPetState(): PetState {
        val isVisible = prefs.getBoolean("pet_visible", true)
        val x = prefs.getFloat("pet_x", 0f)
        val y = prefs.getFloat("pet_y", 0f)
        val sizeOrdinal = prefs.getInt("pet_size", PetSize.MEDIUM.ordinal)
        val transparency = prefs.getFloat("pet_transparency", 1.0f)
        
        return PetState(
            isVisible = isVisible,
            position = PetPosition(x, y),
            size = PetSize.values().getOrNull(sizeOrdinal) ?: PetSize.MEDIUM,
            transparency = transparency
        )
    }
    
    fun savePetState(state: PetState) {
        prefs.edit {
            putBoolean("pet_visible", state.isVisible)
            putFloat("pet_x", state.position.x)
            putFloat("pet_y", state.position.y)
            putInt("pet_size", state.size.ordinal)
            putFloat("pet_transparency", state.transparency)
        }
    }
}
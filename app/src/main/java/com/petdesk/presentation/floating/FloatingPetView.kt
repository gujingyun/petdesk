package com.petdesk.presentation.floating

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.petdesk.R
import com.petdesk.domain.model.PetSize

/**
 * Composable for the floating pet view.
 */
@Composable
fun FloatingPetView(
    petSize: PetSize = PetSize.MEDIUM,
    onSizeChanged: (IntSize) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val imageSize = when (petSize) {
        PetSize.SMALL -> 48.dp
        PetSize.LARGE -> 96.dp
        else -> 64.dp // MEDIUM
    }
    
    Box(
        modifier = modifier
            .size(imageSize)
            .onSizeChanged { size ->
                onSizeChanged(size)
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_pet_default),
            contentDescription = "Desktop Pet",
            modifier = Modifier.fillMaxSize()
        )
    }
}
package com.petdesk.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.petdesk.presentation.theme.PetDeskTheme

/**
 * Main Composable for the PetDesk application.
 */
@Composable
fun PetDeskApp() {
    Text(text = "Hello PetDesk!")
}

@Preview(showBackground = true)
@Composable
fun PetDeskAppPreview() {
    PetDeskTheme {
        PetDeskApp()
    }
}
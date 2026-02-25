package com.petdesk.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.petdesk.domain.model.PetSize
import com.petdesk.domain.model.PetState
import com.petdesk.domain.usecase.GetPetStateUseCase
import com.petdesk.domain.usecase.UpdatePetPositionUseCase
import com.petdesk.domain.usecase.UpdatePetSizeUseCase
import com.petdesk.domain.usecase.UpdatePetTransparencyUseCase
import com.petdesk.domain.usecase.UpdatePetVisibilityUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for FloatingWindowViewModel.
 * Tests the W2 - Floating Window Module functionality.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FloatingWindowViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getPetStateUseCase: GetPetStateUseCase
    private lateinit var updatePetVisibilityUseCase: UpdatePetVisibilityUseCase
    private lateinit var updatePetPositionUseCase: UpdatePetPositionUseCase
    private lateinit var updatePetSizeUseCase: UpdatePetSizeUseCase
    private lateinit var updatePetTransparencyUseCase: UpdatePetTransparencyUseCase

    private lateinit var viewModel: FloatingWindowViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock use cases
        getPetStateUseCase = mockk(relaxed = true)
        updatePetVisibilityUseCase = mockk(relaxed = true)
        updatePetPositionUseCase = mockk(relaxed = true)
        updatePetSizeUseCase = mockk(relaxed = true)
        updatePetTransparencyUseCase = mockk(relaxed = true)

        // Setup default mock behavior
        coEvery { getPetStateUseCase() } returns PetState(
            isVisible = true,
            size = PetSize.MEDIUM,
            transparency = 1.0f
        )

        viewModel = FloatingWindowViewModel(
            getPetStateUseCase = getPetStateUseCase,
            updatePetVisibilityUseCase = updatePetVisibilityUseCase,
            updatePetPositionUseCase = updatePetPositionUseCase,
            updatePetSizeUseCase = updatePetSizeUseCase,
            updatePetTransparencyUseCase = updatePetTransparencyUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Test: Initial pet state is loaded on ViewModel initialization
     */
    @Test
    fun `loadPetState should load initial pet state`() = runTest {
        // Given - ViewModel is already initialized in setup
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - getPetStateUseCase should be called
        coVerify { getPetStateUseCase() }
    }

    /**
     * Test: Update visibility to hidden
     */
    @Test
    fun `updateVisibility should change visibility to false`() = runTest {
        // Given
        val isVisible = false

        // When
        viewModel.updateVisibility(isVisible)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { updatePetVisibilityUseCase(isVisible) }
        assertFalse(viewModel.petState.value.isVisible)
    }

    /**
     * Test: Update visibility to visible
     */
    @Test
    fun `updateVisibility should change visibility to true`() = runTest {
        // Given
        coEvery { getPetStateUseCase() } returns PetState(isVisible = false)
        val viewModel = FloatingWindowViewModel(
            getPetStateUseCase = getPetStateUseCase,
            updatePetVisibilityUseCase = updatePetVisibilityUseCase,
            updatePetPositionUseCase = updatePetPositionUseCase,
            updatePetSizeUseCase = updatePetSizeUseCase,
            updatePetTransparencyUseCase = updatePetTransparencyUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val isVisible = true

        // When
        viewModel.updateVisibility(isVisible)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { updatePetVisibilityUseCase(isVisible) }
        assertTrue(viewModel.petState.value.isVisible)
    }

    /**
     * Test: Update position coordinates
     */
    @Test
    fun `updatePosition should change position coordinates`() = runTest {
        // Given
        val x = 100f
        val y = 200f

        // When
        viewModel.updatePosition(x, y)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { updatePetPositionUseCase(x, y) }
        assertEquals(x, viewModel.petState.value.position.x)
        assertEquals(y, viewModel.petState.value.position.y)
    }

    /**
     * Test: Update position with negative coordinates
     */
    @Test
    fun `updatePosition should handle negative coordinates`() = runTest {
        // Given
        val x = -50f
        val y = -100f

        // When
        viewModel.updatePosition(x, y)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(x, viewModel.petState.value.position.x)
        assertEquals(y, viewModel.petState.value.position.y)
    }

    /**
     * Test: Update size to SMALL
     */
    @Test
    fun `updateSize should change size to SMALL`() = runTest {
        // Given
        val size = "small"

        // When
        viewModel.updateSize(size)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { updatePetSizeUseCase(size) }
        assertEquals(PetSize.SMALL, viewModel.petState.value.size)
    }

    /**
     * Test: Update size to MEDIUM
     */
    @Test
    fun `updateSize should change size to MEDIUM`() = runTest {
        // Given
        coEvery { getPetStateUseCase() } returns PetState(size = PetSize.SMALL)
        val viewModel = FloatingWindowViewModel(
            getPetStateUseCase = getPetStateUseCase,
            updatePetVisibilityUseCase = updatePetVisibilityUseCase,
            updatePetPositionUseCase = updatePetPositionUseCase,
            updatePetSizeUseCase = updatePetSizeUseCase,
            updatePetTransparencyUseCase = updatePetTransparencyUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val size = "medium"

        // When
        viewModel.updateSize(size)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(PetSize.MEDIUM, viewModel.petState.value.size)
    }

    /**
     * Test: Update size to LARGE
     */
    @Test
    fun `updateSize should change size to LARGE`() = runTest {
        // Given
        val size = "large"

        // When
        viewModel.updateSize(size)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { updatePetSizeUseCase(size) }
        assertEquals(PetSize.LARGE, viewModel.petState.value.size)
    }

    /**
     * Test: Update size with invalid value defaults to MEDIUM
     */
    @Test
    fun `updateSize with invalid value should default to MEDIUM`() = runTest {
        // Given
        val size = "invalid"

        // When
        viewModel.updateSize(size)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(PetSize.MEDIUM, viewModel.petState.value.size)
    }

    /**
     * Test: Update transparency to full opacity
     */
    @Test
    fun `updateTransparency should change transparency to full opacity`() = runTest {
        // Given
        val transparency = 1.0f

        // When
        viewModel.updateTransparency(transparency)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { updatePetTransparencyUseCase(transparency) }
        assertEquals(1.0f, viewModel.petState.value.transparency)
    }

    /**
     * Test: Update transparency to minimum value
     */
    @Test
    fun `updateTransparency should change transparency to minimum value`() = runTest {
        // Given
        val transparency = 0.2f

        // When
        viewModel.updateTransparency(transparency)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(0.2f, viewModel.petState.value.transparency)
    }

    /**
     * Test: Update transparency clamps value below minimum
     */
    @Test
    fun `updateTransparency should clamp value below minimum to minimum`() = runTest {
        // Given
        val transparency = 0.1f
        val expectedTransparency = 0.2f

        // When
        viewModel.updateTransparency(transparency)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(expectedTransparency, viewModel.petState.value.transparency)
    }

    /**
     * Test: Update transparency clamps value above maximum
     */
    @Test
    fun `updateTransparency should clamp value above maximum to maximum`() = runTest {
        // Given
        val transparency = 1.5f
        val expectedTransparency = 1.0f

        // When
        viewModel.updateTransparency(transparency)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(expectedTransparency, viewModel.petState.value.transparency)
    }

    /**
     * Test: Update transparency with middle value
     */
    @Test
    fun `updateTransparency should handle middle transparency value`() = runTest {
        // Given
        val transparency = 0.5f

        // When
        viewModel.updateTransparency(transparency)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(0.5f, viewModel.petState.value.transparency)
    }

    /**
     * Test: Multiple visibility toggles work correctly
     */
    @Test
    fun `multiple visibility toggles should work correctly`() = runTest {
        // When - toggle visibility
        viewModel.updateVisibility(false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertFalse(viewModel.petState.value.isVisible)

        // When - toggle back
        viewModel.updateVisibility(true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.petState.value.isVisible)
    }

    /**
     * Test: Use cases are called with correct parameters
     */
    @Test
    fun `updateSize should call use case with correct parameters`() = runTest {
        // Given
        val sizeSlot = slot<String>()

        // When
        viewModel.updateSize("large")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { updatePetSizeUseCase(capture(sizeSlot)) }
        assertEquals("large", sizeSlot.captured)
    }

    /**
     * Test: PetState is correctly copied on visibility update
     */
    @Test
    fun `petState should preserve other properties when updating visibility`() = runTest {
        // Given - initial state has specific position and size
        val initialX = 50f
        val initialY = 60f
        coEvery { getPetStateUseCase() } returns PetState(
            isVisible = true,
            size = PetSize.LARGE,
            transparency = 0.8f
        )
        val viewModel = FloatingWindowViewModel(
            getPetStateUseCase = getPetStateUseCase,
            updatePetVisibilityUseCase = updatePetVisibilityUseCase,
            updatePetPositionUseCase = updatePetPositionUseCase,
            updatePetSizeUseCase = updatePetSizeUseCase,
            updatePetTransparencyUseCase = updatePetTransparencyUseCase
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Update position first
        viewModel.updatePosition(initialX, initialY)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - update visibility
        viewModel.updateVisibility(false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - other properties should be preserved
        assertFalse(viewModel.petState.value.isVisible)
        assertEquals(initialX, viewModel.petState.value.position.x)
        assertEquals(initialY, viewModel.petState.value.position.y)
        assertEquals(PetSize.LARGE, viewModel.petState.value.size)
        assertEquals(0.8f, viewModel.petState.value.transparency)
    }
}

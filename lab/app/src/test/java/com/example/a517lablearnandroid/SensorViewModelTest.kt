package com.example.a517lablearnandroid

import android.app.Application
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SensorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val application: Application = mockk()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock Constructor of SensorTracker
        mockkConstructor(SensorTracker::class)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `sensorData updates from tracker`() = runTest {
        // Given
        val mockData = floatArrayOf(1.0f, 2.0f, 3.0f)
        
        // Mock the method of the constructed SensorTracker
        every { anyConstructed<SensorTracker>().getAccelerometerData() } returns flowOf(mockData)

        // When
        val viewModel = SensorViewModel(application)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        viewModel.sensorData.test {
            val result = awaitItem()
            assertArrayEquals(mockData, result, 0.001f)
        }
    }
}

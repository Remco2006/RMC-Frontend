package com.example.rmcfrontend.compose.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.rmcfrontend.api.models.CreateTelemetryRequest
import com.example.rmcfrontend.api.models.Telemetry
import com.example.rmcfrontend.api.models.TelemetryApiInterface
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import java.time.LocalDateTime

@ExperimentalCoroutinesApi
class TelemetryViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var telemetryApi: TelemetryApiInterface
    private lateinit var viewModel: TelemetryViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        telemetryApi = mockk(relaxed = true)
        viewModel = TelemetryViewModel(telemetryApi)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test saveTelemetry calls onSuccess when API succeeds`() = runTest {
        // Given: successful API response
        val request = CreateTelemetryRequest(
            userId = 1L,
            carId = 1L,
            avgSpeedKmh = 60.0,
            maxSpeedKmh = 100.0,
            tripDistanceKm = 25.5,
            tripDurationMin = 30,
            harshBrakes = 0,
            harshAccelerations = 1,
            corneringScore = 85,
            ecoScore = 90
        )

        val mockResponse = mockk<Response<Telemetry>>()
        every { mockResponse.isSuccessful } returns true
        coEvery { telemetryApi.createTelemetry(request) } returns mockResponse

        var successCalled = false
        var errorCalled = false

        // When: save telemetry
        viewModel.saveTelemetry(
            request = request,
            onSuccess = { successCalled = true },
            onError = { errorCalled = true }
        )
        advanceUntilIdle()

        // Then: onSuccess should be called
        assert(successCalled) { "onSuccess should have been called" }
        assert(!errorCalled) { "onError should not have been called" }
        coVerify { telemetryApi.createTelemetry(request) }
    }

    @Test
    fun `test saveTelemetry calls onError when API fails`() = runTest {
        // Given: API returns error
        val request = CreateTelemetryRequest(
            userId = 1L,
            carId = 1L,
            avgSpeedKmh = 60.0,
            maxSpeedKmh = 100.0,
            tripDistanceKm = 25.5,
            tripDurationMin = 30,
            harshBrakes = 0,
            harshAccelerations = 1,
            corneringScore = 85,
            ecoScore = 90
        )

        val mockResponse = mockk<Response<Telemetry>>()
        every { mockResponse.isSuccessful } returns false
        every { mockResponse.code() } returns 500
        coEvery { telemetryApi.createTelemetry(request) } returns mockResponse

        var successCalled = false
        var errorCalled = false
        var errorMessage = ""

        // When: save telemetry
        viewModel.saveTelemetry(
            request = request,
            onSuccess = { successCalled = true },
            onError = { msg ->
                errorCalled = true
                errorMessage = msg
            }
        )
        advanceUntilIdle()

        // Then: onError should be called
        assert(!successCalled) { "onSuccess should not have been called" }
        assert(errorCalled) { "onError should have been called" }
        assert(errorMessage.isNotEmpty()) { "Error message should not be empty" }
    }

    @Test
    fun `test loadTripsForUser updates state with trips`() = runTest {
        // Given: API returns trip list
        val userId = 1L
        val mockTrips = listOf(
            Telemetry(
                userId = userId,
                carId = 1,
                tripId = 1,
                carName = "Toyota Corolla",
                avgSpeedKmh = 55.0,
                maxSpeedKmh = 95.0,
                tripDistanceKm = 20.0,
                tripDurationMin = 25,
                timestamp = LocalDateTime.parse("2024-01-18T10:30:00"),
                harshBrakes = 0,
                harshAccelerations = 1,
                ecoScore = 88,
                corneringScore = 82
            )
        )

        val mockResponse = mockk<Response<List<Telemetry>>>()
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body() } returns mockTrips
        coEvery { telemetryApi.getTelemetryForUser(userId) } returns mockResponse

        // When: load trips
        viewModel.loadTripsForUser(userId)
        advanceUntilIdle()

        // Then: state should contain trip
        val state = viewModel.uiState.value
        assert(state.trips.size == 1) {
            "Expected 1 trip, got ${state.trips.size}"
        }
        assert(state.trips.first().userId == userId) {
            "User ID should match"
        }
        assert(!state.isLoading) {
            "Loading should be false"
        }
    }

    @Test
    fun `test clearError removes error from state`() = runTest {
        // Given: state with error
        val userId = 1L
        val mockResponse = mockk<Response<List<Telemetry>>>()
        every { mockResponse.isSuccessful } returns false
        coEvery { telemetryApi.getTelemetryForUser(userId) } returns mockResponse

        viewModel.loadTripsForUser(userId)
        advanceUntilIdle()

        // Verify error exists
        assert(viewModel.uiState.value.error != null)

        // When: clear error
        viewModel.clearError()

        // Then: error should be null
        assert(viewModel.uiState.value.error == null) {
            "Error should be null after clearError()"
        }
    }
}
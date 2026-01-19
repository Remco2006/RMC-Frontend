package com.example.rmcfrontend.compose.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.rmcfrontend.api.CarsApi
import com.example.rmcfrontend.api.ReservationsApi
import com.example.rmcfrontend.api.TermsApi
import com.example.rmcfrontend.api.models.Reservation
import com.example.rmcfrontend.api.models.Car
import com.example.rmcfrontend.compose.viewmodel.ReservationsViewModel
import com.example.rmcfrontend.api.models.response.CarsResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDateTime

// Helper function to create test reservations
private fun createTestReservation(
    id: Long = 1,
    startTime: LocalDateTime = LocalDateTime.now(),
    endTime: LocalDateTime = LocalDateTime.now().plusHours(2),
    userId: Long = 1,
    carId: Long = 1
) = Reservation(
    id = id,
    _startTime = startTime.toString(),
    _endTime = endTime.toString(),
    userId = userId,
    carId = carId
)

@ExperimentalCoroutinesApi
class ReservationsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var reservationsApi: ReservationsApi
    private lateinit var carsApi: CarsApi
    private lateinit var termsApi: TermsApi
    private lateinit var viewModel: ReservationsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Create mocks with relaxed = true to avoid errors
        reservationsApi = mockk(relaxed = true)
        carsApi = mockk(relaxed = true)
        termsApi = mockk(relaxed = true)

        // Mock the init block API calls BEFORE creating ViewModel
        coEvery { reservationsApi.getAllReservations() } returns emptyList()
        coEvery { carsApi.getAllCars() } returns CarsResponse(emptyList())

        // Create ViewModel (init block will run with mocked APIs)
        viewModel = ReservationsViewModel(reservationsApi, carsApi, termsApi)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test loadReservations updates state correctly`() = runTest(testDispatcher) {
        // Given: mock API returns 2 reservations
        val now = LocalDateTime.now()
        val mockReservations = listOf(
            createTestReservation(
                id = 1,
                startTime = now,
                endTime = now.plusHours(2),
                userId = 1,
                carId = 1
            ),
            createTestReservation(
                id = 2,
                startTime = now.plusDays(1),
                endTime = now.plusDays(1).plusHours(3),
                userId = 1,
                carId = 2
            )
        )

        coEvery { reservationsApi.getAllReservations() } returns mockReservations

        // When: load reservations
        viewModel.loadReservations()
        advanceUntilIdle()

        // Then: state should contain 2 reservations
        val state = viewModel.uiState.value

        assert(state.reservations.size == 2) {
            "Expected 2 reservations, got ${state.reservations.size}"
        }
        assert(!state.isLoading) {
            "Loading should be false, but was ${state.isLoading}"
        }
        assert(state.error == null) {
            "Error should be null, but was ${state.error}"
        }

        // Verify API was called at least once (init + manual call)
        coVerify(atLeast = 1) { reservationsApi.getAllReservations() }
    }

    @Test
    fun `test loadReservations handles errors`() = runTest(testDispatcher) {
        // Given: API throws exception
        val errorMessage = "Network error"
        coEvery { reservationsApi.getAllReservations() } throws Exception(errorMessage)

        // When: load reservations
        viewModel.loadReservations()
        advanceUntilIdle()

        // Then: state should show error
        val state = viewModel.uiState.value

        assert(!state.isLoading) {
            "Loading should be false"
        }
        assert(state.error != null) {
            "Error should not be null"
        }
        assert(state.error!!.contains("Kon reserveringen niet laden")) {
            "Error should contain 'Kon reserveringen niet laden', but was: ${state.error}"
        }
    }

    @Test
    fun `test clearError removes error from state`() = runTest(testDispatcher) {
        // Given: state has an error
        val errorMessage = "Test error"
        coEvery { reservationsApi.getAllReservations() } throws Exception(errorMessage)

        viewModel.loadReservations()
        advanceUntilIdle()

        // Verify error exists
        assert(viewModel.uiState.value.error != null)

        // When: clear error
        viewModel.clearError()
        advanceUntilIdle()

        // Then: error should be null
        val state = viewModel.uiState.value
        assert(state.error == null) {
            "Error should be null after clearError()"
        }
    }

    @Test
    fun `test loadAvailableCars updates state`() = runTest(testDispatcher) {
        // Given: mock API returns cars - FIX: Explicitly specify Car type
        val mockCars = listOf(
            mockk<Car>(relaxed = true),
            mockk<Car>(relaxed = true)
        )
        coEvery { carsApi.getAllCars() } returns CarsResponse(mockCars)

        // When: load cars
        viewModel.loadAvailableCars()
        advanceUntilIdle()

        // Then: state should contain cars
        val state = viewModel.uiState.value
        assert(state.availableCars.size == 2) {
            "Expected 2 cars, got ${state.availableCars.size}"
        }
    }
}
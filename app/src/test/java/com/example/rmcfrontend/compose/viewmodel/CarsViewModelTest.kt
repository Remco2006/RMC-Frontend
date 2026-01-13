package com.example.rmcfrontend.compose.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.rmcfrontend.api.CarsApi
import com.example.rmcfrontend.api.enums.PowerSourceTypeEnum
import com.example.rmcfrontend.api.models.Car
import com.example.rmcfrontend.api.models.CreateCarRequest
import com.example.rmcfrontend.api.models.UpdateCarRequest
import com.example.rmcfrontend.api.models.response.CarsResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CarsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockApi: CarsApi
    private lateinit var viewModel: CarsViewModel

    private val mockCar = Car(
        id = 1L,
        userId = 1L,
        make = "Toyota",
        model = "Corolla",
        price = 20000.0f,
        pickupLocation = "City Center",
        category = "Sedan",
        powerSourceType = "ICE",
        color = "Grijs",
        imageFileNames = listOf("image1.jpg"),
        engineType = "1.8L I4",
        enginePower = "90kW",
        fuelType = "Benzine",
        transmission = "Automatisch",
        interiorType = "Stof",
        interiorColor = "Zwart",
        exteriorType = "Hatchback",
        exteriorFinish = "Metallic",
        wheelSize = "16 inch",
        wheelType = "Lichtmetaal",
        seats = 5,
        doors = 4,
        modelYear = 2020,
        licensePlate = "AB-123-C",
        mileage = 45000,
        vinNumber = "JT1234567890XYZ01",
        tradeName = "Corolla 1.8 Hybrid",
        bpm = 3800f,
        curbWeight = 1250,
        maxWeight = 1800,
        firstRegistrationDate = "2020-03-15",
        bookingCost = 25.00f,
        costPerKilometer = 0.29f,
        deposit = 100f,
        createdAt = "2024-01-01T10:00:00",
        modifiedAt = "2024-01-01T10:00:00"
    )

    private val mockCarsResponse = CarsResponse(
        GetCarResponseList = listOf(mockCar)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockApi = mockk()
        viewModel = CarsViewModel(api = mockApi)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchAllCars success updates state with cars`() = runTest {
        // Given
        coEvery { mockApi.getMyCars() } returns mockCarsResponse

        // When
        viewModel.fetchAllCars()
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isBusy)
        assertNull(state.errorMessage)
        assertEquals(1, state.cars.size)
        assertEquals(mockCar, state.cars[0])

        coVerify(exactly = 1) { mockApi.getMyCars() }
    }

    @Test
    fun `fetchAllCars failure updates state with error`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { mockApi.getMyCars() } throws Exception(errorMessage)

        // When
        viewModel.fetchAllCars()
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertFalse(state.isBusy)
        assertEquals(errorMessage, state.errorMessage)
        assertTrue(state.cars.isEmpty())
    }

    @Test
    fun `fetchAllCars sets isBusy to true during loading`() = runTest {
        // Given
        coEvery { mockApi.getMyCars() } coAnswers {
            mockCarsResponse
        }

        // When
        viewModel.fetchAllCars()

        // Then - before completion
        assertTrue(viewModel.state.value.isBusy)

        advanceUntilIdle()

        // Then - after completion
        assertFalse(viewModel.state.value.isBusy)
    }

    @Test
    fun `getCar returns car successfully`() = runTest {
        // Given
        val carId = "1"
        coEvery { mockApi.getCar(carId) } returns mockCar

        // When
        val carFlow = viewModel.getCar(carId)
        advanceUntilIdle()

        // Then
        assertEquals(mockCar, carFlow.value)
        coVerify(exactly = 1) { mockApi.getCar(carId) }
    }

    @Test
    fun `getCar handles error`() = runTest {
        // Given
        val carId = "1"
        val errorMessage = "Car not found"
        coEvery { mockApi.getCar(carId) } throws Exception(errorMessage)

        // When
        val carFlow = viewModel.getCar(carId)
        advanceUntilIdle()

        // Then
        assertNull(carFlow.value)
        assertEquals(errorMessage, viewModel.state.value.errorMessage)
    }

    @Test
    fun `createCar success calls onSuccess and refreshes cars`() = runTest {
        // Given
        val request = CreateCarRequest(
            make = "Tesla",
            model = "Model 3",
            price = 45000.0f,
            pickupLocation = "Airport",
            category = "Sedan",
            powerSourceType = PowerSourceTypeEnum.HEV,
            color = "Wit",
            engineType = "Dual Motor",
            enginePower = "190kW",
            fuelType = "Elektrisch",
            transmission = "Automatisch",
            interiorType = "Leder",
            interiorColor = "Wit",
            exteriorType = "Sedan",
            exteriorFinish = "Glans",
            wheelSize = "18 inch",
            wheelType = "Alloy",
            seats = 5,
            doors = 4,
            modelYear = 2023,
            licensePlate = "EV-456-D",
            mileage = 12000,
            vinNumber = "5YJ3E1EA7LF123456",
            tradeName = "Model 3 Long Range",
            bpm = 0f,
            curbWeight = 1750,
            maxWeight = 2200,
            firstRegistrationDate = "2023-02-01",
            bookingCost = "100",
            costPerKilometer = 0.29f,
            deposit = "100",
            userId = 1
        )
        var successCalled = false

        coEvery { mockApi.createCar(request) } returns mockCar
        coEvery { mockApi.getMyCars() } returns mockCarsResponse

        // When
        viewModel.createCar(request) { successCalled = true }
        advanceUntilIdle()

        // Then
        assertTrue(successCalled)
        assertFalse(viewModel.state.value.isBusy)
        assertNull(viewModel.state.value.errorMessage)

        coVerify(exactly = 1) { mockApi.createCar(request) }
        coVerify(exactly = 1) { mockApi.getMyCars() }
    }

    @Test
    fun `createCar failure updates error state`() = runTest {
        // Given
        val request = CreateCarRequest(
            make = "Tesla",
            model = "Model 3",
            price = 45000.0f,
            pickupLocation = "Airport",
            category = "Sedan",
            powerSourceType = PowerSourceTypeEnum.ICE,
            color = "Wit",
            engineType = "Dual Motor",
            enginePower = "190kW",
            fuelType = "Elektrisch",
            transmission = "Automatisch",
            interiorType = "Leder",
            interiorColor = "Wit",
            exteriorType = "Sedan",
            exteriorFinish = "Glans",
            wheelSize = "18 inch",
            wheelType = "Alloy",
            seats = 5,
            doors = 4,
            modelYear = 2023,
            licensePlate = "EV-456-D",
            mileage = 12000,
            vinNumber = "5YJ3E1EA7LF123456",
            tradeName = "Model 3 Long Range",
            bpm = 0f,
            curbWeight = 1750,
            maxWeight = 2200,
            firstRegistrationDate = "2023-02-01",
            bookingCost = "25",
            costPerKilometer = 0.29f,
            deposit = "100",
            userId = 1,
        )
        val errorMessage = "Failed to create"
        var successCalled = false

        coEvery { mockApi.createCar(request) } throws Exception(errorMessage)

        // When
        viewModel.createCar(request) { successCalled = true }
        advanceUntilIdle()

        // Then
        assertFalse(successCalled)
        assertFalse(viewModel.state.value.isBusy)
        assertEquals(errorMessage, viewModel.state.value.errorMessage)
    }

    @Test
    fun `updateCar success calls onSuccess and refreshes cars`() = runTest {
        // Given
        val carId = "1"
        val request = UpdateCarRequest(
            id = 1L,
            make = "Toyota",
            model = "Camry",
            price = 25000.0f,
            pickupLocation = "Downtown",
            category = "Sedan",
            powerSourceType = "ICE",
            color = "Zwart",
            engineType = "2.5L I4",
            enginePower = "150kW",
            fuelType = "Benzine",
            transmission = "Automatisch",
            interiorType = "Leder",
            interiorColor = "Beige",
            exteriorType = "Sedan",
            exteriorFinish = "Metallic",
            wheelSize = "17 inch",
            wheelType = "Lichtmetaal",
            seats = 5,
            doors = 4,
            modelYear = 2022,
            licensePlate = "CD-789-E",
            mileage = 30000,
            vinNumber = "4T1B11HK5KU123456",
            tradeName = "Camry Hybrid",
            bpm = 4200f,
            curbWeight = 1450,
            maxWeight = 1950,
            firstRegistrationDate = "2022-05-10",
            bookingCost = 30.00f,
            costPerKilometer = 0.32f,
            deposit = 150f
        )
        var successCalled = false

        coEvery { mockApi.updateCar(1L, request) } returns Unit
        coEvery { mockApi.getMyCars() } returns mockCarsResponse

        // When
        viewModel.updateCar(carId, request) { successCalled = true }
        advanceUntilIdle()

        // Then
        assertTrue(successCalled)
        assertFalse(viewModel.state.value.isBusy)
        assertNull(viewModel.state.value.errorMessage)

        coVerify(exactly = 1) { mockApi.updateCar(1L, request) }
        coVerify(exactly = 1) { mockApi.getMyCars() }
    }

    @Test
    fun `updateCar failure updates error state`() = runTest {
        // Given
        val carId = "1"
        val request = UpdateCarRequest(
            id = 1L,
            make = "Toyota",
            model = "Camry",
            price = 25000.0f,
            pickupLocation = "Downtown",
            category = "Sedan",
            powerSourceType = "ICE",
            color = "Zwart",
            engineType = "2.5L I4",
            enginePower = "150kW",
            fuelType = "Benzine",
            transmission = "Automatisch",
            interiorType = "Leder",
            interiorColor = "Beige",
            exteriorType = "Sedan",
            exteriorFinish = "Metallic",
            wheelSize = "17 inch",
            wheelType = "Lichtmetaal",
            seats = 5,
            doors = 4,
            modelYear = 2022,
            licensePlate = "CD-789-E",
            mileage = 30000,
            vinNumber = "4T1B11HK5KU123456",
            tradeName = "Camry Hybrid",
            bpm = 4200f,
            curbWeight = 1450,
            maxWeight = 1950,
            firstRegistrationDate = "2022-05-10",
            bookingCost = 30.00f,
            costPerKilometer = 0.32f,
            deposit = 150f
        )
        val errorMessage = "Failed to update"
        var successCalled = false

        coEvery { mockApi.updateCar(1L, request) } throws Exception(errorMessage)

        // When
        viewModel.updateCar(carId, request) { successCalled = true }
        advanceUntilIdle()

        // Then
        assertFalse(successCalled)
        assertFalse(viewModel.state.value.isBusy)
        assertEquals(errorMessage, viewModel.state.value.errorMessage)
    }

    @Test
    fun `deleteCar success removes car from list`() = runTest {
        // Given
        val carId = 1L
        coEvery { mockApi.deleteCar(carId.toString()) } returns Unit
        coEvery { mockApi.getMyCars() } returns mockCarsResponse

        // First load some cars
        viewModel.fetchAllCars()
        advanceUntilIdle()

        // When
        viewModel.deleteCar(carId)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.state.value.isBusy)
        assertNull(viewModel.state.value.errorMessage)
        assertEquals(0, viewModel.state.value.cars.filter { it.id == carId }.size)

        coVerify(exactly = 1) { mockApi.deleteCar(carId.toString()) }
    }

    @Test
    fun `deleteCar failure updates error state`() = runTest {
        // Given
        val carId = 1L
        val errorMessage = "Failed to delete"
        coEvery { mockApi.deleteCar(carId.toString()) } throws Exception(errorMessage)

        // When
        viewModel.deleteCar(carId)
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.state.value.isBusy)
        assertEquals(errorMessage, viewModel.state.value.errorMessage)
    }

    @Test
    fun `clearError clears error message`() = runTest {
        // Given
        coEvery { mockApi.getMyCars() } throws Exception("Some error")
        viewModel.fetchAllCars()
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun `refresh is alias for fetchAllCars`() = runTest {
        // Given
        coEvery { mockApi.getMyCars() } returns mockCarsResponse

        // When
        viewModel.refresh()
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.state.value.cars.size)
        coVerify(exactly = 1) { mockApi.getMyCars() }
    }

    @Test
    fun `state flows are properly updated`() = runTest {
        // Given
        coEvery { mockApi.getMyCars() } returns mockCarsResponse

        // When
        viewModel.fetchAllCars()
        advanceUntilIdle()

        // Then - check all derived flows
        assertEquals(1, viewModel.cars.value.size)
        assertFalse(viewModel.loading.value)
        assertNull(viewModel.error.value)
    }
}
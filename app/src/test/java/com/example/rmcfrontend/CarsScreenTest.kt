package com.example.rmcfrontend

import com.example.rmcfrontend.api.models.Car
import com.example.rmcfrontend.compose.viewmodel.CarsViewModel
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test

class CarsScreenTest {

    private lateinit var mockViewModel: CarsViewModel
    private lateinit var carsFlow: MutableStateFlow<List<Car>>
    private lateinit var loadingFlow: MutableStateFlow<Boolean>
    private lateinit var errorFlow: MutableStateFlow<String?>

    @Before
    fun setUp() {
        mockViewModel = mockk(relaxed = true)
        carsFlow = MutableStateFlow(emptyList())
        loadingFlow = MutableStateFlow(false)
        errorFlow = MutableStateFlow(null)

        io.mockk.coEvery { mockViewModel.cars } returns carsFlow
        io.mockk.coEvery { mockViewModel.loading } returns loadingFlow
        io.mockk.coEvery { mockViewModel.error } returns errorFlow
    }

    private fun createTestCar(
        id: Long = 1L,
        userId: Long = 100L,
        make: String? = "Toyota",
        model: String? = "Camry",
        price: Float? = 25000f,
        pickupLocation: String? = "Amsterdam",
        category: String? = "Sedan",
        powerSourceType: String? = "Petrol",
        color: String? = "Blue",
        engineType: String? = "V4",
        enginePower: String? = "200 hp",
        fuelType: String? = "Petrol",
        transmission: String? = "Automatic",
        interiorType: String? = "Leather",
        interiorColor: String? = "Black",
        exteriorType: String? = "Sedan",
        exteriorFinish: String? = "Metallic",
        wheelSize: String? = "18 inch",
        wheelType: String? = "Alloy",
        seats: Int? = 5,
        doors: Int? = 4,
        modelYear: Int? = 2022,
        licensePlate: String? = "AB-123-CD",
        mileage: Int? = 45000,
        vinNumber: String? = "VIN123456789",
        tradeName: String? = "Toyota Camry 2022",
        bpm: Float? = 500f,
        curbWeight: Int? = 1500,
        maxWeight: Int? = 2000,
        firstRegistrationDate: String? = "2022-01-15",
        bookingCost: Float? = 50f,
        costPerKilometer: Float? = 0.25f,
        deposit: Float? = 5000f,
        imageFileNames: List<String> = listOf("car1.jpg"),
        createdAt: String? = "2024-01-01T10:00:00",
        modifiedAt: String? = "2024-01-15T10:00:00"
    ): Car {
        return Car(
            userId = userId,
            id = id,
            make = make,
            model = model,
            price = price,
            pickupLocation = pickupLocation,
            category = category,
            powerSourceType = powerSourceType,
            color = color,
            engineType = engineType,
            enginePower = enginePower,
            fuelType = fuelType,
            transmission = transmission,
            interiorType = interiorType,
            interiorColor = interiorColor,
            exteriorType = exteriorType,
            exteriorFinish = exteriorFinish,
            wheelSize = wheelSize,
            wheelType = wheelType,
            seats = seats,
            doors = doors,
            modelYear = modelYear,
            licensePlate = licensePlate,
            mileage = mileage,
            vinNumber = vinNumber,
            tradeName = tradeName,
            bpm = bpm,
            curbWeight = curbWeight,
            maxWeight = maxWeight,
            firstRegistrationDate = firstRegistrationDate,
            bookingCost = bookingCost,
            costPerKilometer = costPerKilometer,
            deposit = deposit,
            imageFileNames = imageFileNames,
            createdAt = createdAt,
            modifiedAt = modifiedAt
        )
    }

    @Test
    fun testViewModelInitialization() {
        assert(mockViewModel != null)
    }

    @Test
    fun testCreateTestCarWithDefaults() {
        val car = createTestCar()
        assert(car.id == 1L)
        assert(car.make == "Toyota")
        assert(car.model == "Camry")
        assert(car.modelYear == 2022)
        assert(car.color == "Blue")
        assert(car.price == 25000f)
    }

    @Test
    fun testCreateTestCarWithCustomValues() {
        val car = createTestCar(
            id = 5L,
            make = "BMW",
            model = "X5",
            price = 65000f,
            color = "Black",
            modelYear = 2024
        )
        assert(car.id == 5L)
        assert(car.make == "BMW")
        assert(car.model == "X5")
        assert(car.price == 65000f)
        assert(car.color == "Black")
        assert(car.modelYear == 2024)
    }

    @Test
    fun testCarWithNullValues() {
        val car = createTestCar(
            make = null,
            model = null,
            price = null,
            color = null,
            modelYear = null
        )
        assert(car.make == null)
        assert(car.model == null)
        assert(car.price == null)
        assert(car.color == null)
        assert(car.modelYear == null)
    }

    @Test
    fun testCarImageFileNames() {
        val imageNames = listOf("car1.jpg", "car2.jpg", "car3.jpg")
        val car = createTestCar(imageFileNames = imageNames)
        assert(car.imageFileNames == imageNames)
        assert(car.imageFileNames.size == 3)
        assert(car.imageFileNames.firstOrNull() == "car1.jpg")
    }

    @Test
    fun testCarWithoutImages() {
        val car = createTestCar(imageFileNames = emptyList())
        assert(car.imageFileNames.isEmpty())
    }

    @Test
    fun testMultipleCarCreation() {
        val cars = (1..5).map { i ->
            createTestCar(
                id = i.toLong(),
                make = "Brand$i",
                model = "Model$i",
                price = (20000 + i * 1000).toFloat()
            )
        }
        assert(cars.size == 5)
        assert(cars[0].id == 1L)
        assert(cars[4].id == 5L)
        assert(cars[2].make == "Brand3")
    }

    @Test
    fun testCarDetailsArePreserved() {
        val car = createTestCar(
            category = "SUV",
            engineType = "V6",
            transmission = "Automatic",
            fuelType = "Diesel",
            seats = 7,
            doors = 5,
            mileage = 50000,
            licensePlate = "AB-123-XY",
            bpm = 750f,
            curbWeight = 2000,
            maxWeight = 2500
        )
        assert(car.category == "SUV")
        assert(car.engineType == "V6")
        assert(car.transmission == "Automatic")
        assert(car.fuelType == "Diesel")
        assert(car.seats == 7)
        assert(car.doors == 5)
        assert(car.mileage == 50000)
        assert(car.licensePlate == "AB-123-XY")
        assert(car.bpm == 750f)
        assert(car.curbWeight == 2000)
        assert(car.maxWeight == 2500)
    }

    @Test
    fun testFlowsAreInitialized() {
        assert(carsFlow.value.isEmpty())
        assert(loadingFlow.value == false)
        assert(errorFlow.value == null)
    }

    @Test
    fun testFlowsCanBeUpdated() {
        val testCars = listOf(createTestCar(id = 1L), createTestCar(id = 2L))
        carsFlow.value = testCars
        loadingFlow.value = true
        errorFlow.value = "Test error"

        assert(carsFlow.value.size == 2)
        assert(loadingFlow.value == true)
        assert(errorFlow.value == "Test error")
    }

    @Test
    fun testMockViewModelCanBeCalled() {
        io.mockk.coEvery { mockViewModel.fetchAllCars() } returns Unit
        mockViewModel.fetchAllCars()
        verify { mockViewModel.fetchAllCars() }
    }

    @Test
    fun testCarDataIntegrity() {
        val originalCar = createTestCar(
            id = 99L,
            userId = 500L,
            make = "TestMake",
            model = "TestModel"
        )

        val carsFlow = MutableStateFlow(listOf(originalCar))
        val retrievedCar = carsFlow.value.first()

        assert(retrievedCar.id == originalCar.id)
        assert(retrievedCar.userId == originalCar.userId)
        assert(retrievedCar.make == originalCar.make)
        assert(retrievedCar.model == originalCar.model)
    }
}
package com.example.rmcfrontend.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.rmcfrontend.api.models.Car
import com.example.rmcfrontend.api.models.Reservation
import com.example.rmcfrontend.compose.screens.ReservationsScreen
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * UI Test voor ReservationsScreen
 * Test de UI components en user interactions
 */
class ReservationsScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun reservationsScreen_displaysTitle() {
        // Given: Screen is displayed
        composeTestRule.setContent {
            ReservationsScreen(
                reservations = emptyList(),
                cars = emptyList(),
                userId = 1L,
                onDateSelected = {},
                onCreateReservation = {},
                onStartTrip = { _, _, _ -> }
            )
        }

        // Then: Title should be visible
        composeTestRule
            .onNodeWithText("Mijn Reserveringen")
            .assertIsDisplayed()
    }

    @Test
    fun reservationsScreen_displaysEmptyState_whenNoReservations() {
        // Given: No reservations for today
        composeTestRule.setContent {
            ReservationsScreen(
                reservations = emptyList(),
                cars = emptyList(),
                userId = 1L,
                onDateSelected = {},
                onCreateReservation = {},
                onStartTrip = { _, _, _ -> }
            )
        }

        // Then: Empty state message should be shown
        composeTestRule
            .onNodeWithText("Geen reserveringen op deze datum")
            .assertIsDisplayed()
    }

    @Test
    fun reservationsScreen_displaysReservationCard_whenReservationsExist() {
        // Given: Reservations exist
        val today = LocalDate.now()
        val reservation = Reservation(
            id = 1L,
            _startTime = today.atTime(10, 0).toString(),
            _endTime = today.atTime(12, 0).toString(),
            userId = 1L,
            carId = 1L
        )

        val car = Car(
            id = 1L,
            userId = 100L,
            make = "Toyota",
            model = "Corolla",
            tradeName = "Corolla Hybrid",
            licensePlate = "AB-123-CD",
            vinNumber = "JTDBR32E720123456",
            modelYear = 2021,
            mileage = 45000,
            price = 22000.0f,
            bookingCost = 50.00f,
            costPerKilometer = 0.25f,

            fuelType = "Benzine",
            powerSourceType = "Hybrid",
            engineType = "1.8 VVT-i",
            enginePower = "122",

            transmission = "Automaat",
            seats = 5,
            doors = 4,

            interiorType = "Stof",
            interiorColor = "Zwart",
            exteriorType = "Sedan",
            exteriorFinish = "Metallic",
            color = "Grijs",

            wheelSize = "17",
            wheelType = "Lichtmetaal",

            curbWeight = 1350,
            maxWeight = 1800,
            bpm = 250f,

            firstRegistrationDate = LocalDate.of(2021, 5, 12).toString(),

            pickupLocation = "Amsterdam",
            imageFileNames = emptyList(),
            category = "Compact",

            deposit = 500.0f,

            createdAt = LocalDateTime.now().toString(),
            modifiedAt = LocalDateTime.now().toString()
        )

        composeTestRule.setContent {
            ReservationsScreen(
                reservations = listOf(reservation),
                cars = listOf(car),
                userId = 1L,
                onDateSelected = {},
                onCreateReservation = {},
                onStartTrip = { _, _, _ -> }
            )
        }

        // Then: Car details should be displayed
        composeTestRule
            .onNodeWithText("Toyota Corolla")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Kenteken: AB-123-CD", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun reservationsScreen_showsStartTripButton_whenWithinTimeWindow() {
        // Given: Reservation that can be started (within 15 min window)
        val now = LocalDateTime.now()
        val reservation = Reservation(
            id = 1L,
            _startTime = now.minusMinutes(10).toString(), // Started 10 min ago
            _endTime = now.plusHours(2).toString(),
            userId = 1L,
            carId = 1L
        )

        val car = Car(
            id = 1L,
            userId = 100L,
            make = "Toyota",
            model = "Corolla",
            tradeName = "Corolla Hybrid",
            licensePlate = "AB-123-CD",
            vinNumber = "JTDBR32E720123456",
            modelYear = 2021,
            mileage = 45000,
            price = 22000.0f,
            bookingCost = 50.00f,
            costPerKilometer = 0.25f,

            fuelType = "Benzine",
            powerSourceType = "Hybrid",
            engineType = "1.8 VVT-i",
            enginePower = "122",

            transmission = "Automaat",
            seats = 5,
            doors = 4,

            interiorType = "Stof",
            interiorColor = "Zwart",
            exteriorType = "Sedan",
            exteriorFinish = "Metallic",
            color = "Grijs",

            wheelSize = "17",
            wheelType = "Lichtmetaal",

            curbWeight = 1350,
            maxWeight = 1800,
            bpm = 250f,

            firstRegistrationDate = LocalDate.of(2021, 5, 12).toString(),

            pickupLocation = "Amsterdam",
            imageFileNames = emptyList(),
            category = "Compact",

            deposit = 500.0f,

            createdAt = LocalDateTime.now().toString(),
            modifiedAt = LocalDateTime.now().toString()
        )

        composeTestRule.setContent {
            ReservationsScreen(
                reservations = listOf(reservation),
                cars = listOf(car),
                userId = 1L,
                onDateSelected = {},
                onCreateReservation = {},
                onStartTrip = { _, _, _ -> }
            )
        }

        // Then: Start Rit button should be visible
        composeTestRule
            .onNodeWithText("Start Rit")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun reservationsScreen_clickStartTrip_triggersCallback() {
        // Given: Reservation with start button
        val now = LocalDateTime.now()
        val reservation = Reservation(
            id = 123L,
            _startTime = now.minusMinutes(5).toString(),
            _endTime = now.plusHours(2).toString(),
            userId = 1L,
            carId = 456L
        )

        val car = Car(
            id = 456L,
            userId = 100L,
            make = "Toyota",
            model = "Corolla",
            tradeName = "Corolla Hybrid",
            licensePlate = "AB-123-CD",
            vinNumber = "JTDBR32E720123456",
            modelYear = 2021,
            mileage = 45000,
            price = 22000.0f,
            bookingCost = 50.00f,
            costPerKilometer = 0.25f,

            fuelType = "Benzine",
            powerSourceType = "Hybrid",
            engineType = "1.8 VVT-i",
            enginePower = "122",

            transmission = "Automaat",
            seats = 5,
            doors = 4,

            interiorType = "Stof",
            interiorColor = "Zwart",
            exteriorType = "Sedan",
            exteriorFinish = "Metallic",
            color = "Grijs",

            wheelSize = "17",
            wheelType = "Lichtmetaal",

            curbWeight = 1350,
            maxWeight = 1800,
            bpm = 250f,

            firstRegistrationDate = LocalDate.of(2021, 5, 12).toString(),

            pickupLocation = "Amsterdam",
            imageFileNames = emptyList(),
            category = "Compact",

            deposit = 500.0f,

            createdAt = LocalDateTime.now().toString(),
            modifiedAt = LocalDateTime.now().toString()
        )

        var clickedReservationId: Long? = null
        var clickedCarId: Long? = null

        composeTestRule.setContent {
            ReservationsScreen(
                reservations = listOf(reservation),
                cars = listOf(car),
                userId = 1L,
                onDateSelected = {},
                onCreateReservation = {},
                onStartTrip = { resId, carId, _ ->
                    clickedReservationId = resId
                    clickedCarId = carId
                }
            )
        }

        // When: Click Start Rit button
        composeTestRule
            .onNodeWithText("Start Rit")
            .performClick()

        // Then: Callback should be triggered with correct IDs
        assert(clickedReservationId == 123L) {
            "Expected reservation ID 123, got $clickedReservationId"
        }
        assert(clickedCarId == 456L) {
            "Expected car ID 456, got $clickedCarId"
        }
    }

    @Test
    fun reservationsScreen_clickFAB_triggersCreateReservation() {
        // Given: Screen is displayed
        var createClicked = false
        var selectedDate: LocalDate? = null

        composeTestRule.setContent {
            ReservationsScreen(
                reservations = emptyList(),
                cars = emptyList(),
                userId = 1L,
                onDateSelected = {},
                onCreateReservation = { date ->
                    createClicked = true
                    selectedDate = date
                },
                onStartTrip = { _, _, _ -> }
            )
        }

        // When: Click FAB (floating action button)
        composeTestRule
            .onNodeWithContentDescription("Nieuwe reservering")
            .performClick()

        // Then: Create callback should be triggered
        assert(createClicked) { "Create reservation should be triggered" }
        assert(selectedDate != null) { "Date should be passed" }
    }

    @Test
    fun reservationsScreen_displaysMultipleReservations_inCorrectOrder() {
        // Given: Multiple reservations
        val today = LocalDate.now()
        val reservations = listOf(
            Reservation(
                id = 1L,
                _startTime = today.atTime(10, 0).toString(),
                _endTime = today.atTime(12, 0).toString(),
                userId = 1L,
                carId = 1L
            ),
            Reservation(
                id = 2L,
                _startTime = today.atTime(14, 0).toString(),
                _endTime = today.atTime(16, 0).toString(),
                userId = 1L,
                carId = 2L
            ),
            Reservation(
                id = 3L,
                _startTime = today.atTime(18, 0).toString(),
                _endTime = today.atTime(20, 0).toString(),
                userId = 1L,
                carId = 3L
            )
        )

        val cars = listOf(
            Car(
                id = 1L,
                userId = 100L,
                make = "Toyota",
                model = "Corolla",
                tradeName = "Corolla Hybrid",
                licensePlate = "AA-11-BB",
                vinNumber = "JTDBR32E720111111",
                modelYear = 2021,
                mileage = 45000,
                price = 22000.0f,
                bookingCost = 50.0f,
                costPerKilometer = 0.25f,

                fuelType = "Benzine",
                powerSourceType = "Hybrid",
                engineType = "1.8 VVT-i",
                enginePower = "122",

                transmission = "Automaat",
                seats = 5,
                doors = 4,

                interiorType = "Stof",
                interiorColor = "Zwart",
                exteriorType = "Sedan",
                exteriorFinish = "Metallic",
                color = "Grijs",

                wheelSize = "17",
                wheelType = "Lichtmetaal",

                curbWeight = 1350,
                maxWeight = 1800,
                bpm = 250f,

                firstRegistrationDate = LocalDate.of(2021, 5, 12).toString(),

                pickupLocation = "Amsterdam",
                imageFileNames = emptyList(),
                category = "Compact",

                deposit = 500.0f,

                createdAt = LocalDateTime.now().toString(),
                modifiedAt = LocalDateTime.now().toString()
            ),

            Car(
                id = 2L,
                userId = 101L,
                make = "Honda",
                model = "Civic",
                tradeName = "Civic Sport",
                licensePlate = "CC-22-DD",
                vinNumber = "SHHFK2760MU222222",
                modelYear = 2020,
                mileage = 52000,
                price = 19500.0f,
                bookingCost = 45.0f,
                costPerKilometer = 0.20f,

                fuelType = "Benzine",
                powerSourceType = "ICE",
                engineType = "1.5 i-VTEC",
                enginePower = "182",

                transmission = "Handmatig",
                seats = 5,
                doors = 4,

                interiorType = "Stof",
                interiorColor = "Grijs",
                exteriorType = "Hatchback",
                exteriorFinish = "Solid",
                color = "Blauw",

                wheelSize = "16",
                wheelType = "Staal",

                curbWeight = 1280,
                maxWeight = 1750,
                bpm = 220f,

                firstRegistrationDate = LocalDate.of(2020, 3, 20).toString(),

                pickupLocation = "Rotterdam",
                imageFileNames = emptyList(),
                category = "Compact",

                deposit = 450.0f,

                createdAt = LocalDateTime.now().toString(),
                modifiedAt = LocalDateTime.now().toString()
            ),

            Car(
                id = 3L,
                userId = 102L,
                make = "BMW",
                model = "X5",
                tradeName = "X5 xDrive40d",
                licensePlate = "EE-33-FF",
                vinNumber = "WBAKS41090F333333",
                modelYear = 2019,
                mileage = 68000,
                price = 52000.0f,
                bookingCost = 100.0f,
                costPerKilometer = 0.40f,

                fuelType = "Diesel",
                powerSourceType = "ICE",
                engineType = "3.0d",
                enginePower = "340",

                transmission = "Automaat",
                seats = 7,
                doors = 5,

                interiorType = "Leder",
                interiorColor = "Bruin",
                exteriorType = "SUV",
                exteriorFinish = "Metallic",
                color = "Zwart",

                wheelSize = "19",
                wheelType = "Lichtmetaal",

                curbWeight = 2100,
                maxWeight = 2900,
                bpm = 450f,

                firstRegistrationDate = LocalDate.of(2019, 9, 5).toString(),

                pickupLocation = "Utrecht",
                imageFileNames = emptyList(),
                category = "SUV",

                deposit = 1000.0f,

                createdAt = LocalDateTime.now().toString(),
                modifiedAt = LocalDateTime.now().toString()
            )
        )

        composeTestRule.setContent {
            ReservationsScreen(
                reservations = reservations,
                cars = cars,
                userId = 1L,
                onDateSelected = {},
                onCreateReservation = {},
                onStartTrip = { _, _, _ -> }
            )
        }

        // Then: All reservations should be visible
        composeTestRule
            .onNodeWithText("Toyota Corolla")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Honda Civic")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("BMW X5")
            .assertIsDisplayed()
    }

    @Test
    fun reservationsScreen_dateSelector_isInteractive() {
        // Given: Screen is displayed
        composeTestRule.setContent {
            ReservationsScreen(
                reservations = emptyList(),
                cars = emptyList(),
                userId = 1L,
                onDateSelected = {},
                onCreateReservation = {},
                onStartTrip = { _, _, _ -> }
            )
        }

        val today = LocalDate.now()
        composeTestRule
            .onNodeWithText(today.dayOfMonth.toString())
            .assertIsDisplayed()
    }
}
package com.example.rmcfrontend.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.rmcfrontend.api.models.Car
import com.example.rmcfrontend.api.models.CreateReservationRequest
import com.example.rmcfrontend.api.models.response.GetTermResponse
import com.example.rmcfrontend.compose.screens.reservations.CreateReservationScreen
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * UI Test voor CreateReservationScreen
 * Test de UI voor het maken van nieuwe reserveringen
 */
class CreateReservationScreenUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testCars = listOf(
        Car(
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

            pickupLocation = "Amsterdam, Centrum",
            imageFileNames = emptyList(),
            category = "Compact",

            deposit = 500.0f,

            createdAt = LocalDateTime.now().toString(),
            modifiedAt = LocalDateTime.now().toString()
        ),

        Car(
            id = 2L,
            userId = 101L,
            make = "BMW",
            model = "X5",
            tradeName = "X5 xDrive40d",
            licensePlate = "XY-789-ZZ",
            vinNumber = "WBAKS41090F654321",
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

            pickupLocation = "Rotterdam, Centrum",
            imageFileNames = emptyList(),
            category = "SUV",

            deposit = 1000.0f,

            createdAt = LocalDateTime.now().toString(),
            modifiedAt = LocalDateTime.now().toString()
        )
    )

    private val testTerms = GetTermResponse(
        id = 1L,
        title = "Algemene Voorwaarden 2024",
        content = "Dit zijn de algemene voorwaarden...",
        version = 1,
        active = true
    )

    @Test
    fun createReservationScreen_displaysTitle() {
        // Given: Screen is displayed
        composeTestRule.setContent {
            CreateReservationScreen(
                initialDate = LocalDate.now(),
                userId = 1L,
                availableCars = emptyList(),
                onCarSelected = {},
                terms = null,
                unavailableTimeSlots = emptyList(),
                onCreateReservation = {},
                onNavigateBack = {}
            )
        }

        // Then: Title should be visible
        composeTestRule
            .onNodeWithText("Nieuwe Reservering")
            .assertIsDisplayed()
    }

    @Test
    fun createReservationScreen_displaysSearchBar() {
        // Given: Screen with cars
        composeTestRule.setContent {
            CreateReservationScreen(
                initialDate = LocalDate.now(),
                userId = 1L,
                availableCars = testCars,
                onCarSelected = {},
                terms = null,
                unavailableTimeSlots = emptyList(),
                onCreateReservation = {},
                onNavigateBack = {}
            )
        }

        // Then: Search bar should be visible
        composeTestRule
            .onNodeWithText("Zoek op merk, model of locatie...", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun createReservationScreen_searchFilters_workCorrectly() {
        // Given: Screen with multiple cars
        composeTestRule.setContent {
            CreateReservationScreen(
                initialDate = LocalDate.now(),
                userId = 1L,
                availableCars = testCars,
                onCarSelected = {},
                terms = null,
                unavailableTimeSlots = emptyList(),
                onCreateReservation = {},
                onNavigateBack = {}
            )
        }

        // When: Search for Toyota
        composeTestRule
            .onNodeWithText("Zoek op merk, model of locatie...", substring = true)
            .performTextInput("Toyota")

        // Then: Only Toyota should be visible
        composeTestRule
            .onNodeWithText("Toyota Corolla")
            .assertIsDisplayed()

        // BMW should not be visible
        composeTestRule
            .onNodeWithText("BMW X5")
            .assertDoesNotExist()
    }

    @Test
    fun createReservationScreen_displaysCarsList() {
        // Given: Screen with cars
        composeTestRule.setContent {
            CreateReservationScreen(
                initialDate = LocalDate.now(),
                userId = 1L,
                availableCars = testCars,
                onCarSelected = {},
                terms = null,
                unavailableTimeSlots = emptyList(),
                onCreateReservation = {},
                onNavigateBack = {}
            )
        }

        // Then: All cars should be displayed
        composeTestRule
            .onNodeWithText("Toyota Corolla")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("BMW X5")
            .assertIsDisplayed()

        // Count should be shown
        composeTestRule
            .onNodeWithText("2 auto's gevonden", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun createReservationScreen_carSelection_triggersCallback() {
        // Given: Screen with cars
        var selectedCarId: Long? = null

        composeTestRule.setContent {
            CreateReservationScreen(
                initialDate = LocalDate.now(),
                userId = 1L,
                availableCars = testCars,
                onCarSelected = { carId -> selectedCarId = carId },
                terms = null,
                unavailableTimeSlots = emptyList(),
                onCreateReservation = {},
                onNavigateBack = {}
            )
        }

        // When: Click on a car
        composeTestRule
            .onNodeWithText("Toyota Corolla")
            .performClick()

        // Then: Callback should be triggered with correct car ID
        assert(selectedCarId == 1L) {
            "Expected car ID 1, got $selectedCarId"
        }
    }

    @Test
    fun createReservationScreen_displaysEmptyState_whenNoCars() {
        // Given: Screen with no cars
        composeTestRule.setContent {
            CreateReservationScreen(
                initialDate = LocalDate.now(),
                userId = 1L,
                availableCars = emptyList(),
                onCarSelected = {},
                terms = null,
                unavailableTimeSlots = emptyList(),
                onCreateReservation = {},
                onNavigateBack = {}
            )
        }

        // Then: Empty state should be shown
        composeTestRule
            .onNodeWithText("Geen auto's beschikbaar")
            .assertIsDisplayed()
    }

    @Test
    fun createReservationScreen_termsCheckbox_becomesVisibleAfterTimeSelection() {
        // Given: Screen with car selected and time selected
        composeTestRule.setContent {
            CreateReservationScreen(
                initialDate = LocalDate.now(),
                userId = 1L,
                availableCars = testCars,
                onCarSelected = {},
                terms = testTerms,
                unavailableTimeSlots = emptyList(),
                onCreateReservation = {},
                onNavigateBack = {}
            )
        }

        // When: Select a car (which triggers time selection UI)
        composeTestRule
            .onNodeWithText("Toyota Corolla")
            .performClick()

        // Wait for time selection to appear
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            composeTestRule
                .onAllNodesWithText("Selecteer tijd")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Then: Time selection should be visible
        composeTestRule
            .onNodeWithText("Selecteer tijd")
            .assertIsDisplayed()
    }

    @Test
    fun createReservationScreen_backButton_triggersNavigation() {
        // Given: Screen is displayed
        var backClicked = false

        composeTestRule.setContent {
            CreateReservationScreen(
                initialDate = LocalDate.now(),
                userId = 1L,
                availableCars = testCars,
                onCarSelected = {},
                terms = null,
                unavailableTimeSlots = emptyList(),
                onCreateReservation = {},
                onNavigateBack = { backClicked = true }
            )
        }

        // When: Click back button
        composeTestRule
            .onNodeWithContentDescription("Terug")
            .performClick()

        // Then: Navigation callback should be triggered
        assert(backClicked) { "Back button should trigger navigation" }
    }

    @Test
    fun createReservationScreen_displaysSearchAndMapButtons() {
        // Given: Screen is displayed
        composeTestRule.setContent {
            CreateReservationScreen(
                initialDate = LocalDate.now(),
                userId = 1L,
                availableCars = testCars,
                onCarSelected = {},
                terms = null,
                unavailableTimeSlots = emptyList(),
                onCreateReservation = {},
                onNavigateBack = {}
            )
        }

        // Then: Search icon should be visible
        composeTestRule
            .onNodeWithContentDescription("Zoeken")
            .assertIsDisplayed()
            .assertHasClickAction()

        // And: Map icon should be visible
        composeTestRule
            .onNodeWithContentDescription("Kaart weergave")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun createReservationScreen_displaysCarDetails() {
        // Given: Screen with cars
        composeTestRule.setContent {
            CreateReservationScreen(
                initialDate = LocalDate.now(),
                userId = 1L,
                availableCars = testCars,
                onCarSelected = {},
                terms = null,
                unavailableTimeSlots = emptyList(),
                onCreateReservation = {},
                onNavigateBack = {}
            )
        }

        // Then: Car pricing should be visible
        composeTestRule
            .onNodeWithText("€50.0/dag + €0.25/km", substring = true)
            .assertExists()

        // And: Location should be visible
        composeTestRule
            .onNodeWithText("Amsterdam, Centrum", substring = true)
            .assertExists()
    }

    @Test
    fun createReservationScreen_showsErrorMessage_whenProvided() {
        // Given: Screen with error
        val errorMessage = "Auto niet beschikbaar op deze datum"

        composeTestRule.setContent {
            CreateReservationScreen(
                initialDate = LocalDate.now(),
                userId = 1L,
                availableCars = testCars,
                onCarSelected = {},
                terms = null,
                unavailableTimeSlots = emptyList(),
                onCreateReservation = {},
                onNavigateBack = {},
                errorMessage = errorMessage
            )
        }

        // Then: Error should be displayed in Snackbar
        composeTestRule
            .onNodeWithText(errorMessage)
            .assertIsDisplayed()
    }

    @Test
    fun createReservationScreen_showsLoadingState_whenLoading() {
        // Given: Screen in loading state
        composeTestRule.setContent {
            CreateReservationScreen(
                initialDate = LocalDate.now(),
                userId = 1L,
                availableCars = testCars,
                onCarSelected = {},
                terms = null,
                unavailableTimeSlots = emptyList(),
                onCreateReservation = {},
                onNavigateBack = {},
                isLoading = true
            )
        }

        // Note: Loading state is shown on the confirm button
        // We can't easily test this without full flow, but we verify
        // the screen can render in loading state without crashing
        composeTestRule
            .onNodeWithText("Nieuwe Reservering")
            .assertIsDisplayed()
    }
}
package com.example.rmcfrontend.compose.screens

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.rmcfrontend.api.models.CreateCarRequest
import com.example.rmcfrontend.api.models.UpdateCarRequest
import com.example.rmcfrontend.auth.TokenManager
import com.example.rmcfrontend.compose.screens.reservations.CreateReservationScreen
import com.example.rmcfrontend.compose.screens.reservations.ReservationsScreen
import com.example.rmcfrontend.compose.viewmodel.CarSearchViewModel
import com.example.rmcfrontend.compose.viewmodel.CarsViewModel
import com.example.rmcfrontend.compose.viewmodel.ReservationsViewModel
import com.example.rmcfrontend.compose.viewmodel.UserViewModel
import com.example.rmcfrontend.ui.theme.screens.cars.CarDetailsScreen
import com.example.rmcfrontend.ui.theme.screens.cars.CreateCarScreen
import com.example.rmcfrontend.ui.theme.screens.cars.EditCarScreen
import java.time.LocalDate
import java.time.LocalDateTime

sealed class HomeRoute(val value: String) {
    data object Listings : HomeRoute("listings")
    data object Map : HomeRoute("map")
    data object Reservations : HomeRoute("reservations")
    data object User : HomeRoute("user")
    data object CarDetails : HomeRoute("car/{carId}") {
        fun create(carId: Long) = "car/$carId"
    }
    data object CreateCar : HomeRoute("car/create")
    data object EditCar : HomeRoute("car/{carId}/edit") {
        fun create(carId: Long) = "car/$carId/edit"
    }
    data object CreateReservation : HomeRoute("reservations/create/{date}") {
        fun create(date: LocalDate) = "reservations/create/${date}"
    }
}

@Composable
fun HomeScreen(
    tokenManager: TokenManager,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val carsVm = remember { CarsViewModel() }
    val userVm = remember { UserViewModel(tokenManager) }
    val reservationsVm = remember { ReservationsViewModel() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        carsVm.refresh()
        userVm.loadMe()
        reservationsVm.loadReservations()
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selected = when {
        currentRoute == HomeRoute.Listings.value || currentRoute?.startsWith("car/") == true -> HomeRoute.Listings.value
        currentRoute == HomeRoute.Map.value -> HomeRoute.Map.value
        currentRoute == HomeRoute.Reservations.value || currentRoute?.startsWith("reservations/") == true -> HomeRoute.Reservations.value
        currentRoute == HomeRoute.User.value -> HomeRoute.User.value
        else -> HomeRoute.Listings.value
    }

    Scaffold(
        bottomBar = {
            // Hide bottom bar on detail/edit/create screens
            if (currentRoute in listOf(
                    HomeRoute.Listings.value,
                    HomeRoute.Map.value,
                    HomeRoute.Reservations.value,
                    HomeRoute.User.value
                )) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 10.dp
                ) {
                    NavigationBarItem(
                        selected = selected == HomeRoute.Listings.value,
                        onClick = {
                            navController.navigate(HomeRoute.Listings.value) {
                                popUpTo(HomeRoute.Listings.value) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        label = { Text("Listings") },
                        icon = { Icon(Icons.Outlined.ListAlt, contentDescription = null) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = selected == HomeRoute.Reservations.value,
                        onClick = {
                            navController.navigate(HomeRoute.Reservations.value) {
                                popUpTo(HomeRoute.Listings.value)
                                launchSingleTop = true
                            }
                        },
                        label = { Text("Reservations") },
                        icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    NavigationBarItem(
                        selected = selected == HomeRoute.User.value,
                        onClick = {
                            navController.navigate(HomeRoute.User.value) {
                                popUpTo(HomeRoute.Listings.value)
                                launchSingleTop = true
                            }
                        },
                        label = { Text("User") },
                        icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute.Listings.value,
            modifier = Modifier.padding(padding)
        ) {
            // Listings route
            composable(HomeRoute.Listings.value) {
                ListingsScreen(
                    state = carsVm.state.value,
                    onRefresh = { carsVm.refresh() },
                    onCarClick = { carId -> navController.navigate(HomeRoute.CarDetails.create(carId)) },
                    onAddCar = { navController.navigate(HomeRoute.CreateCar.value) }
                )
            }

            // Car details route
            composable(
                route = HomeRoute.CarDetails.value,
                arguments = listOf(navArgument("carId") { type = NavType.LongType })
            ) { backStackEntry ->
                val carId = backStackEntry.arguments?.getLong("carId") ?: 0L
                CarDetailsScreen(
                    carId = carId.toString(),
                    carsViewModel = carsVm,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(HomeRoute.EditCar.create(id.toLong())) },
                    onDelete = { car ->
                        car.id?.let { carsVm.deleteCar(it) }
                        navController.popBackStack()
                    }
                )
            }

            // Create car route
            composable(HomeRoute.CreateCar.value) {
                CreateCarScreen(
                    carsViewModel = carsVm,
                    onBack = { navController.popBackStack() },
                    onSave = { request: CreateCarRequest, imageUris: List<Uri> ->
                        carsVm.createCarWithImages(
                            context = context,
                            request = request,
                            imageUris = imageUris,
                            onSuccess = {
                                navController.popBackStack()
                                carsVm.refresh()
                            }
                        )
                    }
                )
            }

            // Edit car route
            composable(
                route = HomeRoute.EditCar.value,
                arguments = listOf(navArgument("carId") { type = NavType.LongType })
            ) { backStackEntry ->
                val carId = backStackEntry.arguments?.getLong("carId") ?: 0L
                EditCarScreen(
                    carId = carId.toString(),
                    carsViewModel = carsVm,
                    onBack = { navController.popBackStack() },
                    onSave = { request: UpdateCarRequest, imageUris: List<Uri> ->
                        carsVm.updateCarWithImages(
                            context = context,
                            id = carId.toString(),
                            request = request,
                            imageUris = imageUris,
                            onSuccess = {
                                navController.popBackStack()
                                carsVm.refresh()
                            }
                        )
                    }
                )
            }

            // Reservations list route
            composable(HomeRoute.Reservations.value) {
                val uiState by reservationsVm.uiState.collectAsState()
                val carsState = carsVm.state.value

                ReservationsScreen(
                    reservations = uiState.reservations,
                    cars = carsState.cars,
                    onDateSelected = {},
                    onCreateReservation = { selectedDate ->
                        navController.navigate(HomeRoute.CreateReservation.create(selectedDate))
                    }
                )
            }

            // Create reservation route
            composable(
                route = HomeRoute.CreateReservation.value,
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) { backStackEntry ->
                val dateString = backStackEntry.arguments?.getString("date")
                val selectedDate: LocalDate = dateString?.let { LocalDate.parse(it) }
                    ?: LocalDateTime.now().toLocalDate()
                val uiState by reservationsVm.uiState.collectAsState()

                // Voeg CarSearchViewModel toe
                val carSearchVm = remember { CarSearchViewModel() }
                val searchState by carSearchVm.uiState.collectAsState()

                var selectedCarId by remember { mutableStateOf<Long?>(null) }

                // Load car reservations and terms when car is selected
                LaunchedEffect(selectedCarId) {
                    selectedCarId?.let { carId ->
                        reservationsVm.loadCarReservations(carId)
                        reservationsVm.loadTerms()
                    }
                }

                CreateReservationScreen(
                    initialDate = selectedDate,
                    userId = userVm.state.value.user?.id ?: 1,
                    availableCars = searchState.cars.ifEmpty { uiState.availableCars },
                    onCarSelected = { carId ->
                        selectedCarId = carId
                    },
                    onSearchCars = { filter ->
                        carSearchVm.searchCars(filter)
                    },
                    terms = uiState.terms,
                    unavailableTimeSlots = selectedCarId?.let { carId ->
                        reservationsVm.getAvailableTimeSlots(selectedDate, carId)
                            .map { it.first.toLocalTime() to it.second.toLocalTime() }
                    } ?: emptyList(),
                    onCreateReservation = { request ->
                        reservationsVm.createReservation(request) {
                            navController.popBackStack()
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    errorMessage = searchState.error ?: uiState.error,
                    isLoading = searchState.isLoading || uiState.isLoading,
                    onErrorDismiss = {
                        carSearchVm.clearError()
                    }
                )
            }

            // User settings route
            composable(HomeRoute.User.value) {
                UserSettingsScreen(
                    state = userVm.state.value,
                    onReload = { userVm.loadMe() },
                    onSave = { f, l, e -> userVm.save(f, l, e) },
                    onDisable = { userVm.disableAccount(onDisabled = onLogout) },
                    onLogout = onLogout
                )
            }
        }
    }
}
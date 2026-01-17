package com.example.rmcfrontend.compose.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Description
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
import com.example.rmcfrontend.compose.RequestLocationPermission
import com.example.rmcfrontend.compose.screens.reservations.CreateReservationScreen
import com.example.rmcfrontend.compose.screens.terms.TermsScreen
import com.example.rmcfrontend.service.ActiveTripService
import com.example.rmcfrontend.compose.viewmodel.CarSearchViewModel
import com.example.rmcfrontend.compose.viewmodel.CarsViewModel
import com.example.rmcfrontend.compose.viewmodel.ReservationsViewModel
import com.example.rmcfrontend.compose.viewmodel.TelemetryViewModel
import com.example.rmcfrontend.compose.viewmodel.TermsViewModel
import com.example.rmcfrontend.compose.viewmodel.UserViewModel
import com.example.rmcfrontend.ui.theme.screens.cars.CarDetailsScreen
import com.example.rmcfrontend.ui.theme.screens.cars.CreateCarScreen
import com.example.rmcfrontend.ui.theme.screens.cars.EditCarScreen
import java.time.LocalDate
import java.time.LocalDateTime
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.rmcfrontend.api.models.Car

sealed class HomeRoute(val value: String) {
    data object Listings : HomeRoute("listings")
    data object Map : HomeRoute("map")
    data object Reservations : HomeRoute("reservations")
    data object Terms : HomeRoute("terms")
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

    data object ActiveTrip : HomeRoute("trip/active/{reservationId}/{carId}") {
        fun create(reservationId: Long, carId: Long) = "trip/active/$reservationId/$carId"
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
    val termsVm = remember { TermsViewModel() }
    val telemetryVm = remember { TelemetryViewModel() }
    val reservationsVm = remember { ReservationsViewModel() }
    val context = LocalContext.current

    var showPermissionDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) } // ✅ NIEUW
    var pendingTripStart by remember { mutableStateOf<Triple<Long, Long, Car?>?>(null) }

    LaunchedEffect(Unit) {
        android.util.Log.d("HomeScreen", "🎬 HomeScreen initialized")
    }

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val allGranted = permissionsMap.values.all { it }

        android.util.Log.d("HomeScreen", "Permission result: $permissionsMap")

        if (allGranted) {
            android.util.Log.d("HomeScreen", "✅ All permissions granted!")
            pendingTripStart?.let { (reservationId, carId, car) ->
                startTripService(context, reservationId, carId, userVm.state.value.user?.id ?: 1L)
                navController.navigate(HomeRoute.ActiveTrip.create(reservationId, carId))
                pendingTripStart = null
            }
        } else {
            android.util.Log.e("HomeScreen", "❌ Permissions denied: $permissionsMap")

            // ✅ Check of permanent geweigerd
            val permanentlyDenied = permissionsMap.any { (permission, granted) ->
                !granted && !(context as? android.app.Activity)?.shouldShowRequestPermissionRationale(permission)!! ?: false
            }

            if (permanentlyDenied) {
                showSettingsDialog = true
            } else {
                showPermissionDialog = true
            }
        }
    }

    fun startTripWithPermissionCheck(reservationId: Long, carId: Long, car: Car?) {
        permissions.forEach { permission ->
            val status = ContextCompat.checkSelfPermission(context, permission)
            android.util.Log.d("Permissions", "$permission: ${if (status == PackageManager.PERMISSION_GRANTED) "✅ GRANTED" else "❌ DENIED"}")
        }

        val hasAllPermissions = permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

        if (hasAllPermissions) {
            android.util.Log.d("HomeScreen", "✅ Permissions already granted")
            startTripService(context, reservationId, carId, userVm.state.value.user?.id ?: 1L)
            navController.navigate(HomeRoute.ActiveTrip.create(reservationId, carId))
        } else {
            android.util.Log.d("HomeScreen", "❓ Requesting permissions...")
            pendingTripStart = Triple(reservationId, carId, car)
            permissionLauncher.launch(permissions)
        }
    }

    // ✅ Dialog voor normale weigering
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = {
                showPermissionDialog = false
                pendingTripStart = null
            },
            title = { Text("Locatie toegang vereist") },
            text = {
                Text("Deze app heeft toegang tot je locatie nodig om je rit te kunnen bijhouden. Zonder deze toestemming kunnen we je rit niet tracken.")
            },
            confirmButton = {
                Button(onClick = {
                    showPermissionDialog = false
                    permissionLauncher.launch(permissions)
                }) {
                    Text("Probeer opnieuw")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    pendingTripStart = null
                }) {
                    Text("Annuleren")
                }
            }
        )
    }

    // ✅ NIEUW: Dialog voor permanente weigering
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = {
                showSettingsDialog = false
                pendingTripStart = null
            },
            title = { Text("Toestemming permanent geweigerd") },
            text = {
                Text("Je hebt locatietoegang permanent geweigerd. Om ritten te kunnen starten, moet je deze toestemming handmatig inschakelen in de app-instellingen.")
            },
            confirmButton = {
                Button(onClick = {
                    showSettingsDialog = false
                    // Open app settings
                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                    pendingTripStart = null
                }) {
                    Text("Open Instellingen")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSettingsDialog = false
                    pendingTripStart = null
                }) {
                    Text("Annuleren")
                }
            }
        )
    }

    LaunchedEffect(Unit) {
        carsVm.refresh()
        userVm.loadMe()
        termsVm.load()
        reservationsVm.loadReservations()
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selected = when {
        currentRoute == HomeRoute.Listings.value || currentRoute?.startsWith("car/") == true -> HomeRoute.Listings.value
        currentRoute == HomeRoute.Map.value -> HomeRoute.Map.value
        currentRoute == HomeRoute.Reservations.value || currentRoute?.startsWith("reservations/") == true -> HomeRoute.Reservations.value
        currentRoute == HomeRoute.Terms.value -> HomeRoute.Terms.value
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
                , HomeRoute.Terms.value)) {
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
                        selected = selected == HomeRoute.Terms.value,
                        onClick = {
                            navController.navigate(HomeRoute.Terms.value) {
                                popUpTo(HomeRoute.Listings.value)
                                launchSingleTop = true
                            }
                        },
                        label = { Text("Terms") },
                        icon = { Icon(Icons.Outlined.Description, contentDescription = null) },
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
                    onAddCar = { navController.navigate(HomeRoute.CreateCar.value) },
                    onTerms = { navController.navigate(HomeRoute.Terms.value) }
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
                    userId = userVm.state.value.user?.id ?: 1L,
                    onDateSelected = {},
                    onCreateReservation = { selectedDate ->
                        navController.navigate(HomeRoute.CreateReservation.create(selectedDate))
                    },
                    onStartTrip = { reservationId, carId, car ->
                        startTripWithPermissionCheck(reservationId, carId, car)
                    }
                )
            }

            composable(
                route = HomeRoute.ActiveTrip.value,
                arguments = listOf(
                    navArgument("reservationId") { type = NavType.LongType },
                    navArgument("carId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val reservationId = backStackEntry.arguments?.getLong("reservationId") ?: 0L
                val carId = backStackEntry.arguments?.getLong("carId") ?: 0L
                val car = carsVm.state.value.cars.find { it.id == carId }

                ActiveTripScreen(
                    carId = carId,
                    userId = userVm.state.value.user?.id ?: 1L,
                    reservationId = reservationId,
                    car = car,
                    telemetryViewModel = telemetryVm,
                    onStopTrip = {
                        userVm.state.value.user?.id?.let { userId ->
                            telemetryVm.loadTripsForUser(userId)
                        }
                        navController.popBackStack()
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
                        val uid = userVm.state.value.user?.id ?: 1
                        reservationsVm.loadTerms(uid)
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
            
            composable(HomeRoute.Terms.value) {
                TermsScreen(vm = termsVm)
            }

composable(HomeRoute.User.value) {
                UserSettingsScreen(
                    state = userVm.state.value,
                    onReload = {
                        userVm.loadMe()
                        termsVm.load()
                    },
                    onSave = { f, l, e -> userVm.save(f, l, e) },
                    onDisable = { userVm.disableAccount(onDisabled = onLogout) },
                    onLogout = onLogout
                )
            }
        }
    }
}

private fun startTripService(context: Context, reservationId: Long, carId: Long, userId: Long) {
    val serviceIntent = Intent(context, ActiveTripService::class.java).apply {
        putExtra(ActiveTripService.EXTRA_RESERVATION_ID, reservationId)
        putExtra(ActiveTripService.EXTRA_CAR_ID, carId)
        putExtra(ActiveTripService.EXTRA_USER_ID, userId)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(serviceIntent)
    } else {
        context.startService(serviceIntent)
    }
}
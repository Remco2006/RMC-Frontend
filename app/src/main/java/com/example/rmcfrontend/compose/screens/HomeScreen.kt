package com.example.rmcfrontend.compose.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.rmcfrontend.auth.TokenManager
import com.example.rmcfrontend.compose.viewmodel.CarsViewModel
import com.example.rmcfrontend.compose.viewmodel.UserViewModel
import com.example.rmcfrontend.api.models.CreateCarRequest
import com.example.rmcfrontend.api.models.UpdateCarRequest
import com.example.rmcfrontend.ui.theme.screens.cars.EditCarScreen
import com.example.rmcfrontend.ui.theme.screens.cars.CreateCarScreen
import com.example.rmcfrontend.ui.theme.screens.cars.CarDetailsScreen



sealed class HomeRoute(val value: String) {
    data object Listings : HomeRoute("listings")
    data object Map : HomeRoute("map")
    data object User : HomeRoute("user")
    data object CarDetails : HomeRoute("car/{carId}") {
        fun create(carId: Long) = "car/$carId"
    }
    data object CreateCar : HomeRoute("car/create")
    data object EditCar : HomeRoute("car/{carId}/edit") {
        fun create(carId: Long) = "car/$carId/edit"
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

    // Load initial data
    LaunchedEffect(Unit) {
        carsVm.refresh()
        userVm.loadMe()
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Bepaal welke bottom nav item geselecteerd moet zijn
    val selected = when {
        currentRoute == HomeRoute.Listings.value ||
                currentRoute?.startsWith("car/") == true -> HomeRoute.Listings.value
        currentRoute == HomeRoute.Map.value -> HomeRoute.Map.value
        currentRoute == HomeRoute.User.value -> HomeRoute.User.value
        else -> HomeRoute.Listings.value
    }

    Scaffold(
        bottomBar = {
            // Verberg bottom bar op detail/edit/create screens
            if (currentRoute in listOf(
                    HomeRoute.Listings.value,
                    HomeRoute.Map.value,
                    HomeRoute.User.value
                )) {
                NavigationBar {
                    NavigationBarItem(
                        selected = selected == HomeRoute.Listings.value,
                        onClick = {
                            navController.navigate(HomeRoute.Listings.value) {
                                popUpTo(HomeRoute.Listings.value) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        label = { Text("Listings") },
                        icon = { Text("🚗") }
                    )
                    NavigationBarItem(
                        selected = selected == HomeRoute.Map.value,
                        onClick = {
                            navController.navigate(HomeRoute.Map.value) {
                                popUpTo(HomeRoute.Listings.value)
                                launchSingleTop = true
                            }
                        },
                        label = { Text("Map") },
                        icon = { Text("🗺") }
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
                        icon = { Text("👤") }
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
            // 🚗 Listings Screen
            composable(HomeRoute.Listings.value) {
                ListingsScreen(
                    state = carsVm.state.value,
                    onRefresh = { carsVm.refresh() },
                    onCarClick = { carId ->
                        navController.navigate(HomeRoute.CarDetails.create(carId))
                    },
                    onAddCar = {
                        navController.navigate(HomeRoute.CreateCar.value)
                    }
                )
            }

            // 📄 Car Details Screen
            composable(
                route = HomeRoute.CarDetails.value,
                arguments = listOf(navArgument("carId") { type = NavType.LongType })
            ) { backStackEntry ->
                val carId = backStackEntry.arguments?.getLong("carId") ?: 0L
                CarDetailsScreen(
                    carId = carId.toString(),
                    carsViewModel = carsVm,
                    onBack = { navController.popBackStack() },
                    onEdit = { id ->
                        navController.navigate(HomeRoute.EditCar.create(id.toLong()))
                    },
                    onDelete = { car ->
                        car.id?.let { carsVm.deleteCar(it) }
                        navController.popBackStack()
                    }
                )
            }

            // ➕ Create Car Screen
            composable(HomeRoute.CreateCar.value) {
                CreateCarScreen(
                    carsViewModel = carsVm,
                    onBack = { navController.popBackStack() },
                    onSave = { request: CreateCarRequest ->
                        carsVm.createCar(
                            request = request,
                            onSuccess = {
                                navController.popBackStack()
                                carsVm.refresh()
                            }
                        )
                    }
                )
            }

            // ✏️ Edit Car Screen
            composable(
                route = HomeRoute.EditCar.value,
                arguments = listOf(navArgument("carId") { type = NavType.LongType })
            ) { backStackEntry ->
                val carId = backStackEntry.arguments?.getLong("carId") ?: 0L
                EditCarScreen(
                    carId = carId.toString(),
                    carsViewModel = carsVm,
                    onBack = { navController.popBackStack() },
                    onSave = { request: UpdateCarRequest ->
                        carsVm.updateCar(
                            id = carId.toString(),
                            request = request,
                            onSuccess = {
                                navController.popBackStack()
                                carsVm.refresh()
                            }
                        )
                    }
                )
            }

            // 🗺 Map Screen
            composable(HomeRoute.Map.value) {
                MapScreen()
            }

            // 👤 User Settings Screen
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
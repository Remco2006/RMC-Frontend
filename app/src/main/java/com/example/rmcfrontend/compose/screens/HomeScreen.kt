package com.example.rmcfrontend.compose.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import com.example.rmcfrontend.compose.viewmodel.CarsViewModel
import com.example.rmcfrontend.compose.viewmodel.UserViewModel
import com.example.rmcfrontend.ui.theme.screens.cars.CarDetailsScreen
import com.example.rmcfrontend.ui.theme.screens.cars.CreateCarScreen
import com.example.rmcfrontend.ui.theme.screens.cars.EditCarScreen

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

    LaunchedEffect(Unit) {
        carsVm.refresh()
        userVm.loadMe()
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selected = when {
        currentRoute == HomeRoute.Listings.value || currentRoute?.startsWith("car/") == true -> HomeRoute.Listings.value
        currentRoute == HomeRoute.Map.value -> HomeRoute.Map.value
        currentRoute == HomeRoute.User.value -> HomeRoute.User.value
        else -> HomeRoute.Listings.value
    }

    Scaffold(
        bottomBar = {
            // Hide bottom bar on detail/edit/create
            if (currentRoute in listOf(HomeRoute.Listings.value, HomeRoute.Map.value, HomeRoute.User.value)) {
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
                        selected = selected == HomeRoute.Map.value,
                        onClick = {
                            navController.navigate(HomeRoute.Map.value) {
                                popUpTo(HomeRoute.Listings.value)
                                launchSingleTop = true
                            }
                        },
                        label = { Text("Map") },
                        icon = { Icon(Icons.Outlined.Map, contentDescription = null) },
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
            composable(HomeRoute.Listings.value) {
                ListingsScreen(
                    state = carsVm.state.value,
                    onRefresh = { carsVm.refresh() },
                    onCarClick = { carId -> navController.navigate(HomeRoute.CarDetails.create(carId)) },
                    onAddCar = { navController.navigate(HomeRoute.CreateCar.value) }
                )
            }

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

            composable(HomeRoute.Map.value) { MapScreen() }

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

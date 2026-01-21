package com.example.rmcfrontend.compose

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.rmcfrontend.api.ApiClient
import com.example.rmcfrontend.auth.TokenManager
import com.example.rmcfrontend.compose.screens.HomeScreen
import com.example.rmcfrontend.compose.screens.LoginScreen
import com.example.rmcfrontend.compose.screens.RegisterScreen
import com.example.rmcfrontend.compose.viewmodel.AuthViewModel
import com.example.rmcfrontend.compose.viewmodel.CarsViewModel
import com.example.rmcfrontend.ui.theme.screens.cars.CarDetailsScreen
import com.example.rmcfrontend.ui.theme.screens.cars.CarsScreen
import com.example.rmcfrontend.ui.theme.screens.cars.CreateCarScreen
import com.example.rmcfrontend.ui.theme.screens.cars.EditCarScreen
import org.koin.androidx.compose.koinViewModel

sealed class Route(val value: String) {
    data object Login : Route("login")
    data object Register : Route("register")
    data object Home : Route("home")

    data object Cars : Route("cars")
    data object CreateCar : Route("create_car")
    data object CarDetails : Route("car_details/{carId}") {
        fun create(carId: Long) = "car_details/$carId"
    }
    data object EditCar : Route("edit_car/{carId}") {
        fun create(carId: Long) = "edit_car/$carId"
    }
}

@Composable
fun AppRoot(
    tokenManager: TokenManager,
    authVm: AuthViewModel = koinViewModel(),
    carsVm: CarsViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val ctx = LocalContext.current

    // Decide initial route based on stored token.
    val startDestination = if (tokenManager.isLoggedIn()) Route.Home.value else Route.Login.value

    // When auth state changes, keep Retrofit token in sync.
    LaunchedEffect(authVm.authState.value.isLoggedIn) {
        val token = tokenManager.getToken()
        if (token != null) ApiClient.setAuthToken(token) else ApiClient.clearAuthToken()
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Route.Login.value) {
            LoginScreen(
                state = authVm.authState.value,
                onLogin = { email, password -> authVm.login(email, password) },
                onNavigateToRegister = { navController.navigate(Route.Register.value) },
                onLoginSuccess = {
                    Toast.makeText(ctx, "Logged in", Toast.LENGTH_SHORT).show()
                    navController.navigate(Route.Home.value) {
                        popUpTo(Route.Login.value) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Register.value) {
            RegisterScreen(
                state = authVm.authState.value,
                onRegister = { first, last, email, password ->
                    authVm.register(first, last, email, password)
                },
                onBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    Toast.makeText(ctx, "Account created. Please login.", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            )
        }

        composable(Route.Home.value) {
            HomeScreen(
                tokenManager = tokenManager,
                onLogout = {
                    authVm.logout()
                    Toast.makeText(ctx, "Logged out", Toast.LENGTH_SHORT).show()
                    navController.navigate(Route.Login.value) {
                        popUpTo(Route.Home.value) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Cars.value) {
            CarsScreen(
                carsViewModel = carsVm,
                onCarClick = { carId -> navController.navigate(Route.CarDetails.create(carId)) },
                onAddCar = { navController.navigate(Route.CreateCar.value) }
            )
        }

        composable(
            Route.CarDetails.value,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            CarDetailsScreen(
                carId = carId,
                carsViewModel = carsVm,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Route.EditCar.create(id)) },
                onDelete = { car ->
                    car.id?.let { carsVm.deleteCar(it) }
                    navController.popBackStack()
                }
            )
        }

        composable(Route.CreateCar.value) {
            CreateCarScreen(
                carsViewModel = carsVm,
                onBack = { navController.popBackStack() },
                onSave = { request, imageUris ->
                    carsVm.createCarWithImages(
                        context = ctx,
                        request = request,
                        imageUris = imageUris,
                        onSuccess = { navController.popBackStack() }
                    )
                }
            )
        }

        composable(
            Route.EditCar.value,
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            EditCarScreen(
                carId = carId,
                carsViewModel = carsVm,
                onBack = { navController.popBackStack() },
                onSave = { request, imageUris ->
                    carsVm.updateCarWithImages(
                        context = ctx,
                        id = carId,
                        request = request,
                        imageUris = imageUris,
                        onSuccess = { navController.popBackStack() }
                    )
                }
            )
        }
    }
}
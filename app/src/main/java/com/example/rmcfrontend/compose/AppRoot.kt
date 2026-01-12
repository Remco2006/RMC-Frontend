package com.example.rmcfrontend.compose

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rmcfrontend.api.ApiClient
import com.example.rmcfrontend.auth.TokenManager
import com.example.rmcfrontend.compose.screens.HomeScreen
import com.example.rmcfrontend.compose.screens.LoginScreen
import com.example.rmcfrontend.compose.screens.RegisterScreen
import com.example.rmcfrontend.compose.viewmodel.AuthViewModel

sealed class Route(val value: String) {
    data object Login : Route("login")
    data object Register : Route("register")
    data object Home : Route("home")
}

@Composable
fun AppRoot(tokenManager: TokenManager) {
    val navController = rememberNavController()
    val ctx = LocalContext.current
    val authVm = remember { AuthViewModel(tokenManager) }

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
    }
}

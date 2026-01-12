package com.example.rmcfrontend.compose.screens

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
import com.example.rmcfrontend.auth.TokenManager
import com.example.rmcfrontend.compose.viewmodel.CarsViewModel
import androidx.compose.foundation.layout.padding
import com.example.rmcfrontend.compose.viewmodel.UserViewModel

sealed class HomeRoute(val value: String) {
    data object Listings : HomeRoute("listings")
    data object Map : HomeRoute("map")
    data object User : HomeRoute("user")
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
    val selected = navBackStackEntry?.destination?.route ?: HomeRoute.Listings.value

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selected == HomeRoute.Listings.value,
                    onClick = {
                        navController.navigate(HomeRoute.Listings.value) {
                            launchSingleTop = true
                        }
                    },
                    label = { Text("Listings") },
                    icon = { Text("L") }
                )
                NavigationBarItem(
                    selected = selected == HomeRoute.Map.value,
                    onClick = {
                        navController.navigate(HomeRoute.Map.value) {
                            launchSingleTop = true
                        }
                    },
                    label = { Text("Map") },
                    icon = { Text("M") }
                )
                NavigationBarItem(
                    selected = selected == HomeRoute.User.value,
                    onClick = {
                        navController.navigate(HomeRoute.User.value) {
                            launchSingleTop = true
                        }
                    },
                    label = { Text("User") },
                    icon = { Text("U") }
                )
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
                    onRefresh = { carsVm.refresh() }
                )
            }
            composable(HomeRoute.Map.value) {
                MapScreen()
            }
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


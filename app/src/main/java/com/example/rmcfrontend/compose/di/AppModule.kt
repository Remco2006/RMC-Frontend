package com.example.rmcfrontend.di

import com.example.rmcfrontend.auth.TokenManager
import com.example.rmcfrontend.compose.viewmodel.AuthViewModel
import com.example.rmcfrontend.compose.viewmodel.CarSearchViewModel
import com.example.rmcfrontend.compose.viewmodel.CarsViewModel
import com.example.rmcfrontend.compose.viewmodel.ReservationsViewModel
import com.example.rmcfrontend.compose.viewmodel.TelemetryViewModel
import com.example.rmcfrontend.compose.viewmodel.TermsViewModel
import com.example.rmcfrontend.compose.viewmodel.UserViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    // Singletons
    singleOf(::TokenManager)

    // ViewModels
    factory { AuthViewModel(get()) }
    factory { CarsViewModel() }
    factory { UserViewModel(get()) }
    factory { TermsViewModel() }
    factory { TelemetryViewModel() }
    factory { ReservationsViewModel() }
    factory { CarSearchViewModel() }
}
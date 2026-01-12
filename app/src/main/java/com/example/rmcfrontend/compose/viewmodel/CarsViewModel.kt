package com.example.rmcfrontend.compose.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rmcfrontend.api.ApiClient
import com.example.rmcfrontend.api.models.Car
import kotlinx.coroutines.launch

data class CarsState(
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
    val cars: List<Car> = emptyList()
)

class CarsViewModel : ViewModel() {

    val state = mutableStateOf(CarsState())

    fun refresh() {
        state.value = state.value.copy(isBusy = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val res = ApiClient.carsApi.getAllCars()
                state.value = CarsState(isBusy = false, cars = res.GetCarResponseList)
            } catch (t: Throwable) {
                state.value = CarsState(isBusy = false, errorMessage = t.message ?: "Unknown error")
            }
        }
    }
}

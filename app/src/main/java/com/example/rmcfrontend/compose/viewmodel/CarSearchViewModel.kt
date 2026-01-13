package com.example.rmcfrontend.compose.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rmcfrontend.api.ApiClient
import com.example.rmcfrontend.api.CarsApi
import com.example.rmcfrontend.api.models.Car
import com.example.rmcfrontend.api.models.CarSearchFilterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CarSearchUiState(
    val cars: List<Car> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentFilter: CarSearchFilterRequest = CarSearchFilterRequest()
)

class CarSearchViewModel(
    private val carsApi: CarsApi = ApiClient.carsApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(CarSearchUiState())
    val uiState: StateFlow<CarSearchUiState> = _uiState.asStateFlow()

    init {
        searchCars(CarSearchFilterRequest())
    }

    fun searchCars(filter: CarSearchFilterRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                currentFilter = filter
            )

            try {
                val response = carsApi.searchCars(filter)
                _uiState.value = _uiState.value.copy(
                    cars = response.GetCarResponseList,
                    isLoading = false
                )
                Log.d("CarSearchVM", "Found ${response.GetCarResponseList.size} cars")
            } catch (e: Exception) {
                Log.e("CarSearchVM", "Error searching cars", e)
                _uiState.value = _uiState.value.copy(
                    error = "Kon auto's niet laden: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
package com.example.rmcfrontend.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rmcfrontend.api.ApiClient
import com.example.rmcfrontend.api.CarsApi
import com.example.rmcfrontend.api.models.Car
import com.example.rmcfrontend.api.models.CreateCarRequest
import com.example.rmcfrontend.api.models.UpdateCarRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CarsState(
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
    val cars: List<Car> = emptyList()
)

class CarsViewModel(private val api: CarsApi = ApiClient.carsApi) : ViewModel() {

    private val _state = MutableStateFlow(CarsState())
    val state: StateFlow<CarsState> = _state

    // Backward compatibility: expose individual flows
    val cars: StateFlow<List<Car>> get() = MutableStateFlow(_state.value.cars)
    val loading: StateFlow<Boolean> get() = MutableStateFlow(_state.value.isBusy)
    val error: StateFlow<String?> get() = MutableStateFlow(_state.value.errorMessage)

    fun fetchAllCars() {
        _state.value = _state.value.copy(isBusy = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val response = api.getAllCars()
                _state.value = CarsState(isBusy = false, cars = response.GetCarResponseList)
            } catch (e: Exception) {
                _state.value = CarsState(isBusy = false, errorMessage = e.message ?: "Unknown error")
            }
        }
    }

    // Alias voor refresh (voor backward compatibility)
    fun refresh() = fetchAllCars()

    fun getCar(id: String): StateFlow<Car?> {
        val flow = MutableStateFlow<Car?>(null)
        viewModelScope.launch {
            try {
                flow.value = api.getCar(id)
            } catch (e: Exception) {
                _state.value = _state.value.copy(errorMessage = e.message)
            }
        }
        return flow
    }

    fun createCar(request: CreateCarRequest, onSuccess: () -> Unit) {
        _state.value = _state.value.copy(isBusy = true, errorMessage = null)
        viewModelScope.launch {
            try {
                api.createCar(request)
                fetchAllCars()
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isBusy = false,
                    errorMessage = e.message ?: "Failed to create car"
                )
            }
        }
    }

    fun updateCar(id: String, request: UpdateCarRequest, onSuccess: () -> Unit) {
        _state.value = _state.value.copy(isBusy = true, errorMessage = null)
        viewModelScope.launch {
            try {
                api.updateCar(id.toLong(), request)
                fetchAllCars()
                onSuccess()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isBusy = false,
                    errorMessage = e.message ?: "Failed to update car"
                )
            }
        }
    }

    fun deleteCar(id: Long) {
        _state.value = _state.value.copy(isBusy = true, errorMessage = null)
        viewModelScope.launch {
            try {
                api.deleteCar(id.toString())
                _state.value = _state.value.copy(
                    isBusy = false,
                    cars = _state.value.cars.filter { it.id != id }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isBusy = false,
                    errorMessage = e.message ?: "Failed to delete car"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
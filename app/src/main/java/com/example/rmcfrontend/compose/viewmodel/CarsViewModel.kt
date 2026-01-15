package com.example.rmcfrontend.compose.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rmcfrontend.api.ApiClient
import com.example.rmcfrontend.api.CarsApi
import com.example.rmcfrontend.api.models.Car
import com.example.rmcfrontend.api.models.CreateCarRequest
import com.example.rmcfrontend.api.models.UpdateCarRequest
import com.example.rmcfrontend.util.ImageUploadUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CarsState(
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
    val cars: List<Car> = emptyList()
)

class CarsViewModel(private val api: CarsApi = ApiClient.carsApi) : ViewModel() {

    private val _state = MutableStateFlow(CarsState())
    val state: StateFlow<CarsState> = _state

    // Optional compatibility flows (FIXED so Compose can observe updates)
    val cars: StateFlow<List<Car>> =
        state.map { it.cars }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val loading: StateFlow<Boolean> =
        state.map { it.isBusy }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val error: StateFlow<String?> =
        state.map { it.errorMessage }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    fun fetchAllCars() {
        _state.value = _state.value.copy(isBusy = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val response = api.getMyCars()
                _state.value = CarsState(isBusy = false, cars = response.GetCarResponseList)
            } catch (e: Exception) {
                _state.value = CarsState(isBusy = false, errorMessage = e.message ?: "Unknown error")
            }
        }
    }

    // Alias for backward compatibility
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

    /**
     * Create a car and (optionally) upload one or more images.
     * Uses existing endpoint:
     *   POST /cars/car-image/{carId}
     */
    fun createCarWithImages(
        context: Context,
        request: CreateCarRequest,
        imageUris: List<Uri>,
        onSuccess: () -> Unit
    ) {
        _state.value = _state.value.copy(isBusy = true, errorMessage = null)
        viewModelScope.launch {
            try {
                val created = api.createCar(request)

                val carId = created.id
                if (carId != null && imageUris.isNotEmpty()) {
                    val parts = ImageUploadUtils.urisToMultipartParts(context, imageUris)
                    if (parts.isNotEmpty()) {
                        api.uploadCarImages(carId = carId, images = parts)
                    }
                }

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

    /**
     * Update car details and (optionally) upload additional images.
     * Backend currently supports adding images; it does not remove/replace.
     */
    fun updateCarWithImages(
        context: Context,
        id: String,
        request: UpdateCarRequest,
        imageUris: List<Uri>,
        onSuccess: () -> Unit
    ) {
        _state.value = _state.value.copy(isBusy = true, errorMessage = null)
        viewModelScope.launch {
            try {
                api.updateCar(id.toLong(), request)

                val carId = id.toLongOrNull()
                if (carId != null && imageUris.isNotEmpty()) {
                    val parts = ImageUploadUtils.urisToMultipartParts(context, imageUris)
                    if (parts.isNotEmpty()) {
                        api.uploadCarImages(carId = carId, images = parts)
                    }
                }

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

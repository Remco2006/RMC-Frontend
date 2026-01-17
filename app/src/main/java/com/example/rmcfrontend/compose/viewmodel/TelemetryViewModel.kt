package com.example.rmcfrontend.compose.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rmcfrontend.api.ApiClient
import com.example.rmcfrontend.api.models.CreateTelemetryRequest
import com.example.rmcfrontend.api.models.Telemetry
import com.example.rmcfrontend.api.models.TelemetryApiInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TelemetryUiState(
    val trips: List<Telemetry> = emptyList(),
    val currentTrip: Telemetry? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class TelemetryViewModel(
    private val telemetryApi: TelemetryApiInterface = ApiClient.telemetryAPi
) : ViewModel() {

    private val _uiState = MutableStateFlow(TelemetryUiState())
    val uiState: StateFlow<TelemetryUiState> = _uiState.asStateFlow()

    fun loadTripsForUser(userId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = telemetryApi.getTelemetryForUser(userId)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        trips = response.body() ?: emptyList(),
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Kon ritten niet laden: ${response.code()}",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("TelemetryVM", "Error loading trips", e)
                _uiState.value = _uiState.value.copy(
                    error = "Fout bij laden ritten: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun saveTelemetry(
        request: CreateTelemetryRequest,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val response = telemetryApi.createTelemetry(request)
                if (response.isSuccessful) {
                    Log.d("TelemetryVM", "Telemetry saved successfully")
                    onSuccess()
                } else {
                    val errorMsg = "Fout bij opslaan: ${response.code()}"
                    Log.e("TelemetryVM", errorMsg)
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.e("TelemetryVM", "Error saving telemetry", e)
                onError(e.message ?: "Onbekende fout")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
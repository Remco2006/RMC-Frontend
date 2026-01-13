package com.example.rmcfrontend.compose.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rmcfrontend.api.ApiClient
import com.example.rmcfrontend.api.CarsApi
import com.example.rmcfrontend.api.ReservationsApi
import com.example.rmcfrontend.api.TermsApi
import com.example.rmcfrontend.api.models.Car
import com.example.rmcfrontend.api.models.CreateReservationRequest
import com.example.rmcfrontend.api.models.Reservation
import com.example.rmcfrontend.api.models.response.GetTermResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class ReservationsUiState(
    val reservations: List<Reservation> = emptyList(),
    val availableCars: List<Car> = emptyList(),
    val selectedCarReservations: List<Reservation> = emptyList(),
    val terms: GetTermResponse? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ReservationsViewModel(
    private val reservationsApi: ReservationsApi = ApiClient.reservationsApi,
    private val carsApi: CarsApi = ApiClient.carsApi,
    private val termsApi: TermsApi = ApiClient.termsApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationsUiState())
    val uiState: StateFlow<ReservationsUiState> = _uiState.asStateFlow()

    init {
        loadReservations()
        loadAvailableCars()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun loadReservations() {
        viewModelScope.launch {
            try {
                val reservations = reservationsApi.getAllReservations()
                _uiState.value = _uiState.value.copy(
                    reservations = reservations,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("ReservationsVM", "Error loading reservations", e)
                _uiState.value = _uiState.value.copy(
                    error = "Kon reserveringen niet laden: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun loadAvailableCars() {
        viewModelScope.launch {
            try {
                val response = carsApi.getAllCars()
                _uiState.value = _uiState.value.copy(
                    availableCars = response.GetCarResponseList
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Kon auto's niet laden: ${e.message}"
                )
            }
        }
    }

    fun loadCarReservations(carId: Long) {
        viewModelScope.launch {
            try {
                val response = reservationsApi.getCarReservations(carId)
                _uiState.value = _uiState.value.copy(
                    selectedCarReservations = response.reservations
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Kon reserveringen niet laden: ${e.message}"
                )
            }
        }
    }

    fun loadTerms(userId: Long) {
        viewModelScope.launch {
            try {
                val terms = termsApi.getActiveTermForUser(userId)
                _uiState.value = _uiState.value.copy(
                    terms = terms
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Kon voorwaarden niet laden: ${e.message}"
                )
            }
        }
    }

    fun createReservation(request: CreateReservationRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                reservationsApi.createReservation(request)
                loadReservations()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Reservering succesvol aangemaakt!"
                )
                onSuccess()
            } catch (e: HttpException) {
                // Parse HTTP error
                val errorMessage = try {
                    e.response()?.errorBody()?.string() ?: "Onbekende fout"
                } catch (ex: Exception) {
                    "Reservering mislukt"
                }

                Log.e("ReservationsVM", "HTTP Error: ${e.code()} - $errorMessage")

                val userMessage = when {
                    errorMessage.contains("already been booked", ignoreCase = true) ->
                        "Deze auto is al geboekt voor de geselecteerde tijd"
                    e.code() == 400 -> "Ongeldige reserveringsgegevens: $errorMessage"
                    e.code() == 401 -> "Niet geautoriseerd"
                    e.code() == 404 -> "Auto of gebruiker niet gevonden"
                    else -> "Reservering mislukt: $errorMessage"
                }

                _uiState.value = _uiState.value.copy(
                    error = userMessage,
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("ReservationsVM", "Error creating reservation", e)
                _uiState.value = _uiState.value.copy(
                    error = "Reservering mislukt: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun getAvailableTimeSlots(
        selectedDate: LocalDate,
        carId: Long
    ): List<Pair<LocalDateTime, LocalDateTime>> {
        val reservations = _uiState.value.selectedCarReservations
            .filter { it.startTime == selectedDate }

        return reservations.map {
            it.startTime to it.endTime
        }
    }

    fun isTimeSlotAvailable(
        selectedDate: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        carId: Long
    ): Boolean {
        val unavailableSlots = getAvailableTimeSlots(selectedDate, carId)
        val requestedStart = LocalDateTime.of(selectedDate, startTime)
        val requestedEnd = LocalDateTime.of(selectedDate, endTime)

        return unavailableSlots.none { (bookedStart, bookedEnd) ->
            requestedStart < bookedEnd && requestedEnd > bookedStart
        }
    }
}
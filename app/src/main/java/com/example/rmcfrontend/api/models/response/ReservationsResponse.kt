package com.example.rmcfrontend.api.models.response

import com.example.rmcfrontend.api.models.Reservation

data class ReservationsResponse (
    val reservations: List<Reservation>
)
package com.example.rmcfrontend.api.models

import java.time.LocalDateTime

data class CreateReservationRequest(
    val startTime: String,
    val endTime: String,
    val userId: Long,
    val carId: Long,
    val termId: Long,
    val status: String = "CONFIRMED",
    val startMileage: Int,
    val endMileage: Int,
    val costPerKm: String
)
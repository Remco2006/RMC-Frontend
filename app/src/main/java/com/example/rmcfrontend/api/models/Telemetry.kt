package com.example.rmcfrontend.api.models

import java.time.LocalDateTime

data class Telemetry(
    val tripId: Long?,
    val userId: Long?,
    val carId: Long?,
    val carName: String?,
    val timestamp: LocalDateTime?,
    val avgSpeedKmh: Double?,
    val maxSpeedKmh: Double?,
    val tripDistanceKm: Double?,
    val tripDurationMin: Int?,
    val harshBrakes: Int?,
    val harshAccelerations: Int?,
    val corneringScore: Int?,
    val ecoScore: Int?
)

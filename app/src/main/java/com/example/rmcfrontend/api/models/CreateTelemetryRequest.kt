package com.example.rmcfrontend.api.models

data class CreateTelemetryRequest(
    val userId: Long,
    val carId: Long,
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val tripDistanceKm: Double,
    val tripDurationMin: Int,
    val harshBrakes: Int,
    val harshAccelerations: Int,
    val corneringScore: Int,
    val ecoScore: Int
)
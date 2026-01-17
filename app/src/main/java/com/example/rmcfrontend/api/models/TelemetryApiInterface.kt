package com.example.rmcfrontend.api.models

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TelemetryApiInterface {
    @POST("/telemetry")
    suspend fun createTelemetry(@Body request: CreateTelemetryRequest): Response<Telemetry>

    @GET("/telemetry")
    suspend fun getAllTelemetry(): Response<List<Telemetry>>

    @GET("/telemetry/user/{userId}")
    suspend fun getTelemetryForUser(@Path("userId") userId: Long): Response<List<Telemetry>>

    @GET("/telemetry/car/{carId}")
    suspend fun getTelemetryForCar(@Path("carId") carId: Long): Response<List<Telemetry>>

    @GET("/telemetry/{tripId}")
    suspend fun getTelemetryById(@Path("tripId") tripId: Long): Response<Telemetry>
}

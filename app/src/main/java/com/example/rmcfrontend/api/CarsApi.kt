package com.example.rmcfrontend.api

import com.example.rmcfrontend.api.models.CarsResponse
import retrofit2.http.GET

interface CarsApi {
    @GET("cars")
    suspend fun getAllCars(): CarsResponse
}

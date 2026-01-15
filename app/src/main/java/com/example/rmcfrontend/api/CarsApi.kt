package com.example.rmcfrontend.api

import com.example.rmcfrontend.api.models.Car
import com.example.rmcfrontend.api.models.CarSearchFilterRequest
import com.example.rmcfrontend.api.models.CreateCarRequest
import com.example.rmcfrontend.api.models.UpdateCarRequest
import com.example.rmcfrontend.api.models.response.CarsResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface CarsApi {
    @GET("cars")
    suspend fun getAllCars(): CarsResponse

    @GET("cars/my-cars")
    suspend fun getMyCars(): CarsResponse

    @GET("cars/{id}")
    suspend fun getCar(
        @Path("id") carId: String
    ): Car
    @PUT("/cars/{id}")
    suspend fun updateCar(
        @Path("id") id: Long,
        @Body car: UpdateCarRequest
    )
    @POST("/cars/create")
    suspend fun createCar(
        @Body request: CreateCarRequest
    ): Car

    /**
     * Upload one or more images for a car.
     * Kotlin API expects multipart file parts (part name is not important).
     */
    @Multipart
    @POST("/cars/car-image/{carId}")
    suspend fun uploadCarImages(
        @Path("carId") carId: Long,
        @Part images: List<MultipartBody.Part>
    ): Car
    @DELETE("cars/{id}")
    suspend fun deleteCar(@Path("id") id: String)

    @POST("cars/search")
    suspend fun searchCars(@Body filter: CarSearchFilterRequest): CarsResponse
}

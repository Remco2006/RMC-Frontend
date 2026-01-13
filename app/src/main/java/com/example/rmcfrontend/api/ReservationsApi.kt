package com.example.rmcfrontend.api

import com.example.rmcfrontend.api.models.CreateReservationRequest
import com.example.rmcfrontend.api.models.Reservation
import com.example.rmcfrontend.api.models.response.ReservationsResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReservationsApi {
    @GET("reservations")
    suspend fun getAllReservations(): List<Reservation>

    @GET("reservations/{id}")
    suspend fun getReservation(
        @Path("id") id: Long
    ): Reservation

    @GET("reservations/available-car/{carId}")
    suspend fun getCarReservations(
        @Path("carId") carId: Long
    ): ReservationsResponse

    @POST("reservations")
    suspend fun createReservation(
        @Body request: CreateReservationRequest
    ): Reservation

    @DELETE("reservations/{id}")
    suspend fun deleteReservation(
        @Path("id") id: Long
    )
}
package com.example.rmcfrontend.api

import com.example.rmcfrontend.api.models.LoginRequest
import com.example.rmcfrontend.api.models.response.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
package com.example.rmcfrontend.api.models.response

data class LoginResponse(
    val token: String,
    val email: String,
    val id: Int
)
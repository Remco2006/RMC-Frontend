package com.example.rmcfrontend.api.models

data class CreateUserRequest(
    val firstName: String,
    val lastName: String,
    val password: String,
    val email: String
)

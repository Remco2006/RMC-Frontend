package com.example.rmcfrontend.api.models

data class UpdateUserRequest(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String
)

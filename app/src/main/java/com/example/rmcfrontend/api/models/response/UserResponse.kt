package com.example.rmcfrontend.api.models.response

data class UserResponse(
    val id: Long?,
    val firstName: String,
    val lastName: String,
    val email: String,
    val createdAt: String? = null,
    val modifiedAt: String? = null
)

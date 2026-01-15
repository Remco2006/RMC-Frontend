package com.example.rmcfrontend.api.models

/**
 * Mirrors AvansAPI prof.Requests.CreateTermRequest
 */
data class CreateTermRequest(
    val title: String,
    val content: String,
    val active: Boolean
)

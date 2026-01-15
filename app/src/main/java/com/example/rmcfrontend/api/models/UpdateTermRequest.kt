package com.example.rmcfrontend.api.models

/**
 * Mirrors AvansAPI prof.Requests.UpdateTermRequest
 */
data class UpdateTermRequest(
    val id: Long,
    val title: String,
    val content: String,
    val active: Boolean
)

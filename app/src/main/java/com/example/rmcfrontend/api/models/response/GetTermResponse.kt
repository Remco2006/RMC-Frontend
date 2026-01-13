package com.example.rmcfrontend.api.models.response

data class GetTermResponse (
    val id: Long? = null,
    val title: String? = null,
    val content: String? = null,
    val version: Int? = null,
    val active: Boolean? = null,
    val createdAt: String? = null,
    val modifiedAt: String? = null
)
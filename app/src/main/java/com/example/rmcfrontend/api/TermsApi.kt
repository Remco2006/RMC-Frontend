package com.example.rmcfrontend.api

import com.example.rmcfrontend.api.models.response.GetTermResponse
import retrofit2.http.*

interface TermsApi {
    @GET("terms/user/{userId}")
    suspend fun getActiveTermForUser(
        @Path("userId") userId: Long
    ): GetTermResponse
}
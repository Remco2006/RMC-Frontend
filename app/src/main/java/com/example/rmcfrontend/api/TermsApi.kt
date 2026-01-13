package com.example.rmcfrontend.api

import com.example.rmcfrontend.api.models.CreateTermRequest
import com.example.rmcfrontend.api.models.UpdateTermRequest
import com.example.rmcfrontend.api.models.response.GetTermResponse
import retrofit2.http.*

interface TermsApi {
    @GET("terms/user/{userId}")
    suspend fun getActiveTermForUser(
        @Path("userId") userId: Long
    ): GetTermResponse

    // Authenticated: list all terms for the current user (principal)
    @GET("terms")
    suspend fun getMyTerms(): List<GetTermResponse>

    @GET("terms/{id}")
    suspend fun getTermById(@Path("id") id: Long): GetTermResponse

    @POST("terms")
    suspend fun createTerm(@Body body: CreateTermRequest): GetTermResponse

    // Backend expects PUT /terms with UpdateTermRequest in body
    @PUT("terms")
    suspend fun updateTerm(@Body body: UpdateTermRequest)

    @DELETE("terms/{id}")
    suspend fun deleteTerm(@Path("id") id: Long)
}
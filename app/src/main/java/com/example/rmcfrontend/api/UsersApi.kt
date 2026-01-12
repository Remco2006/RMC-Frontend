package com.example.rmcfrontend.api

import com.example.rmcfrontend.api.models.CreateUserRequest
import com.example.rmcfrontend.api.models.UpdateUserRequest
import com.example.rmcfrontend.api.models.response.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UsersApi {

    @GET("/users/{id}")
    suspend fun getUser(@Path("id") id: Int): Response<UserResponse>

    @PUT("/users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body request: UpdateUserRequest
    ): Response<Void>

    /**
     * Registration endpoint.
     * Note: Depending on backend security configuration, this may require an auth token.
     */
    @POST("/users")
    suspend fun register(@Body request: CreateUserRequest): Response<UserResponse>

    @PUT("/users/{id}/disable")
    suspend fun disableUser(@Path("id") id: Int): Response<Void>
}

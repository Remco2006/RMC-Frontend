package com.example.rmcfrontend.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val BEARER_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJqd3QtYXVkaWVuY2UiLCJpc3MiOiJodHRwczovL2xvY2FsaG9zdDo4MDgwLyIsImVtYWlsIjoidGVzdEBleGFtcGxlLmNvbSIsImlkIjoyLCJleHAiOjE3NjM3NTg4OTR9.UniZdt9I4pTCJRK7_Y2mEo4VTlBnhFXHhBIAjwmJF-k"

    private val authInterceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $BEARER_TOKEN")
            .build()

        chain.proceed(req)
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val carsApi: CarsApi = retrofit.create(CarsApi::class.java)
}

package com.example.rmcfrontend.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    /**
     * API base URL.
     * - Android Emulator -> use http://10.0.2.2:8080/ to reach your computer's localhost.
     * - Physical phone   -> replace with your PC's LAN IP, e.g. http://192.168.1.50:8080/
     */
    const val BASE_URL = "http://10.0.2.2:8080/"
    private var authToken: String? = null

    fun setAuthToken(token: String) {
        authToken = token
    }

    fun clearAuthToken() {
        authToken = null
    }

    fun hasAuthToken(): Boolean = authToken != null

    // Interceptor voor Authorization header
    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        authToken?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        chain.proceed(requestBuilder.build())
    }

    // Logging interceptor voor debugging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val carsApi: CarsApi = retrofit.create(CarsApi::class.java)
    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val usersApi: UsersApi = retrofit.create(UsersApi::class.java)
    val termsApi: TermsApi = retrofit.create(TermsApi::class.java)
    val reservationsApi: ReservationsApi = retrofit.create(ReservationsApi::class.java)
}

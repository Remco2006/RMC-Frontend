package com.example.rmcfrontend.api

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    private val devToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJqd3QtYXVkaWVuY2UiLCJpc3MiOiJodHRwczovL2xvY2FsaG9zdDo4MDgwLyIsImVtYWlsIjoidGVzdEBleGFtcGxlLmNvbSIsImlkIjoyLCJleHAiOjE3NjM3NTc3Mjl9.ODfrGs1Nyc3SCDIaUm04fNPnqBKSbLdWtxS47ZA8O4c"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $devToken")
            .build()

        return chain.proceed(request)
    }
}

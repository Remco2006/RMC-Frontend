package com.example.rmcfrontend.api

/**
 * Small abstraction around [ApiClient] auth token handling.
 *
 * This makes ViewModels unit-testable without relying on the global singleton.
 */
interface ApiSession {
    fun setAuthToken(token: String)
    fun clearAuthToken()
}

object DefaultApiSession : ApiSession {
    override fun setAuthToken(token: String) = ApiClient.setAuthToken(token)
    override fun clearAuthToken() = ApiClient.clearAuthToken()
}

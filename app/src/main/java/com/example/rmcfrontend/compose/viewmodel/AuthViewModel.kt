package com.example.rmcfrontend.compose.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rmcfrontend.api.ApiClient
import com.example.rmcfrontend.api.models.CreateUserRequest
import com.example.rmcfrontend.api.models.LoginRequest
import com.example.rmcfrontend.auth.TokenManager
import kotlinx.coroutines.launch

data class AuthState(
    val isLoggedIn: Boolean = false,
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
    val lastAction: LastAction? = null
)

enum class LastAction { LOGIN_SUCCESS, REGISTER_SUCCESS }

class AuthViewModel(private val tokenManager: TokenManager) : ViewModel() {

    val authState = mutableStateOf(
        AuthState(isLoggedIn = tokenManager.isLoggedIn())
    )

    fun login(email: String, password: String) {
        authState.value = authState.value.copy(isBusy = true, errorMessage = null, lastAction = null)
        viewModelScope.launch {
            try {
                val res = ApiClient.authApi.login(LoginRequest(email = email, password = password))
                if (res.isSuccessful && res.body() != null) {
                    val body = res.body()!!
                    tokenManager.saveToken(body.token, body.id, body.email)
                    ApiClient.setAuthToken(body.token)
                    authState.value = AuthState(
                        isLoggedIn = true,
                        isBusy = false,
                        errorMessage = null,
                        lastAction = LastAction.LOGIN_SUCCESS
                    )
                } else {
                    val msg = "Login failed (${res.code()})."
                    authState.value = authState.value.copy(isBusy = false, errorMessage = msg)
                }
            } catch (t: Throwable) {
                authState.value = authState.value.copy(isBusy = false, errorMessage = t.message ?: "Unknown error")
            }
        }
    }

    fun register(firstName: String, lastName: String, email: String, password: String) {
        authState.value = authState.value.copy(isBusy = true, errorMessage = null, lastAction = null)
        viewModelScope.launch {
            try {
                val res = ApiClient.usersApi.register(
                    CreateUserRequest(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        password = password
                    )
                )
                if (res.isSuccessful) {
                    authState.value = authState.value.copy(
                        isBusy = false,
                        errorMessage = null,
                        lastAction = LastAction.REGISTER_SUCCESS
                    )
                } else {
                    val msg = when (res.code()) {
                        409 -> "Email already exists."
                        400 -> "Invalid registration data."
                        else -> "Registration failed (${res.code()})."
                    }
                    authState.value = authState.value.copy(isBusy = false, errorMessage = msg)
                }
            } catch (t: Throwable) {
                authState.value = authState.value.copy(isBusy = false, errorMessage = t.message ?: "Unknown error")
            }
        }
    }

    fun logout() {
        tokenManager.clearToken()
        ApiClient.clearAuthToken()
        authState.value = AuthState(isLoggedIn = false)
    }
}

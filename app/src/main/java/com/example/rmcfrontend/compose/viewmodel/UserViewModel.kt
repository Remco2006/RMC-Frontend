package com.example.rmcfrontend.compose.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rmcfrontend.api.ApiClient
import com.example.rmcfrontend.api.models.UpdateUserRequest
import com.example.rmcfrontend.api.models.response.UserResponse
import com.example.rmcfrontend.auth.TokenManager
import kotlinx.coroutines.launch

data class UserState(
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
    val user: UserResponse? = null,
    val lastSavedOk: Boolean = false,
    val lastDisabledOk: Boolean = false
)

class UserViewModel(private val tokenManager: TokenManager) : ViewModel() {

    val state = mutableStateOf(UserState())

    fun loadMe() {
        val id = tokenManager.getUserId()
        if (id <= 0) {
            state.value = state.value.copy(errorMessage = "Missing user id")
            return
        }
        state.value = state.value.copy(isBusy = true, errorMessage = null, lastSavedOk = false, lastDisabledOk = false)
        viewModelScope.launch {
            try {
                val res = ApiClient.usersApi.getUser(id)
                if (res.isSuccessful && res.body() != null) {
                    state.value = state.value.copy(isBusy = false, user = res.body())
                } else {
                    state.value = state.value.copy(isBusy = false, errorMessage = "Failed to load profile (${res.code()}).")
                }
            } catch (t: Throwable) {
                state.value = state.value.copy(isBusy = false, errorMessage = t.message ?: "Unknown error")
            }
        }
    }

    fun save(firstName: String, lastName: String, email: String) {
        val id = tokenManager.getUserId()
        if (id <= 0) {
            state.value = state.value.copy(errorMessage = "Missing user id")
            return
        }
        state.value = state.value.copy(isBusy = true, errorMessage = null, lastSavedOk = false)
        viewModelScope.launch {
            try {
                val res = ApiClient.usersApi.updateUser(
                    id,
                    UpdateUserRequest(
                        id = id.toLong(),
                        firstName = firstName,
                        lastName = lastName,
                        email = email
                    )
                )
                if (res.isSuccessful) {
                    tokenManager.updateEmail(email)
                    loadMe()
                    state.value = state.value.copy(lastSavedOk = true)
                } else {
                    state.value = state.value.copy(isBusy = false, errorMessage = "Save failed (${res.code()}).")
                }
            } catch (t: Throwable) {
                state.value = state.value.copy(isBusy = false, errorMessage = t.message ?: "Unknown error")
            }
        }
    }

    fun disableAccount(onDisabled: () -> Unit) {
        val id = tokenManager.getUserId()
        if (id <= 0) {
            state.value = state.value.copy(errorMessage = "Missing user id")
            return
        }
        state.value = state.value.copy(isBusy = true, errorMessage = null, lastDisabledOk = false)
        viewModelScope.launch {
            try {
                val res = ApiClient.usersApi.disableUser(id)
                if (res.isSuccessful) {
                    state.value = state.value.copy(isBusy = false, lastDisabledOk = true)
                    // Clear local auth immediately.
                    tokenManager.clearToken()
                    ApiClient.clearAuthToken()
                    onDisabled()
                } else {
                    state.value = state.value.copy(isBusy = false, errorMessage = "Disable failed (${res.code()}).")
                }
            } catch (t: Throwable) {
                state.value = state.value.copy(isBusy = false, errorMessage = t.message ?: "Unknown error")
            }
        }
    }
}

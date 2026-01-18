package com.example.rmcfrontend.compose.viewmodel

import com.example.rmcfrontend.api.ApiSession
import com.example.rmcfrontend.api.AuthApi
import com.example.rmcfrontend.api.UsersApi
import com.example.rmcfrontend.api.models.CreateUserRequest
import com.example.rmcfrontend.api.models.LoginRequest
import com.example.rmcfrontend.api.models.response.LoginResponse
import com.example.rmcfrontend.api.models.response.UserResponse
import com.example.rmcfrontend.auth.TokenManager
import com.example.rmcfrontend.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun login_success_savesToken_setsSession_andUpdatesState() = runTest(mainDispatcherRule.dispatcher) {
        val tokenManager = mockk<TokenManager>(relaxed = true)
        every { tokenManager.isLoggedIn() } returns false

        val authApi = mockk<AuthApi>()
        val usersApi = mockk<UsersApi>()
        val apiSession = mockk<ApiSession>(relaxed = true)

        coEvery { authApi.login(LoginRequest(email = "a@b.com", password = "pw")) } returns
            Response.success(LoginResponse(token = "tkn", email = "a@b.com", id = 7))

        val vm = AuthViewModel(tokenManager, authApi, usersApi, apiSession)

        vm.login("a@b.com", "pw")
        advanceUntilIdle()

        verify { tokenManager.saveToken("tkn", 7, "a@b.com") }
        verify { apiSession.setAuthToken("tkn") }

        assertTrue(vm.authState.value.isLoggedIn)
        assertFalse(vm.authState.value.isBusy)
        assertNull(vm.authState.value.errorMessage)
        assertEquals(LastAction.LOGIN_SUCCESS, vm.authState.value.lastAction)
    }

    @Test
    fun login_httpError_setsErrorMessage() = runTest(mainDispatcherRule.dispatcher) {
        val tokenManager = mockk<TokenManager>(relaxed = true)
        every { tokenManager.isLoggedIn() } returns false

        val authApi = mockk<AuthApi>()
        val usersApi = mockk<UsersApi>()
        val apiSession = mockk<ApiSession>(relaxed = true)

        val body = "Unauthorized".toResponseBody("text/plain".toMediaType())
        coEvery { authApi.login(any()) } returns Response.error(401, body)

        val vm = AuthViewModel(tokenManager, authApi, usersApi, apiSession)

        vm.login("a@b.com", "pw")
        advanceUntilIdle()

        assertFalse(vm.authState.value.isBusy)
        assertNotNull(vm.authState.value.errorMessage)
        assertTrue(vm.authState.value.errorMessage!!.contains("401"))
        verify(exactly = 0) { tokenManager.saveToken(any(), any(), any()) }
        verify(exactly = 0) { apiSession.setAuthToken(any()) }
    }

    @Test
    fun register_success_setsLastActionRegisterSuccess() = runTest(mainDispatcherRule.dispatcher) {
        val tokenManager = mockk<TokenManager>(relaxed = true)
        every { tokenManager.isLoggedIn() } returns false

        val authApi = mockk<AuthApi>()
        val usersApi = mockk<UsersApi>()
        val apiSession = mockk<ApiSession>(relaxed = true)

        val userResponse = UserResponse(id = 1L, firstName = "A", lastName = "B", email = "a@b.com")
        coEvery { usersApi.register(any()) } returns Response.success(userResponse)

        val vm = AuthViewModel(tokenManager, authApi, usersApi, apiSession)

        vm.register("A", "B", "a@b.com", "pw")
        advanceUntilIdle()

        coVerify {
            usersApi.register(
                CreateUserRequest(firstName = "A", lastName = "B", email = "a@b.com", password = "pw")
            )
        }
        assertFalse(vm.authState.value.isBusy)
        assertNull(vm.authState.value.errorMessage)
        assertEquals(LastAction.REGISTER_SUCCESS, vm.authState.value.lastAction)
    }

    @Test
    fun register_conflict409_setsFriendlyMessage() = runTest(mainDispatcherRule.dispatcher) {
        val tokenManager = mockk<TokenManager>(relaxed = true)
        every { tokenManager.isLoggedIn() } returns false

        val authApi = mockk<AuthApi>()
        val usersApi = mockk<UsersApi>()
        val apiSession = mockk<ApiSession>(relaxed = true)

        val body = "Conflict".toResponseBody("text/plain".toMediaType())
        coEvery { usersApi.register(any()) } returns Response.error(409, body)

        val vm = AuthViewModel(tokenManager, authApi, usersApi, apiSession)

        vm.register("A", "B", "a@b.com", "pw")
        advanceUntilIdle()

        assertEquals("Email already exists.", vm.authState.value.errorMessage)
        assertFalse(vm.authState.value.isBusy)
        assertNull(vm.authState.value.lastAction)
    }
}

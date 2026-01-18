package com.example.rmcfrontend.compose.viewmodel

import com.example.rmcfrontend.api.ApiSession
import com.example.rmcfrontend.api.UsersApi
import com.example.rmcfrontend.api.models.response.UserResponse
import com.example.rmcfrontend.auth.TokenManager
import com.example.rmcfrontend.testing.MainDispatcherRule
import io.mockk.coEvery
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
class UserViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadMe_missingUserId_setsErrorImmediately() = runTest(mainDispatcherRule.dispatcher) {
        val tokenManager = mockk<TokenManager>(relaxed = true)
        every { tokenManager.getUserId() } returns -1

        val usersApi = mockk<UsersApi>(relaxed = true)
        val apiSession = mockk<ApiSession>(relaxed = true)

        val vm = UserViewModel(tokenManager, usersApi, apiSession)

        vm.loadMe()

        assertEquals("Missing user id", vm.state.value.errorMessage)
    }

    @Test
    fun loadMe_success_setsUserInState() = runTest(mainDispatcherRule.dispatcher) {
        val tokenManager = mockk<TokenManager>(relaxed = true)
        every { tokenManager.getUserId() } returns 3

        val usersApi = mockk<UsersApi>()
        val apiSession = mockk<ApiSession>(relaxed = true)

        val user = UserResponse(id = 3L, firstName = "Jane", lastName = "Doe", email = "j@d.com")
        coEvery { usersApi.getUser(3) } returns Response.success(user)

        val vm = UserViewModel(tokenManager, usersApi, apiSession)

        vm.loadMe()
        advanceUntilIdle()

        assertFalse(vm.state.value.isBusy)
        assertNull(vm.state.value.errorMessage)
        assertNotNull(vm.state.value.user)
        assertEquals("Jane", vm.state.value.user!!.firstName)
    }

    @Test
    fun disableAccount_success_clearsToken_andCallsCallback() = runTest(mainDispatcherRule.dispatcher) {
        val tokenManager = mockk<TokenManager>(relaxed = true)
        every { tokenManager.getUserId() } returns 9

        val usersApi = mockk<UsersApi>()
        val apiSession = mockk<ApiSession>(relaxed = true)

        coEvery { usersApi.disableUser(9) } returns Response.success<Void>(null)

        val vm = UserViewModel(tokenManager, usersApi, apiSession)

        var disabledCalled = false
        vm.disableAccount { disabledCalled = true }
        advanceUntilIdle()

        assertTrue(disabledCalled)
        assertTrue(vm.state.value.lastDisabledOk)
        verify { tokenManager.clearToken() }
        verify { apiSession.clearAuthToken() }
    }

    @Test
    fun disableAccount_httpError_setsErrorMessage() = runTest(mainDispatcherRule.dispatcher) {
        val tokenManager = mockk<TokenManager>(relaxed = true)
        every { tokenManager.getUserId() } returns 9

        val usersApi = mockk<UsersApi>()
        val apiSession = mockk<ApiSession>(relaxed = true)

        val body = "Forbidden".toResponseBody("text/plain".toMediaType())
        coEvery { usersApi.disableUser(9) } returns Response.error(403, body)

        val vm = UserViewModel(tokenManager, usersApi, apiSession)

        vm.disableAccount { }
        advanceUntilIdle()

        assertFalse(vm.state.value.lastDisabledOk)
        assertNotNull(vm.state.value.errorMessage)
        assertTrue(vm.state.value.errorMessage!!.contains("403"))
    }
}

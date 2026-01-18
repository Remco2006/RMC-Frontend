package com.example.rmcfrontend.api

import com.example.rmcfrontend.api.models.CreateTermRequest
import com.example.rmcfrontend.api.models.CreateUserRequest
import com.example.rmcfrontend.api.models.LoginRequest
import com.example.rmcfrontend.api.models.response.GetTermResponse
import com.example.rmcfrontend.api.models.response.LoginResponse
import com.example.rmcfrontend.api.models.response.UserResponse
import com.google.gson.Gson
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiEndpointsTest {

    private lateinit var server: MockWebServer
    private lateinit var retrofit: Retrofit

    private val gson = Gson()

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun authApi_login_postsToLogin() {
        val api = retrofit.create(AuthApi::class.java)
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(gson.toJson(LoginResponse(token = "tkn", email = "a@b.com", id = 7)))
        )

        val response = kotlinx.coroutines.runBlocking {
            api.login(LoginRequest(email = "a@b.com", password = "pw"))
        }

        assertTrue(response.isSuccessful)
        val req = server.takeRequest()
        assertEquals("/login", req.path)
        assertEquals("POST", req.method)
        assertTrue(req.body.readUtf8().contains("\"email\":\"a@b.com\""))
    }

    @Test
    fun usersApi_register_postsToUsers() {
        val api = retrofit.create(UsersApi::class.java)
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(gson.toJson(UserResponse(id = 1L, firstName = "A", lastName = "B", email = "a@b.com")))
        )

        val response = kotlinx.coroutines.runBlocking {
            api.register(CreateUserRequest(firstName = "A", lastName = "B", password = "pw", email = "a@b.com"))
        }

        assertTrue(response.isSuccessful)
        val req = server.takeRequest()
        assertEquals("/users", req.path)
        assertEquals("POST", req.method)
        val body = req.body.readUtf8()
        assertTrue(body.contains("\"firstName\":\"A\""))
        assertTrue(body.contains("\"password\":\"pw\""))
    }

    @Test
    fun usersApi_disable_putsToUsersIdDisable() {
        val api = retrofit.create(UsersApi::class.java)
        server.enqueue(MockResponse().setResponseCode(200).setBody(""))

        val response = kotlinx.coroutines.runBlocking { api.disableUser(9) }

        assertTrue(response.isSuccessful)
        val req = server.takeRequest()
        assertEquals("/users/9/disable", req.path)
        assertEquals("PUT", req.method)
    }

    @Test
    fun termsApi_getMyTerms_getsFromTerms() {
        val api = retrofit.create(TermsApi::class.java)
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(
                    gson.toJson(
                        listOf(
                            GetTermResponse(id = 1L, title = "t", content = "c", version = 1, active = true)
                        )
                    )
                )
        )

        val list = kotlinx.coroutines.runBlocking { api.getMyTerms() }

        assertEquals(1, list.size)
        val req = server.takeRequest()
        assertEquals("/terms", req.path)
        assertEquals("GET", req.method)
    }

    @Test
    fun termsApi_createTerm_postsToTerms() {
        val api = retrofit.create(TermsApi::class.java)
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(gson.toJson(GetTermResponse(id = 2L, title = "New", content = "Body", version = 2, active = false)))
        )

        val created = kotlinx.coroutines.runBlocking {
            api.createTerm(CreateTermRequest(title = "New", content = "Body", active = false))
        }

        // id is nullable Long? in the model; compare as Long to avoid Integer/Long boxing mismatch.
        assertEquals(2L, created.id)
        val req = server.takeRequest()
        assertEquals("/terms", req.path)
        assertEquals("POST", req.method)
        assertTrue(req.body.readUtf8().contains("\"active\":false"))
    }
}

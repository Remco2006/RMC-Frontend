package com.example.rmcfrontend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.rmcfrontend.api.ApiClient
import com.example.rmcfrontend.api.models.LoginRequest
import com.example.rmcfrontend.auth.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tokenManager = TokenManager(this)

        if (tokenManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        progressBar = findViewById(R.id.login_progress)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vul alle velden in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(email, password)
        }
    }

    private fun performLogin(email: String, password: String) {
        loginButton.isEnabled = false
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.authApi.login(LoginRequest(email, password))
                }

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    print(loginResponse.token)
                    tokenManager.saveToken(
                        loginResponse.token,
                        loginResponse.id,
                        loginResponse.email
                    )

                    ApiClient.setAuthToken(loginResponse.token)

                    Toast.makeText(this@LoginActivity, "Welkom terug!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Login mislukt: ${response.message()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    "Fout: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                loginButton.isEnabled = true
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
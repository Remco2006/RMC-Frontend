package com.example.rmcfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.rmcfrontend.api.ApiClient
import com.example.rmcfrontend.auth.TokenManager
import com.example.rmcfrontend.compose.AppRoot
import com.example.rmcfrontend.compose.theme.RmcTheme
import com.example.rmcfrontend.compose.viewmodel.CarsViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Restore token (if any) so Retrofit adds Authorization automatically.
        val tokenManager = TokenManager(this)
        tokenManager.getToken()?.let { ApiClient.setAuthToken(it) }

        setContent {
            RmcTheme {
                AppRoot(
                    tokenManager = tokenManager,
                    carsViewModel = CarsViewModel()
                )
            }
        }
    }
}

package com.example.rmcfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.rmcfrontend.api.ApiClient
import com.example.rmcfrontend.auth.TokenManager
import com.example.rmcfrontend.compose.AppRoot
import com.example.rmcfrontend.compose.theme.RmcTheme
import com.example.rmcfrontend.di.appModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.startKoin

class MainActivity : ComponentActivity() {

    // ✅ Inject TokenManager via Koin
    private val tokenManager: TokenManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Initialiseer Koin (alleen als nog niet gestart)
        if (GlobalContext.getOrNull() == null) {
            startKoin {
                androidContext(this@MainActivity)
                modules(appModule)
            }
        }

        // Restore token (if any) so Retrofit adds Authorization automatically.
        tokenManager.getToken()?.let { ApiClient.setAuthToken(it) }

        setContent {
            RmcTheme {
                AppRoot(
                    tokenManager = tokenManager,
                )
            }
        }
    }
}
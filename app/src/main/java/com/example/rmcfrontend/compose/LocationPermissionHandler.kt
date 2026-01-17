package com.example.rmcfrontend.compose
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun RequestLocationPermission(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    var showRationale by remember { mutableStateOf(false) }

    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val allGranted = permissionsMap.values.all { it }
        if (allGranted) {
            onPermissionGranted()
        } else {
            showRationale = true
        }
    }

    LaunchedEffect(Unit) {
        val allPermissionsGranted = permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

        if (allPermissionsGranted) {
            onPermissionGranted()
        } else {
            launcher.launch(permissions)
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Locatie toegang vereist") },
            text = {
                Text("Deze app heeft toegang tot je locatie nodig om je rit te kunnen bijhouden. Zonder deze toestemming kunnen we je rit niet tracken.")
            },
            confirmButton = {
                Button(onClick = {
                    showRationale = false
                    launcher.launch(permissions)
                }) {
                    Text("Toestemming geven")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRationale = false
                    onPermissionDenied()
                }) {
                    Text("Annuleren")
                }
            }
        )
    }
}
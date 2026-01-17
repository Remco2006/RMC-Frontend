package com.example.rmcfrontend.compose.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rmcfrontend.api.models.Car
import com.example.rmcfrontend.api.models.CreateTelemetryRequest
import com.example.rmcfrontend.service.ActiveTripService
import com.example.rmcfrontend.compose.viewmodel.TelemetryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveTripScreen(
    carId: Long,
    userId: Long,
    reservationId: Long,
    car: Car?,
    telemetryViewModel: TelemetryViewModel,
    onStopTrip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var distance by remember { mutableDoubleStateOf(0.0) }
    var currentSpeed by remember { mutableDoubleStateOf(0.0) }
    var maxSpeed by remember { mutableDoubleStateOf(0.0) }
    var avgSpeed by remember { mutableDoubleStateOf(0.0) }
    var duration by remember { mutableIntStateOf(0) }

    var showStopDialog by remember { mutableStateOf(false) }
    var showSavingDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                android.util.Log.d("ActiveTripScreen", "Broadcast ontvangen!")
                distance = intent?.getDoubleExtra("distance", 0.0) ?: 0.0
                currentSpeed = intent?.getDoubleExtra("speed", 0.0) ?: 0.0
                maxSpeed = intent?.getDoubleExtra("max_speed", 0.0) ?: 0.0
                avgSpeed = intent?.getDoubleExtra("avg_speed", 0.0) ?: 0.0
                duration = intent?.getIntExtra("duration", 0) ?: 0
                android.util.Log.d("ActiveTripScreen", "Data: speed=$currentSpeed, distance=$distance")
            }
        }

        android.util.Log.d("ActiveTripScreen", "Receiver registreren...")

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                receiver,
                IntentFilter("TRIP_UPDATE"),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            context.registerReceiver(receiver, IntentFilter("TRIP_UPDATE"))
        }

        onDispose {
            android.util.Log.d("ActiveTripScreen", "Receiver afmelden")
            context.unregisterReceiver(receiver)
        }
    }

    if (showSavingDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Rit opslaan...") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Je ritgegevens worden opgeslagen")
                }
            },
            confirmButton = { }
        )
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text("Rit Beëindigen") },
            text = {
                Column {
                    Text("Weet je zeker dat je deze rit wilt beëindigen?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Rit samenvatting:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text("• Afstand: ${String.format("%.2f", distance)} km")
                    Text("• Duur: ${formatDuration(duration)}")
                    Text("• Gem. snelheid: ${avgSpeed.toInt()} km/h")
                    Text("• Max snelheid: ${maxSpeed.toInt()} km/h")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStopDialog = false
                        showSavingDialog = true

                        // Stop de service
                        val stopIntent = Intent(context, ActiveTripService::class.java)
                        context.stopService(stopIntent)

                        // Bereken scores
                        val ecoScore = calculateEcoScore(avgSpeed, maxSpeed)
                        val corneringScore = 75 // Placeholder

                        // Sla telemetry op
                        val telemetryRequest = CreateTelemetryRequest(
                            userId = userId,
                            carId = carId,
                            avgSpeedKmh = avgSpeed,
                            maxSpeedKmh = maxSpeed,
                            tripDistanceKm = distance,
                            tripDurationMin = duration,
                            harshBrakes = 0, // TODO: implement in service
                            harshAccelerations = 0, // TODO: implement in service
                            corneringScore = corneringScore,
                            ecoScore = ecoScore
                        )

                        telemetryViewModel.saveTelemetry(
                            request = telemetryRequest,
                            onSuccess = {
                                showSavingDialog = false
                                onStopTrip()
                            },
                            onError = { error ->
                                showSavingDialog = false
                                android.util.Log.e("ActiveTrip", "Failed to save: $error")
                                onStopTrip() // Still navigate back
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Rit Beëindigen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) {
                    Text("Annuleren")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actieve Rit") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            car?.let { "${it.make} ${it.model}" } ?: "Auto",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            car?.licensePlate ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = pulseAlpha)
                    )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${currentSpeed.toInt()}",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "km/h",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TripStatCard(
                    icon = Icons.Default.Timer,
                    label = "Tijd",
                    value = formatDuration(duration),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                TripStatCard(
                    icon = Icons.Default.Route,
                    label = "Afstand",
                    value = String.format("%.1f km", distance),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TripStatCard(
                    icon = Icons.Default.Speed,
                    label = "Gemiddeld",
                    value = "${avgSpeed.toInt()} km/h",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                TripStatCard(
                    icon = Icons.Default.TrendingUp,
                    label = "Max",
                    value = "${maxSpeed.toInt()} km/h",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Je rit wordt bijgehouden op de achtergrond. Rijd veilig!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showStopDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Rit Beëindigen",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TripStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun formatDuration(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) {
        "${hours}u ${mins}m"
    } else {
        "${mins}m"
    }
}

private fun calculateEcoScore(avgSpeed: Double, maxSpeed: Double): Int {
    val speedPenalty = when {
        avgSpeed > 100 -> 30
        avgSpeed > 80 -> 20
        avgSpeed > 60 -> 10
        else -> 0
    }

    val maxSpeedPenalty = when {
        maxSpeed > 130 -> 20
        maxSpeed > 110 -> 10
        else -> 0
    }

    return (100 - speedPenalty - maxSpeedPenalty).coerceIn(0, 100)
}
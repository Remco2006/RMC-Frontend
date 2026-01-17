package com.example.rmcfrontend.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.rmcfrontend.R
import com.google.android.gms.location.*
import kotlin.math.max

class ActiveTripService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var lastLocation: Location? = null
    private var totalDistance = 0.0 // in meters
    private var startTime = System.currentTimeMillis()
    private var maxSpeed = 0.0
    private var speedSum = 0.0
    private var speedCount = 0
    private var harshBrakes = 0
    private var harshAccelerations = 0
    private var lastSpeed = 0.0

    companion object {
        const val CHANNEL_ID = "ActiveTripChannel"
        const val NOTIFICATION_ID = 1
        const val EXTRA_CAR_ID = "carId"
        const val EXTRA_USER_ID = "userId"
        const val EXTRA_RESERVATION_ID = "reservationId"
        private const val TAG = "ActiveTripService"

        // Thresholds
        private const val HARSH_BRAKE_THRESHOLD = -3.0 // m/s²
        private const val HARSH_ACCEL_THRESHOLD = 3.0 // m/s²
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🚀 Service onCreate()")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        setupLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "▶️ Service onStartCommand()")

        val carId = intent?.getLongExtra(EXTRA_CAR_ID, 0L)
        val userId = intent?.getLongExtra(EXTRA_USER_ID, 0L)
        val reservationId = intent?.getLongExtra(EXTRA_RESERVATION_ID, 0L)

        Log.d(TAG, "📋 Parameters: carId=$carId, userId=$userId, reservationId=$reservationId")

        startForeground(NOTIFICATION_ID, createNotification())
        startLocationUpdates()

        return START_STICKY
    }

    private fun setupLocationCallback() {
        Log.d(TAG, "🔧 Setting up location callback")
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    Log.d(TAG, "📍 Location update: lat=${location.latitude}, lng=${location.longitude}, speed=${location.speed * 3.6} km/h")
                    updateTripData(location)
                    broadcastUpdate()
                }
            }
        }
    }

    private fun updateTripData(location: Location) {
        // Calculate distance
        lastLocation?.let { last ->
            val distance = last.distanceTo(location)
            if (distance < 100) { // Filter out GPS jumps > 100m
                totalDistance += distance
                Log.d(TAG, "📏 Distance added: ${distance}m, Total: ${totalDistance/1000}km")
            } else {
                Log.w(TAG, "⚠️ GPS jump detected: ${distance}m - ignoring")
            }
        }

        // Calculate speed (m/s to km/h)
        val currentSpeed = location.speed * 3.6

        if (currentSpeed > 0) {
            speedSum += currentSpeed
            speedCount++
            maxSpeed = max(maxSpeed, currentSpeed)

            // Detect harsh events
            if (lastSpeed > 0) {
                val timeDiff = 1.0 // Approximate 1 second between updates
                val accel = (currentSpeed - lastSpeed) / 3.6 / timeDiff

                when {
                    accel < HARSH_BRAKE_THRESHOLD -> {
                        harshBrakes++
                        Log.d(TAG, "🚨 Harsh brake detected! Total: $harshBrakes")
                    }
                    accel > HARSH_ACCEL_THRESHOLD -> {
                        harshAccelerations++
                        Log.d(TAG, "⚡ Harsh acceleration detected! Total: $harshAccelerations")
                    }
                }
            }
            lastSpeed = currentSpeed
        }

        lastLocation = location
    }

    private fun broadcastUpdate() {
        val distanceKm = totalDistance / 1000.0
        val currentSpeed = lastLocation?.speed?.times(3.6) ?: 0.0
        val avgSpeed = if (speedCount > 0) speedSum / speedCount else 0.0
        val duration = ((System.currentTimeMillis() - startTime) / 60000).toInt()

        val intent = Intent("TRIP_UPDATE").apply {
            putExtra("distance", distanceKm)
            putExtra("speed", currentSpeed)
            putExtra("max_speed", maxSpeed)
            putExtra("avg_speed", avgSpeed)
            putExtra("duration", duration)
        }

        sendBroadcast(intent)
        Log.d(TAG, "📡 Broadcast sent - Distance: ${String.format("%.2f", distanceKm)}km, Speed: ${currentSpeed.toInt()}km/h, Avg: ${avgSpeed.toInt()}km/h, Duration: ${duration}min")
    }

    private fun startLocationUpdates() {
        Log.d(TAG, "🌍 Starting location updates...")

        // Check permissions
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "❌ Location permission NOT granted!")
            // Send a broadcast to inform the user
            val intent = Intent("TRIP_UPDATE").apply {
                putExtra("error", "Location permission not granted")
            }
            sendBroadcast(intent)
            stopSelf()
            return
        }

        Log.d(TAG, "✅ Location permission granted")

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L // 1 second updates
        ).apply {
            setMinUpdateIntervalMillis(500L)
            setWaitForAccurateLocation(true)
        }.build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        Log.d(TAG, "✅ Location updates requested")

        // Also try to get last known location immediately
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                Log.d(TAG, "📍 Got last known location: ${location.latitude}, ${location.longitude}")
                lastLocation = location
                broadcastUpdate()
            } else {
                Log.w(TAG, "⚠️ No last known location available")
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "❌ Failed to get last location: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Actieve Rit Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Houdt je rit bij"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            Log.d(TAG, "📢 Notification channel created")
        }
    }

    private fun createNotification(): Notification {
        val pendingIntent = try {
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, Class.forName("com.example.rmcfrontend.MainActivity")),
                PendingIntent.FLAG_IMMUTABLE
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create pending intent", e)
            null
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Rit actief")
            .setContentText("Je rit wordt bijgehouden")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    fun getTripData(): TripData {
        val durationMinutes = ((System.currentTimeMillis() - startTime) / 60000).toInt()
        val avgSpeed = if (speedCount > 0) speedSum / speedCount else 0.0

        return TripData(
            distanceKm = totalDistance / 1000.0,
            durationMin = durationMinutes,
            avgSpeedKmh = avgSpeed,
            maxSpeedKmh = maxSpeed,
            harshBrakes = harshBrakes,
            harshAccelerations = harshAccelerations
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🛑 Service onDestroy() - Removing location updates")
        fusedLocationClient.removeLocationUpdates(locationCallback)

        // Send final broadcast
        broadcastUpdate()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

data class TripData(
    val distanceKm: Double,
    val durationMin: Int,
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val harshBrakes: Int,
    val harshAccelerations: Int
)
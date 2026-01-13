package com.example.rmcfrontend.compose.screens.reservations

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.rmcfrontend.api.models.Car
import com.example.rmcfrontend.api.models.Reservation
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationsScreen(
    reservations: List<Reservation>,
    cars: List<Car>,
    onDateSelected: (LocalDate) -> Unit,
    onCreateReservation: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDate by remember {
        mutableStateOf(LocalDate.now())
    }

    val context = LocalContext.current

    val filteredReservations = remember(reservations, selectedDate) {
        val startOfDay = selectedDate.atStartOfDay()
        val endOfDay = selectedDate.atTime(23, 59, 59)

        reservations.filter {
            it.startTime.isAfter(startOfDay) || it.startTime.isEqual(startOfDay) &&
                    it.startTime.isBefore(endOfDay) || it.startTime.isEqual(endOfDay)
        }.sortedBy { it.startTime }
    }

    val upcomingReservation = filteredReservations.firstOrNull()
    val otherReservations = filteredReservations.drop(1)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mijn Reserveringen") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onCreateReservation(selectedDate) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Nieuwe reservering")
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Horizontale datum selector
            DateSelector(
                selectedDate = selectedDate,
                onDateSelected = {
                    selectedDate = it
                    onDateSelected(it)
                },
                modifier = Modifier.padding(vertical = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (upcomingReservation != null) {
                    item {
                        Text(
                            "Eerstvolgende",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        val car = cars.find { it.id == upcomingReservation.carId }
                        UpcomingReservationCard(
                            reservation = upcomingReservation,
                            car = car,
                            onLocationClick = { location ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$location"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                if (otherReservations.isNotEmpty()) {
                    item {
                        Text(
                            "Overige reserveringen",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(otherReservations) { reservation ->
                        val car = cars.find { it.id == reservation.carId }
                        ReservationCard(
                            reservation = reservation,
                            car = car,
                            onLocationClick = { location ->
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$location"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }

                if (filteredReservations.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Geen reserveringen op deze datum",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val dates = (0..30).map { today.plusDays(it.toLong()) }

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(dates) { date ->
            DateChip(
                date = date,
                isSelected = date == selectedDate,
                onClick = { onDateSelected(date) }
            )
        }
    }
}

@Composable
fun DateChip(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun UpcomingReservationCard(
    reservation: Reservation,
    car: Car?,
    onLocationClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Auto afbeelding
                if (car != null && car.imageFileNames.isNotEmpty()) {
                    AsyncImage(
                        model = "${car.imageFileNames.first()}",
                        contentDescription = "Auto afbeelding",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Geen foto", style = MaterialTheme.typography.bodySmall)
                    }
                }

                // Auto informatie
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = car?.let { "${it.make} ${it.model}" } ?: "Onbekende auto",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "${reservation.startTime.hour}:${reservation.startTime.minute.toString().padStart(2, '0')} - " +
                                "${reservation.endTime.hour}:${reservation.endTime.minute.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "Kenteken: ${car?.licensePlate ?: "Onbekend"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Locatie knop
                IconButton(
                    onClick = { onLocationClick(car?.pickupLocation ?: "Pickup Location") },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Open kaart",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Specificaties
            if (car != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SpecItem(label = "Brandstof", value = car.fuelType ?: "N/A")
                    SpecItem(label = "Transmissie", value = car.transmission ?: "N/A")
                    SpecItem(label = "Zitplaatsen", value = car.seats?.toString() ?: "N/A")
                    SpecItem(label = "Deuren", value = car.doors?.toString() ?: "N/A")
                }
            }
        }
    }
}

@Composable
fun ReservationCard(
    reservation: Reservation,
    car: Car?,
    onLocationClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Auto afbeelding
            if (car != null && car.imageFileNames.isNotEmpty()) {
                AsyncImage(
                    model = "${car.imageFileNames.first()}",
                    contentDescription = "Auto afbeelding",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Geen foto", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Auto informatie
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = car?.let { "${it.make} ${it.model}" } ?: "Onbekende auto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    "${reservation.startTime.hour}:${reservation.startTime.minute.toString().padStart(2, '0')} - " +
                            "${reservation.endTime.hour}:${reservation.endTime.minute.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    "Kenteken: ${car?.licensePlate ?: "Onbekend"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Specificaties in compacte vorm
                if (car != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${car.fuelType ?: "N/A"} • ${car.transmission ?: "N/A"} • ${car.seats ?: "N/A"} zitpl.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Locatie knop
            IconButton(
                onClick = { onLocationClick(car?.pickupLocation ?: "Pickup Location") },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Open kaart",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun SpecItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
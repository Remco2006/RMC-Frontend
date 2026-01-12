package com.example.rmcfrontend.ui.theme.screens.cars

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.rmcfrontend.R
import com.example.rmcfrontend.api.models.Car
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rmcfrontend.compose.viewmodel.CarsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailsScreen(
    carId: String,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDelete: (Car) -> Unit,
    carsViewModel: CarsViewModel = viewModel()
) {
    val car by carsViewModel.getCar(carId).collectAsState(initial = null)
    val isLoading by carsViewModel.loading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Car Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else {
            car?.let { c ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Car Image
                    AsyncImage(
                        model = c.imageFileNames.firstOrNull()?.let { "http://10.0.2.2:8080/images/$it" } ?: R.drawable.car,
                        contentDescription = "Car Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Title
                    Text(
                        text = "${c.make ?: "-"} ${c.model ?: "-"}",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    // Basic Information Section
                    Text("Basic Information", style = MaterialTheme.typography.titleMedium)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailRow("User ID", c.userId?.toString())
                            DetailRow("Car ID", c.id?.toString())
                            DetailRow("Make", c.make)
                            DetailRow("Model", c.model)
                            DetailRow("Price", c.price?.let { "€$it" })
                            DetailRow("Pickup Location", c.pickupLocation)
                            DetailRow("Category", c.category)
                            DetailRow("Power Source Type", c.powerSourceType)
                        }
                    }

                    // Specifications Section
                    Text("Specifications", style = MaterialTheme.typography.titleMedium)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailRow("Color", c.color)
                            DetailRow("Engine Type", c.engineType)
                            DetailRow("Engine Power", c.enginePower)
                            DetailRow("Fuel Type", c.fuelType)
                            DetailRow("Transmission", c.transmission)
                        }
                    }

                    // Interior & Exterior Section
                    Text("Interior & Exterior", style = MaterialTheme.typography.titleMedium)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailRow("Interior Type", c.interiorType)
                            DetailRow("Interior Color", c.interiorColor)
                            DetailRow("Exterior Type", c.exteriorType)
                            DetailRow("Exterior Finish", c.exteriorFinish)
                            DetailRow("Wheel Size", c.wheelSize)
                            DetailRow("Wheel Type", c.wheelType)
                        }
                    }

                    // Vehicle Details Section
                    Text("Vehicle Details", style = MaterialTheme.typography.titleMedium)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailRow("Seats", c.seats?.toString())
                            DetailRow("Doors", c.doors?.toString())
                            DetailRow("Model Year", c.modelYear?.toString())
                            DetailRow("Mileage", c.mileage?.let { "$it km" })
                        }
                    }

                    // Registration Section
                    Text("Registration", style = MaterialTheme.typography.titleMedium)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailRow("License Plate", c.licensePlate)
                            DetailRow("VIN Number", c.vinNumber)
                            DetailRow("Trade Name", c.tradeName)
                            DetailRow("First Registration Date", c.firstRegistrationDate)
                        }
                    }

                    // Weight & Costs Section
                    Text("Weight & Costs", style = MaterialTheme.typography.titleMedium)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailRow("BPM", c.bpm?.let { "€$it" })
                            DetailRow("Curb Weight", c.curbWeight?.let { "$it kg" })
                            DetailRow("Max Weight", c.maxWeight?.let { "$it kg" })
                            DetailRow("Booking Cost", c.bookingCost?.let { "€$it" })
                            DetailRow("Cost Per Kilometer", c.costPerKilometer?.let { "€$it" })
                            DetailRow("Deposit", c.deposit?.let { "€$it" })
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action Buttons
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { onEdit(c.id ?: 0L) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Edit")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onDelete(c) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Delete", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun DetailRow(label: String, value: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value ?: "-",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
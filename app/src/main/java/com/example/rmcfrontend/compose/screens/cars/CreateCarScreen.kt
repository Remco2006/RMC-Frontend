package com.example.rmcfrontend.ui.theme.screens.cars

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.rmcfrontend.api.enums.PowerSourceTypeEnum
import com.example.rmcfrontend.api.models.CreateCarRequest
import com.example.rmcfrontend.compose.viewmodel.CarsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCarScreen(
    onBack: () -> Unit,
    onSave: (CreateCarRequest) -> Unit,
    carsViewModel: CarsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val isLoading by carsViewModel.loading.collectAsState()
    val context = LocalContext.current

    // Basic fields
    var userId by rememberSaveable { mutableStateOf("1") }
    var make by rememberSaveable { mutableStateOf("") }
    var model by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var pickupLocation by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }

    // PowerSourceType dropdown
    var selectedPowerSource by rememberSaveable { mutableStateOf<PowerSourceTypeEnum?>(null) }
    var powerSourceExpanded by remember { mutableStateOf(false) }

    // Specifications
    var color by rememberSaveable { mutableStateOf("") }
    var engineType by rememberSaveable { mutableStateOf("") }
    var enginePower by rememberSaveable { mutableStateOf("") }
    var fuelType by rememberSaveable { mutableStateOf("") }
    var transmission by rememberSaveable { mutableStateOf("") }

    // Interior/Exterior
    var interiorType by rememberSaveable { mutableStateOf("") }
    var interiorColor by rememberSaveable { mutableStateOf("") }
    var exteriorType by rememberSaveable { mutableStateOf("") }
    var exteriorFinish by rememberSaveable { mutableStateOf("") }
    var wheelSize by rememberSaveable { mutableStateOf("") }
    var wheelType by rememberSaveable { mutableStateOf("") }

    // Numbers
    var seats by rememberSaveable { mutableStateOf("") }
    var doors by rememberSaveable { mutableStateOf("") }
    var modelYear by rememberSaveable { mutableStateOf("") }
    var mileage by rememberSaveable { mutableStateOf("") }

    // Registration details
    var licensePlate by rememberSaveable { mutableStateOf("") }
    var vinNumber by rememberSaveable { mutableStateOf("") }
    var tradeName by rememberSaveable { mutableStateOf("") }
    var firstRegistrationDate by rememberSaveable { mutableStateOf("") }

    // Weight and costs
    var bpm by rememberSaveable { mutableStateOf("") }
    var curbWeight by rememberSaveable { mutableStateOf("") }
    var maxWeight by rememberSaveable { mutableStateOf("") }
    var bookingCost by rememberSaveable { mutableStateOf("") }
    var costPerKilometer by rememberSaveable { mutableStateOf("") }
    var deposit by rememberSaveable { mutableStateOf("") }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Create Car") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Back")
                }
            }
        )
    }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Basic Information", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = make,
                onValueChange = { make = it },
                label = { Text("Make *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Model *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = pickupLocation,
                onValueChange = { pickupLocation = it },
                label = { Text("Pickup Location") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth()
            )

            // PowerSourceType Dropdown
            ExposedDropdownMenuBox(
                expanded = powerSourceExpanded,
                onExpandedChange = { powerSourceExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedPowerSource?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Power Source Type") },
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = powerSourceExpanded,
                    onDismissRequest = { powerSourceExpanded = false }
                ) {
                    PowerSourceTypeEnum.values().forEach { powerSource ->
                        DropdownMenuItem(
                            text = { Text(powerSource.name) },
                            onClick = {
                                selectedPowerSource = powerSource
                                powerSourceExpanded = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Specifications", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = color,
                onValueChange = { color = it },
                label = { Text("Color") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = engineType,
                onValueChange = { engineType = it },
                label = { Text("Engine Type") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = enginePower,
                onValueChange = { enginePower = it },
                label = { Text("Engine Power") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = fuelType,
                onValueChange = { fuelType = it },
                label = { Text("Fuel Type") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = transmission,
                onValueChange = { transmission = it },
                label = { Text("Transmission") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Interior & Exterior", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = interiorType,
                onValueChange = { interiorType = it },
                label = { Text("Interior Type") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = interiorColor,
                onValueChange = { interiorColor = it },
                label = { Text("Interior Color") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = exteriorType,
                onValueChange = { exteriorType = it },
                label = { Text("Exterior Type") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = exteriorFinish,
                onValueChange = { exteriorFinish = it },
                label = { Text("Exterior Finish") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = wheelSize,
                onValueChange = { wheelSize = it },
                label = { Text("Wheel Size") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = wheelType,
                onValueChange = { wheelType = it },
                label = { Text("Wheel Type") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Vehicle Details", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = seats,
                onValueChange = { seats = it },
                label = { Text("Seats") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = doors,
                onValueChange = { doors = it },
                label = { Text("Doors") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = modelYear,
                onValueChange = { modelYear = it },
                label = { Text("Model Year") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = mileage,
                onValueChange = { mileage = it },
                label = { Text("Mileage") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Registration", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = licensePlate,
                onValueChange = { licensePlate = it },
                label = { Text("License Plate") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = vinNumber,
                onValueChange = { vinNumber = it },
                label = { Text("VIN Number") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = tradeName,
                onValueChange = { tradeName = it },
                label = { Text("Trade Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = firstRegistrationDate,
                onValueChange = { firstRegistrationDate = it },
                label = { Text("First Registration Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Weight & Costs", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = bpm,
                onValueChange = { bpm = it },
                label = { Text("BPM") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = curbWeight,
                onValueChange = { curbWeight = it },
                label = { Text("Curb Weight (kg)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = maxWeight,
                onValueChange = { maxWeight = it },
                label = { Text("Max Weight (kg)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = bookingCost,
                onValueChange = { bookingCost = it },
                label = { Text("Booking Cost") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = costPerKilometer,
                onValueChange = { costPerKilometer = it },
                label = { Text("Cost Per Kilometer") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = deposit,
                onValueChange = { deposit = it },
                label = { Text("Deposit") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onSave(
                        CreateCarRequest(
                            userId = userId.toLongOrNull(),
                            make = make,
                            model = model,
                            price = price.toFloatOrNull(),
                            pickupLocation = pickupLocation.ifBlank { null },
                            category = category.ifBlank { null },
                            powerSourceType = selectedPowerSource,
                            color = color.ifBlank { null },
                            engineType = engineType.ifBlank { null },
                            enginePower = enginePower.ifBlank { null },
                            fuelType = fuelType.ifBlank { null },
                            transmission = transmission.ifBlank { null },
                            interiorType = interiorType.ifBlank { null },
                            interiorColor = interiorColor.ifBlank { null },
                            exteriorType = exteriorType.ifBlank { null },
                            exteriorFinish = exteriorFinish.ifBlank { null },
                            wheelSize = wheelSize.ifBlank { null },
                            wheelType = wheelType.ifBlank { null },
                            seats = seats.toIntOrNull(),
                            doors = doors.toIntOrNull(),
                            modelYear = modelYear.toIntOrNull(),
                            licensePlate = licensePlate.ifBlank { null },
                            mileage = mileage.toIntOrNull(),
                            vinNumber = vinNumber.ifBlank { null },
                            tradeName = tradeName.ifBlank { null },
                            bpm = bpm.toFloatOrNull(),
                            curbWeight = curbWeight.toIntOrNull(),
                            maxWeight = maxWeight.toIntOrNull(),
                            firstRegistrationDate = firstRegistrationDate.ifBlank { null },
                            bookingCost = bookingCost.toFloatOrNull() as String?,
                            costPerKilometer = costPerKilometer.toFloatOrNull(),
                            deposit = deposit.toFloatOrNull() as String?,
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = make.isNotBlank() && model.isNotBlank() && !isLoading
            ) {
                Text("Save Car")
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}
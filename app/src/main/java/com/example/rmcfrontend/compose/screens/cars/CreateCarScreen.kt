package com.example.rmcfrontend.ui.theme.screens.cars

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import com.example.rmcfrontend.api.enums.PowerSourceTypeEnum
import com.example.rmcfrontend.api.models.CreateCarRequest
import com.example.rmcfrontend.compose.viewmodel.CarsViewModel
import com.example.rmcfrontend.util.ImageCaptureUtils
import com.example.rmcfrontend.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCarScreen(
    onBack: () -> Unit,
    onSave: (CreateCarRequest, List<Uri>) -> Unit,
    carsViewModel: CarsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val isLoading by carsViewModel.loading.collectAsState()
    val context = LocalContext.current

    // Images (gallery/camera)
    val selectedImageUris = remember { mutableStateListOf<Uri>() }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val pickImagesLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            // Avoid duplicates
            uris.filterNot { selectedImageUris.contains(it) }.forEach { selectedImageUris.add(it) }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let { selectedImageUris.add(it) }
        }
    }

    // Basic fields
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

    // Error states
    var userIdError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var seatsError by remember { mutableStateOf<String?>(null) }
    var doorsError by remember { mutableStateOf<String?>(null) }
    var modelYearError by remember { mutableStateOf<String?>(null) }
    var mileageError by remember { mutableStateOf<String?>(null) }
    var bpmError by remember { mutableStateOf<String?>(null) }
    var curbWeightError by remember { mutableStateOf<String?>(null) }
    var maxWeightError by remember { mutableStateOf<String?>(null) }
    var bookingCostError by remember { mutableStateOf<String?>(null) }
    var costPerKilometerError by remember { mutableStateOf<String?>(null) }
    var depositError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

    // Validation helpers
    fun validateInteger(value: String, fieldName: String, setError: (String?) -> Unit): Boolean {
        if (value.isBlank()) {
            setError(null)
            return true
        }
        return if (value.toIntOrNull() == null) {
            setError("$fieldName moet een geldig getal zijn")
            false
        } else if (value.toInt() < 0) {
            setError("$fieldName moet positief zijn")
            false
        } else {
            setError(null)
            true
        }
    }

    fun validateFloat(value: String, fieldName: String, setError: (String?) -> Unit): Boolean {
        if (value.isBlank()) {
            setError(null)
            return true
        }
        return if (value.toFloatOrNull() == null) {
            setError("$fieldName moet een geldig decimaal getal zijn")
            false
        } else if (value.toFloat() < 0) {
            setError("$fieldName moet positief zijn")
            false
        } else {
            setError(null)
            true
        }
    }

    fun validateDate(value: String): Boolean {
        if (value.isBlank()) {
            dateError = null
            return true
        }
        val datePattern = Regex("""^\d{4}-\d{2}-\d{2}$""")
        return if (!datePattern.matches(value)) {
            dateError = "Datum moet in formaat YYYY-MM-DD zijn"
            false
        } else {
            dateError = null
            true
        }
    }

    fun validateAllFields(): Boolean {
        val validations = listOf(
            validateFloat(price, "Prijs", { priceError = it }),
            validateInteger(seats, "Stoelen", { seatsError = it }),
            validateInteger(doors, "Deuren", { doorsError = it }),
            validateInteger(modelYear, "Bouwjaar", { modelYearError = it }),
            validateInteger(mileage, "Kilometerstand", { mileageError = it }),
            validateFloat(bpm, "BPM", { bpmError = it }),
            validateInteger(curbWeight, "Leeggewicht", { curbWeightError = it }),
            validateInteger(maxWeight, "Max gewicht", { maxWeightError = it }),
            validateFloat(bookingCost, "Boekingskosten", { bookingCostError = it }),
            validateFloat(costPerKilometer, "Kosten per kilometer", { costPerKilometerError = it }),
            validateFloat(deposit, "Borg", { depositError = it }),
            validateDate(firstRegistrationDate)
        )
        return validations.all { it }
    }

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

            // Images
            Text("Images", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        pickImagesLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_gallery_black_24dp),
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Gallery")
                }

                OutlinedButton(
                    onClick = {
                        pendingCameraUri = ImageCaptureUtils.createTempImageUri(context)
                        pendingCameraUri?.let { takePictureLauncher.launch(it) }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera_black_24dp),
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Camera")
                }
            }

            if (selectedImageUris.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(selectedImageUris) { uri ->
                        Box {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Selected image",
                                modifier = Modifier
                                    .size(110.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { selectedImageUris.remove(uri) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove")
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No images selected yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

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
                onValueChange = {
                    price = it
                    validateFloat(it, "Prijs", { priceError = it })
                },
                label = { Text("Price") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = priceError != null,
                supportingText = priceError?.let { { Text(it) } }
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
                    PowerSourceTypeEnum.entries.forEach { powerSource ->
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
                onValueChange = {
                    seats = it
                    validateInteger(it, "Stoelen", { seatsError = it })
                },
                label = { Text("Seats") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = seatsError != null,
                supportingText = seatsError?.let { { Text(it) } }
            )

            OutlinedTextField(
                value = doors,
                onValueChange = {
                    doors = it
                    validateInteger(it, "Deuren", { doorsError = it })
                },
                label = { Text("Doors") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = doorsError != null,
                supportingText = doorsError?.let { { Text(it) } }
            )

            OutlinedTextField(
                value = modelYear,
                onValueChange = {
                    modelYear = it
                    validateInteger(it, "Bouwjaar", { modelYearError = it })
                },
                label = { Text("Model Year") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = modelYearError != null,
                supportingText = modelYearError?.let { { Text(it) } }
            )

            OutlinedTextField(
                value = mileage,
                onValueChange = {
                    mileage = it
                    validateInteger(it, "Kilometerstand", { mileageError = it })
                },
                label = { Text("Mileage") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = mileageError != null,
                supportingText = mileageError?.let { { Text(it) } }
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
                onValueChange = {
                    firstRegistrationDate = it
                    validateDate(it)
                },
                label = { Text("First Registration Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                isError = dateError != null,
                supportingText = dateError?.let { { Text(it) } }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Weight & Costs", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = bpm,
                onValueChange = {
                    bpm = it
                    validateFloat(it, "BPM", { bpmError = it })
                },
                label = { Text("BPM") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = bpmError != null,
                supportingText = bpmError?.let { { Text(it) } }
            )

            OutlinedTextField(
                value = curbWeight,
                onValueChange = {
                    curbWeight = it
                    validateInteger(it, "Leeggewicht", { curbWeightError = it })
                },
                label = { Text("Curb Weight (kg)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = curbWeightError != null,
                supportingText = curbWeightError?.let { { Text(it) } }
            )

            OutlinedTextField(
                value = maxWeight,
                onValueChange = {
                    maxWeight = it
                    validateInteger(it, "Max gewicht", { maxWeightError = it })
                },
                label = { Text("Max Weight (kg)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = maxWeightError != null,
                supportingText = maxWeightError?.let { { Text(it) } }
            )

            OutlinedTextField(
                value = bookingCost,
                onValueChange = {
                    bookingCost = it
                    validateFloat(it, "Boekingskosten", { bookingCostError = it })
                },
                label = { Text("Booking Cost") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = bookingCostError != null,
                supportingText = bookingCostError?.let { { Text(it) } }
            )

            OutlinedTextField(
                value = costPerKilometer,
                onValueChange = {
                    costPerKilometer = it
                    validateFloat(it, "Kosten per kilometer", { costPerKilometerError = it })
                },
                label = { Text("Cost Per Kilometer") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = costPerKilometerError != null,
                supportingText = costPerKilometerError?.let { { Text(it) } }
            )

            OutlinedTextField(
                value = deposit,
                onValueChange = {
                    deposit = it
                    validateFloat(it, "Borg", { depositError = it })
                },
                label = { Text("Deposit") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = depositError != null,
                supportingText = depositError?.let { { Text(it) } }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (validateAllFields()) {
                        onSave(
                            CreateCarRequest(
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
                                bookingCost = bookingCost.ifBlank { null },
                                costPerKilometer = costPerKilometer.toFloatOrNull(),
                                deposit = deposit.ifBlank { null },
                            ),
                            selectedImageUris.toList()
                        )
                    }
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
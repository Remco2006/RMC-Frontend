package com.example.rmcfrontend.ui.theme.screens.cars

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.rmcfrontend.api.models.UpdateCarRequest
import com.example.rmcfrontend.compose.viewmodel.CarsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCarScreen(
    carId: String,
    onBack: () -> Unit,
    onSave: (UpdateCarRequest) -> Unit,
    carsViewModel: CarsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val car by carsViewModel.getCar(carId).collectAsState(initial = null)
    val isLoading by carsViewModel.loading.collectAsState()

    // Use car data directly, fallback to empty string
    val make = remember(car) { mutableStateOf(car?.make ?: "") }
    val model = remember(car) { mutableStateOf(car?.model ?: "") }
    val price = remember(car) { mutableStateOf(car?.price?.toString() ?: "") }
    val pickupLocation = remember(car) { mutableStateOf(car?.pickupLocation ?: "") }
    val category = remember(car) { mutableStateOf(car?.category ?: "") }
    val powerSourceType = remember(car) { mutableStateOf(car?.powerSourceType ?: "") }
    val color = remember(car) { mutableStateOf(car?.color ?: "") }
    val engineType = remember(car) { mutableStateOf(car?.engineType ?: "") }
    val enginePower = remember(car) { mutableStateOf(car?.enginePower ?: "") }
    val fuelType = remember(car) { mutableStateOf(car?.fuelType ?: "") }
    val transmission = remember(car) { mutableStateOf(car?.transmission ?: "") }
    val interiorType = remember(car) { mutableStateOf(car?.interiorType ?: "") }
    val interiorColor = remember(car) { mutableStateOf(car?.interiorColor ?: "") }
    val exteriorType = remember(car) { mutableStateOf(car?.exteriorType ?: "") }
    val exteriorFinish = remember(car) { mutableStateOf(car?.exteriorFinish ?: "") }
    val wheelSize = remember(car) { mutableStateOf(car?.wheelSize ?: "") }
    val wheelType = remember(car) { mutableStateOf(car?.wheelType ?: "") }
    val seats = remember(car) { mutableStateOf(car?.seats?.toString() ?: "") }
    val doors = remember(car) { mutableStateOf(car?.doors?.toString() ?: "") }
    val modelYear = remember(car) { mutableStateOf(car?.modelYear?.toString() ?: "") }
    val licensePlate = remember(car) { mutableStateOf(car?.licensePlate ?: "") }
    val mileage = remember(car) { mutableStateOf(car?.mileage?.toString() ?: "") }
    val vinNumber = remember(car) { mutableStateOf(car?.vinNumber ?: "") }
    val tradeName = remember(car) { mutableStateOf(car?.tradeName ?: "") }
    val bpm = remember(car) { mutableStateOf(car?.bpm?.toString() ?: "") }
    val curbWeight = remember(car) { mutableStateOf(car?.curbWeight?.toString() ?: "") }
    val maxWeight = remember(car) { mutableStateOf(car?.maxWeight?.toString() ?: "") }
    val firstRegistrationDate = remember(car) { mutableStateOf(car?.firstRegistrationDate ?: "") }
    val bookingCost = remember(car) { mutableStateOf(car?.bookingCost?.toString() ?: "") }
    val costPerKilometer = remember(car) { mutableStateOf(car?.costPerKilometer?.toString() ?: "") }
    val deposit = remember(car) { mutableStateOf(car?.deposit?.toString() ?: "") }

    // Error states
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
            validateFloat(price.value, "Prijs", { priceError = it }),
            validateInteger(seats.value, "Stoelen", { seatsError = it }),
            validateInteger(doors.value, "Deuren", { doorsError = it }),
            validateInteger(modelYear.value, "Bouwjaar", { modelYearError = it }),
            validateInteger(mileage.value, "Kilometerstand", { mileageError = it }),
            validateFloat(bpm.value, "BPM", { bpmError = it }),
            validateInteger(curbWeight.value, "Leeggewicht", { curbWeightError = it }),
            validateInteger(maxWeight.value, "Max gewicht", { maxWeightError = it }),
            validateFloat(bookingCost.value, "Boekingskosten", { bookingCostError = it }),
            validateFloat(costPerKilometer.value, "Kosten per kilometer", { costPerKilometerError = it }),
            validateFloat(deposit.value, "Borg", { depositError = it }),
            validateDate(firstRegistrationDate.value)
        )
        return validations.all { it }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Edit Car") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Back")
                }
            }
        )
    }) { padding ->
        if (car == null && isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        } else if (car != null) {
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
                    value = make.value,
                    onValueChange = { make.value = it },
                    label = { Text("Make *") },
                    placeholder = { Text(car?.make ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = model.value,
                    onValueChange = { model.value = it },
                    label = { Text("Model") },
                    placeholder = { Text(car?.model ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = price.value,
                    onValueChange = {
                        price.value = it
                        validateFloat(it, "Prijs", { priceError = it })
                    },
                    label = { Text("Price") },
                    placeholder = { Text(car?.price?.toString() ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = priceError != null,
                    supportingText = priceError?.let { { Text(it) } }
                )

                OutlinedTextField(
                    value = pickupLocation.value,
                    onValueChange = { pickupLocation.value = it },
                    label = { Text("Pickup Location") },
                    placeholder = { Text(car?.pickupLocation ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category.value,
                    onValueChange = { category.value = it },
                    label = { Text("Category *") },
                    placeholder = { Text(car?.category ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = powerSourceType.value,
                    onValueChange = { powerSourceType.value = it },
                    label = { Text("Power Source Type *") },
                    placeholder = { Text(car?.powerSourceType ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Specifications", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = color.value,
                    onValueChange = { color.value = it },
                    label = { Text("Color") },
                    placeholder = { Text(car?.color ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = engineType.value,
                    onValueChange = { engineType.value = it },
                    label = { Text("Engine Type") },
                    placeholder = { Text(car?.engineType ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = enginePower.value,
                    onValueChange = { enginePower.value = it },
                    label = { Text("Engine Power") },
                    placeholder = { Text(car?.enginePower ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = fuelType.value,
                    onValueChange = { fuelType.value = it },
                    label = { Text("Fuel Type") },
                    placeholder = { Text(car?.fuelType ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = transmission.value,
                    onValueChange = { transmission.value = it },
                    label = { Text("Transmission") },
                    placeholder = { Text(car?.transmission ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Interior & Exterior", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = interiorType.value,
                    onValueChange = { interiorType.value = it },
                    label = { Text("Interior Type") },
                    placeholder = { Text(car?.interiorType ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = interiorColor.value,
                    onValueChange = { interiorColor.value = it },
                    label = { Text("Interior Color") },
                    placeholder = { Text(car?.interiorColor ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = exteriorType.value,
                    onValueChange = { exteriorType.value = it },
                    label = { Text("Exterior Type") },
                    placeholder = { Text(car?.exteriorType ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = exteriorFinish.value,
                    onValueChange = { exteriorFinish.value = it },
                    label = { Text("Exterior Finish") },
                    placeholder = { Text(car?.exteriorFinish ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = wheelSize.value,
                    onValueChange = { wheelSize.value = it },
                    label = { Text("Wheel Size") },
                    placeholder = { Text(car?.wheelSize ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = wheelType.value,
                    onValueChange = { wheelType.value = it },
                    label = { Text("Wheel Type") },
                    placeholder = { Text(car?.wheelType ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Vehicle Details", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = seats.value,
                    onValueChange = {
                        seats.value = it
                        validateInteger(it, "Stoelen", { seatsError = it })
                    },
                    label = { Text("Seats") },
                    placeholder = { Text(car?.seats?.toString() ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = seatsError != null,
                    supportingText = seatsError?.let { { Text(it) } }
                )

                OutlinedTextField(
                    value = doors.value,
                    onValueChange = {
                        doors.value = it
                        validateInteger(it, "Deuren", { doorsError = it })
                    },
                    label = { Text("Doors") },
                    placeholder = { Text(car?.doors?.toString() ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = doorsError != null,
                    supportingText = doorsError?.let { { Text(it) } }
                )

                OutlinedTextField(
                    value = modelYear.value,
                    onValueChange = {
                        modelYear.value = it
                        validateInteger(it, "Bouwjaar", { modelYearError = it })
                    },
                    label = { Text("Model Year") },
                    placeholder = { Text(car?.modelYear?.toString() ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = modelYearError != null,
                    supportingText = modelYearError?.let { { Text(it) } }
                )

                OutlinedTextField(
                    value = mileage.value,
                    onValueChange = {
                        mileage.value = it
                        validateInteger(it, "Kilometerstand", { mileageError = it })
                    },
                    label = { Text("Mileage") },
                    placeholder = { Text(car?.mileage?.toString() ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = mileageError != null,
                    supportingText = mileageError?.let { { Text(it) } }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Registration", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = licensePlate.value,
                    onValueChange = { licensePlate.value = it },
                    label = { Text("License Plate") },
                    placeholder = { Text(car?.licensePlate ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = vinNumber.value,
                    onValueChange = { vinNumber.value = it },
                    label = { Text("VIN Number") },
                    placeholder = { Text(car?.vinNumber ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tradeName.value,
                    onValueChange = { tradeName.value = it },
                    label = { Text("Trade Name") },
                    placeholder = { Text(car?.tradeName ?: "") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = firstRegistrationDate.value,
                    onValueChange = {
                        firstRegistrationDate.value = it
                        validateDate(it)
                    },
                    label = { Text("First Registration Date (YYYY-MM-DD)") },
                    placeholder = { Text(car?.firstRegistrationDate ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = dateError != null,
                    supportingText = dateError?.let { { Text(it) } }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Weight & Costs", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = bpm.value,
                    onValueChange = {
                        bpm.value = it
                        validateFloat(it, "BPM", { bpmError = it })
                    },
                    label = { Text("BPM") },
                    placeholder = { Text(car?.bpm?.toString() ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = bpmError != null,
                    supportingText = bpmError?.let { { Text(it) } }
                )

                OutlinedTextField(
                    value = curbWeight.value,
                    onValueChange = {
                        curbWeight.value = it
                        validateInteger(it, "Leeggewicht", { curbWeightError = it })
                    },
                    label = { Text("Curb Weight (kg)") },
                    placeholder = { Text(car?.curbWeight?.toString() ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = curbWeightError != null,
                    supportingText = curbWeightError?.let { { Text(it) } }
                )

                OutlinedTextField(
                    value = maxWeight.value,
                    onValueChange = {
                        maxWeight.value = it
                        validateInteger(it, "Max gewicht", { maxWeightError = it })
                    },
                    label = { Text("Max Weight (kg)") },
                    placeholder = { Text(car?.maxWeight?.toString() ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = maxWeightError != null,
                    supportingText = maxWeightError?.let { { Text(it) } }
                )

                OutlinedTextField(
                    value = bookingCost.value,
                    onValueChange = {
                        bookingCost.value = it
                        validateFloat(it, "Boekingskosten", { bookingCostError = it })
                    },
                    label = { Text("Booking Cost") },
                    placeholder = { Text(car?.bookingCost?.toString() ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = bookingCostError != null,
                    supportingText = bookingCostError?.let { { Text(it) } }
                )

                OutlinedTextField(
                    value = costPerKilometer.value,
                    onValueChange = {
                        costPerKilometer.value = it
                        validateFloat(it, "Kosten per kilometer", { costPerKilometerError = it })
                    },
                    label = { Text("Cost Per Kilometer") },
                    placeholder = { Text(car?.costPerKilometer?.toString() ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = costPerKilometerError != null,
                    supportingText = costPerKilometerError?.let { { Text(it) } }
                )

                OutlinedTextField(
                    value = deposit.value,
                    onValueChange = {
                        deposit.value = it
                        validateFloat(it, "Borg", { depositError = it })
                    },
                    label = { Text("Deposit") },
                    placeholder = { Text(car?.deposit?.toString() ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = depositError != null,
                    supportingText = depositError?.let { { Text(it) } }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (validateAllFields()) {
                            car?.id?.let { id ->
                                val request = UpdateCarRequest(
                                    id = id,
                                    make = make.value,
                                    model = model.value.ifBlank { null },
                                    price = price.value.toFloatOrNull(),
                                    pickupLocation = pickupLocation.value.ifBlank { null },
                                    category = category.value,
                                    powerSourceType = powerSourceType.value,
                                    color = color.value.ifBlank { null },
                                    engineType = engineType.value.ifBlank { null },
                                    enginePower = enginePower.value.ifBlank { null },
                                    fuelType = fuelType.value.ifBlank { null },
                                    transmission = transmission.value.ifBlank { null },
                                    interiorType = interiorType.value.ifBlank { null },
                                    interiorColor = interiorColor.value.ifBlank { null },
                                    exteriorType = exteriorType.value.ifBlank { null },
                                    exteriorFinish = exteriorFinish.value.ifBlank { null },
                                    wheelSize = wheelSize.value.ifBlank { null },
                                    wheelType = wheelType.value.ifBlank { null },
                                    seats = seats.value.toIntOrNull(),
                                    doors = doors.value.toIntOrNull(),
                                    modelYear = modelYear.value.toIntOrNull(),
                                    licensePlate = licensePlate.value.ifBlank { null },
                                    mileage = mileage.value.toIntOrNull(),
                                    vinNumber = vinNumber.value.ifBlank { null },
                                    tradeName = tradeName.value.ifBlank { null },
                                    bpm = bpm.value.toFloatOrNull(),
                                    curbWeight = curbWeight.value.toIntOrNull(),
                                    maxWeight = maxWeight.value.toIntOrNull(),
                                    firstRegistrationDate = firstRegistrationDate.value.ifBlank { null },
                                    bookingCost = bookingCost.value.toFloatOrNull(),
                                    costPerKilometer = costPerKilometer.value.toFloatOrNull(),
                                    deposit = deposit.value.toFloatOrNull()
                                )
                                onSave(request)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = make.value.isNotBlank() && category.value.isNotBlank() && powerSourceType.value.isNotBlank() && !isLoading
                ) {
                    Text("Save Changes")
                }

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
        }
    }
}
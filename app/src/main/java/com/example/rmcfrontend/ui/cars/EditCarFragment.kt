package com.example.rmcfrontend.ui.cars

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.rmcfrontend.R
import com.example.rmcfrontend.api.ApiClient
import com.example.rmcfrontend.api.enums.PowerSourceTypeEnum
import com.example.rmcfrontend.api.models.Car
import com.example.rmcfrontend.api.models.request.UpdateCarRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditCarFragment : Fragment(R.layout.fragment_edit_car) {

    private val args: EditCarFragmentArgs by navArgs()

    private lateinit var progressBar: ProgressBar
    private lateinit var makeEdit: EditText
    private lateinit var modelEdit: EditText
    private lateinit var priceEdit: EditText
    private lateinit var colorEdit: EditText
    private lateinit var transmissionEdit: EditText
    private lateinit var engineTypeEdit: EditText
    private lateinit var enginePowerEdit: EditText
    private lateinit var bookingCostEdit: EditText
    private lateinit var depositEdit: EditText
    private lateinit var costPerKmEdit: EditText
    private lateinit var saveButton: Button

    private var car: Car? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind UI elementen
        progressBar = view.findViewById(R.id.edit_car_progress)
        makeEdit = view.findViewById(R.id.edit_make)
        modelEdit = view.findViewById(R.id.edit_model)
        priceEdit = view.findViewById(R.id.edit_price)
        colorEdit = view.findViewById(R.id.edit_color)
        transmissionEdit = view.findViewById(R.id.edit_transmission)
        engineTypeEdit = view.findViewById(R.id.edit_engineType)
        enginePowerEdit = view.findViewById(R.id.edit_enginePower)
        bookingCostEdit = view.findViewById(R.id.edit_bookingCost)
        depositEdit = view.findViewById(R.id.edit_deposit)
        costPerKmEdit = view.findViewById(R.id.edit_costPerKilometer)
        saveButton = view.findViewById(R.id.edit_save_btn)

        // Laad auto gegevens
        loadCar(args.carId)

        // Save button klik
        saveButton.setOnClickListener {
            car?.let { c ->
                val updatedCar = c.copy(
                    make = makeEdit.text.toString(),
                    model = modelEdit.text.toString(),
                    price = priceEdit.text.toString().toFloatOrNull(),
                    color = colorEdit.text.toString(),
                    transmission = transmissionEdit.text.toString(),
                    engineType = engineTypeEdit.text.toString(),
                    enginePower = enginePowerEdit.text.toString(),
                    bookingCost = bookingCostEdit.text.toString().toFloatOrNull(),
                    deposit = depositEdit.text.toString().toFloatOrNull(),
                    costPerKilometer = costPerKmEdit.text.toString().toFloatOrNull(),
                )
                updateCar(updatedCar)
            }
        }
    }

    private fun loadCar(carId: String) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.carsApi.getCar(carId)
                }

                car = response
                car?.let { c ->
                    // Veilige conversie naar String
                    makeEdit.setText(c.make ?: "")
                    modelEdit.setText(c.model ?: "")
                    priceEdit.setText(c.price?.toString() ?: "")
                    colorEdit.setText(c.color ?: "")
                    transmissionEdit.setText(c.transmission ?: "")
                    engineTypeEdit.setText(c.engineType ?: "")
                    enginePowerEdit.setText(c.enginePower ?: "")
                    bookingCostEdit.setText(c.bookingCost?.toString() ?: "")
                    depositEdit.setText(c.deposit?.toString() ?: "")
                    costPerKmEdit.setText(c.costPerKilometer?.toString() ?: "")
                }

            } catch (t: Throwable) {
                Toast.makeText(context, "Fout bij laden: ${t.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateCar(updatedCar: Car) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val request = UpdateCarRequest(
                    id = updatedCar.id ?: 0L,
                    make = updatedCar.make.orEmpty(),
                    model = updatedCar.model,
                    price = updatedCar.price,
                    pickupLocation = updatedCar.pickupLocation,
                    category = updatedCar.category.orEmpty(),
                    powerSourceType = (updatedCar.powerSourceType ?: PowerSourceTypeEnum.ICE) as String,
                    color = updatedCar.color,
                    engineType = updatedCar.engineType,
                    enginePower = updatedCar.enginePower,
                    fuelType = updatedCar.fuelType,
                    transmission = updatedCar.transmission,
                    interiorType = updatedCar.interiorType,
                    interiorColor = updatedCar.interiorColor,
                    exteriorType = updatedCar.exteriorType,
                    exteriorFinish = updatedCar.exteriorFinish,
                    wheelSize = updatedCar.wheelSize,
                    wheelType = updatedCar.wheelType,
                    seats = updatedCar.seats,
                    doors = updatedCar.doors,
                    modelYear = updatedCar.modelYear,
                    licensePlate = updatedCar.licensePlate,
                    mileage = updatedCar.mileage,
                    vinNumber = updatedCar.vinNumber,
                    tradeName = updatedCar.tradeName,
                    bpm = updatedCar.bpm,
                    curbWeight = updatedCar.curbWeight,
                    maxWeight = updatedCar.maxWeight,
                    firstRegistrationDate = updatedCar.firstRegistrationDate,
                    bookingCost = updatedCar.bookingCost,
                    costPerKilometer = updatedCar.costPerKilometer,
                    deposit = updatedCar.deposit,
                    imageFileNames = updatedCar.imageFileNames.toMutableList()
                )

                withContext(Dispatchers.IO) {
                    ApiClient.carsApi.updateCar(updatedCar.id.toString(), request)
                }

                Toast.makeText(context, "Auto succesvol bijgewerkt", Toast.LENGTH_SHORT).show()
            } catch (t: Throwable) {
                Toast.makeText(context, "Fout bij update: ${t.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}

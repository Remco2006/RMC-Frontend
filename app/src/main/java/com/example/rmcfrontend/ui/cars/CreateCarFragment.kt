package com.example.rmcfrontend.ui.cars

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.rmcfrontend.R
import com.example.rmcfrontend.api.ApiClient
import com.example.rmcfrontend.api.enums.PowerSourceTypeEnum
import com.example.rmcfrontend.api.models.CreateCarRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateCarFragment : Fragment(R.layout.fragment_create_car) {

    private lateinit var progressBar: ProgressBar
    private lateinit var makeEdit: EditText
    private lateinit var modelEdit: EditText
    private lateinit var priceEdit: EditText
    private lateinit var pickupEdit: EditText
    private lateinit var categoryEdit: EditText
    private lateinit var powerSpinner: Spinner
    private lateinit var colorEdit: EditText
    private lateinit var engineTypeEdit: EditText
    private lateinit var enginePowerEdit: EditText
    private lateinit var fuelTypeEdit: EditText
    private lateinit var transmissionEdit: EditText
    private lateinit var seatsEdit: EditText
    private lateinit var doorsEdit: EditText
    private lateinit var modelYearEdit: EditText
    private lateinit var bookingCostEdit: EditText
    private lateinit var costPerKmEdit: EditText
    private lateinit var depositEdit: EditText
    private lateinit var saveButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.create_car_progress)
        makeEdit = view.findViewById(R.id.create_make)
        modelEdit = view.findViewById(R.id.create_model)
        priceEdit = view.findViewById(R.id.create_price)
        pickupEdit = view.findViewById(R.id.create_pickupLocation)
        categoryEdit = view.findViewById(R.id.create_category)
        powerSpinner = view.findViewById(R.id.create_powerSourceType)
        colorEdit = view.findViewById(R.id.create_color)
        engineTypeEdit = view.findViewById(R.id.create_engineType)
        enginePowerEdit = view.findViewById(R.id.create_enginePower)
        fuelTypeEdit = view.findViewById(R.id.create_fuelType)
        transmissionEdit = view.findViewById(R.id.create_transmission)
        seatsEdit = view.findViewById(R.id.create_seats)
        doorsEdit = view.findViewById(R.id.create_doors)
        modelYearEdit = view.findViewById(R.id.create_modelYear)
        bookingCostEdit = view.findViewById(R.id.create_bookingCost)
        costPerKmEdit = view.findViewById(R.id.create_costPerKilometer)
        depositEdit = view.findViewById(R.id.create_deposit)
        saveButton = view.findViewById(R.id.create_save_btn)

        // PowerSourceType dropdown
        val powerValues = PowerSourceTypeEnum.entries.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, powerValues)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        powerSpinner.adapter = adapter

        saveButton.setOnClickListener {
            val request = CreateCarRequest(
                userId = 1, // TODO: huidige gebruiker
                make = makeEdit.text.toString(),
                model = modelEdit.text.toString(),
                price = priceEdit.text.toString().toFloatOrNull(),
                pickupLocation = pickupEdit.text.toString(), // nu geen TODO()
                category = categoryEdit.text.toString(),
                powerSourceType = PowerSourceTypeEnum.valueOf(powerSpinner.selectedItem.toString()),
                color = colorEdit.text.toString(),
                engineType = engineTypeEdit.text.toString(),
                enginePower = enginePowerEdit.text.toString(),
                fuelType = fuelTypeEdit.text.toString(),
                transmission = transmissionEdit.text.toString(),
                seats = seatsEdit.text.toString().toIntOrNull(),
                doors = doorsEdit.text.toString().toIntOrNull(),
                modelYear = modelYearEdit.text.toString().toIntOrNull(),
                licensePlate = "", // voorlopig leeg
                mileage = 0, // voorlopig default
                vinNumber = "",
                tradeName = "",
                bpm = 0f,
                curbWeight = 0,
                maxWeight = 0,
                firstRegistrationDate = "",
                bookingCost = bookingCostEdit.text.toString(),
                costPerKilometer = costPerKmEdit.text.toString().toDoubleOrNull() ?: 0.0,
                deposit = depositEdit.text.toString(),
                interiorType = "",
                interiorColor = "",
                exteriorType = "",
                exteriorFinish = "",
                wheelSize = "",
                wheelType = "",
            )
            createCar(request)
        }
    }

    private fun createCar(request: CreateCarRequest) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    ApiClient.carsApi.createCar(request)
                }
                Toast.makeText(context, "Auto succesvol toegevoegd", Toast.LENGTH_SHORT).show()
            } catch (t: Throwable) {
                Log.e("CarCreateFragment", "Fout bij aanmaken auto", t)
                Toast.makeText(context, "Fout bij aanmaken: ${t.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}

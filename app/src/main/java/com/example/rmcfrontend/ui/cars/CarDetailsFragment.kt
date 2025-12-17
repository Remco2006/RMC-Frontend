package com.example.rmcfrontend.ui.cars

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.rmcfrontend.R
import com.example.rmcfrontend.api.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CarDetailsFragment : Fragment(R.layout.fragment_car_details) {

    private lateinit var progressBar: ProgressBar
    private lateinit var image: ImageView
    private lateinit var title: TextView
    private lateinit var subtitle: TextView
    private lateinit var price: TextView
    private lateinit var category: TextView
    private lateinit var color: TextView
    private lateinit var transmission: TextView
    private lateinit var mileage: TextView
    private lateinit var engine: TextView
    private lateinit var licensePlate: TextView
    private lateinit var bookingCost: TextView
    private lateinit var deposit: TextView
    private lateinit var editButton: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.car_progress)
        image = view.findViewById(R.id.car_image)
        title = view.findViewById(R.id.car_title)
        subtitle = view.findViewById(R.id.car_subtitle)
        price = view.findViewById(R.id.car_price)
        category = view.findViewById(R.id.car_category)
        color = view.findViewById(R.id.car_color)
        transmission = view.findViewById(R.id.car_transmission)
        mileage = view.findViewById(R.id.car_mileage)
        engine = view.findViewById(R.id.car_engine)
        licensePlate = view.findViewById(R.id.car_license_plate)
        bookingCost = view.findViewById(R.id.car_booking_cost)
        deposit = view.findViewById(R.id.car_deposit)
        editButton = view.findViewById(R.id.car_edit)

        val carId = arguments?.getString("carId")
        if (carId != null) loadCarDetails(carId)
        else Toast.makeText(context, "Geen carId ontvangen", Toast.LENGTH_SHORT).show()
    }

    private fun loadCarDetails(carId: String) {
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.carsApi.getCar(carId)
                }

                val car = response ?: run {
                    Toast.makeText(context, "Auto niet gevonden", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                title.text = "${car.make.orEmpty()} ${car.model.orEmpty()}".trim()
                subtitle.text = "Year: ${car.modelYear ?: "-"}"
                price.text = car.price?.let { "€ %.2f".format(it) } ?: "-"
                category.text = "Category: ${car.category ?: "-"}"
                color.text = "Color: ${car.color ?: "-"}"
                transmission.text = "Transmission: ${car.transmission ?: "-"}"
                mileage.text = "Mileage: ${car.mileage ?: "-"} km"
                engine.text = "Engine: ${car.engineType ?: "-"} ${car.enginePower ?: ""}"
                licensePlate.text = "License Plate: ${car.licensePlate ?: "-"}"
                bookingCost.text = "Booking Cost: ${car.bookingCost ?: "-"}"
                deposit.text = "Deposit: ${car.deposit ?: "-"}"

                // Glide image
                if (car.imageFileNames.isNotEmpty()) {
                    val url = "http://10.0.2.2:8080/images/${car.imageFileNames[0]}"
                    Glide.with(this@CarDetailsFragment)
                        .load(url)
                        .centerCrop()
                        .into(image)
                } else {
                    image.setImageResource(R.drawable.car)
                }

                // Edit knop click
                editButton.setOnClickListener {
                    val bundle = Bundle().apply { putString("carId", car.id.toString()) }
                    findNavController().navigate(R.id.editCarFragment, bundle)
                }

            } catch (t: Throwable) {
                Log.e("CarDetailsFragment", "Fout bij laden auto", t)
                Toast.makeText(context, "Kan auto niet laden: ${t.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}

package com.example.rmcfrontend.ui.cars

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rmcfrontend.R
import com.example.rmcfrontend.api.ApiClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CarsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CarAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_cars, container, false)

        recyclerView = root.findViewById(R.id.cars_recycler)
        progressBar = root.findViewById(R.id.cars_progress)
        emptyText = root.findViewById(R.id.cars_empty)

        adapter = CarAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadCars()
        return root
    }

    private fun loadCars() {
        progressBar.visibility = View.VISIBLE
        emptyText.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.carsApi.getAllCars()
                }

                val cars = response.GetCarResponseList

                adapter.submitList(cars)
                emptyText.visibility =
                    if (cars.isEmpty()) View.VISIBLE else View.GONE

            } catch (t: Throwable) {
                emptyText.visibility = View.VISIBLE
                emptyText.text = "Kon auto's niet laden: ${t.message}"
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}

package com.example.rmcfrontend.api.models.response

import com.example.rmcfrontend.api.models.Car

data class CarsResponse(
    val GetCarResponseList: List<Car>
)
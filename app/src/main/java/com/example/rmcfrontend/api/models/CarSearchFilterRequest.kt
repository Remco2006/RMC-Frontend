package com.example.rmcfrontend.api.models

data class CarSearchFilterRequest (
    val latitude: Double? = null,
    val longitude: Double? = null,
    val maxDistanceKm: Double? = null,

    val make: String? = null,
    val model: String? = null,
    val powerSourceType: String? = null,
    val category: String? = null,
    val fuelType: String? = null,
    val transmission: String? = null,

    val color: String? = null,
    val interiorColor: String? = null,
    val exteriorType: String? = null,

    val minPrice: Double? = null,
    val maxPrice: Double? = null,

    val minSeats: Int? = null,
    val maxSeats: Int? = null,
    val minDoors: Int? = null,
    val maxDoors: Int? = null,

    val minModelYear: Int? = null,
    val maxModelYear: Int? = null,
    val minMileage: Int? = null,
    val maxMileage: Int? = null,

    val searchQuery: String? = null
)
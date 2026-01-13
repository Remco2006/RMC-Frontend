package com.example.rmcfrontend.api.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Reservation(
    val id: Long? = null,

    @SerializedName("startTime")
    private val _startTime: String,

    @SerializedName("endTime")
    private val _endTime: String,

    val userId: Long,
    val carId: Long,

    @SerializedName("createdAt")
    private val _createdAt: String? = null,

    @SerializedName("modifiedAt")
    private val _modifiedAt: String? = null
) {
    val startTime: LocalDateTime
        get() = LocalDateTime.parse(_startTime)

    val endTime: LocalDateTime
        get() = LocalDateTime.parse(_endTime)

    val createdAt: LocalDateTime?
        get() = _createdAt?.let { LocalDateTime.parse(it) }

    val modifiedAt: LocalDateTime?
        get() = _modifiedAt?.let { LocalDateTime.parse(it) }
}
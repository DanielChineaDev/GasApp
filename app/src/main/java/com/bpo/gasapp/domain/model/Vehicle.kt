package com.bpo.gasapp.domain.model

data class Vehicle(
    val id: Long = 0,
    val name: String,
    val fuel: FuelType,
    val consumption: Double
)

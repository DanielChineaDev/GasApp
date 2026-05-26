package com.bpo.gasapp.domain.model

data class PricePoint(
    val fuel: FuelType,
    val price: Double,
    val timestamp: Long
)

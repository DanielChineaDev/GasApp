package com.bpo.gasapp.data.local.entity

import androidx.room.Entity

@Entity(tableName = "price_history", primaryKeys = ["stationId", "fuel", "timestamp"])
data class PriceHistoryEntity(
    val stationId: String,
    val fuel: String,
    val price: Double,
    val timestamp: Long
)

package com.bpo.gasapp.ui.components

import android.content.Context
import android.content.Intent
import com.bpo.gasapp.domain.model.FuelType
import com.bpo.gasapp.domain.model.Station

/** Opens the system share sheet with the station's name, prices and location. */
fun shareStation(context: Context, station: Station, fuel: FuelType? = null) {
    val maps = "https://maps.google.com/?q=${station.latitude},${station.longitude}"
    val text = buildString {
        appendLine(station.brand)
        if (station.address.isNotBlank()) appendLine(station.address)
        val place = listOf(station.city, station.province).filter { it.isNotBlank() }.joinToString(", ")
        if (place.isNotBlank()) appendLine(place)
        appendLine()
        val fuels = if (fuel != null) listOf(fuel) else FuelType.entries
        fuels.forEach { f ->
            station.priceOf(f)?.let { appendLine("${f.label}: %.3f €/L".format(it)) }
        }
        appendLine()
        append(maps)
        append("\n\nvía GasApp")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Gasolinera ${station.brand}")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir gasolinera"))
}

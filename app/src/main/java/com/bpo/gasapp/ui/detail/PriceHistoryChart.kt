package com.bpo.gasapp.ui.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bpo.gasapp.domain.model.FuelType
import com.bpo.gasapp.domain.model.PricePoint

private val fuelColors = mapOf(
    FuelType.GASOLINA_95 to Color(0xFF2E7D32),
    FuelType.GASOLINA_98 to Color(0xFF1565C0),
    FuelType.DIESEL to Color(0xFFEF6C00),
    FuelType.DIESEL_PREMIUM to Color(0xFF6A1B9A)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PriceHistoryChart(history: List<PricePoint>, modifier: Modifier = Modifier) {
    val byFuel = history.groupBy { it.fuel }
        .mapValues { (_, points) -> points.sortedBy { it.timestamp } }
        .filterValues { it.size >= 2 }

    if (byFuel.isEmpty()) {
        Text(
            "Aún no hay suficiente historial. Se registra cada vez que se actualizan los precios de tus favoritas.",
            style = MaterialTheme.typography.bodySmall,
            modifier = modifier
        )
        return
    }

    val allPrices = byFuel.values.flatten().map { it.price }
    val minPrice = allPrices.min()
    val maxPrice = allPrices.max()
    val range = (maxPrice - minPrice).takeIf { it > 0.0001 } ?: 1.0
    val minTime = byFuel.values.flatten().minOf { it.timestamp }
    val maxTime = byFuel.values.flatten().maxOf { it.timestamp }
    val timeSpan = (maxTime - minTime).takeIf { it > 0 } ?: 1L

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp)
        ) {
            byFuel.forEach { (fuel, points) ->
                val color = fuelColors[fuel] ?: Color.Gray
                val offsets = points.map { point ->
                    val x = ((point.timestamp - minTime).toFloat() / timeSpan) * size.width
                    val y = size.height - ((point.price - minPrice).toFloat() / range.toFloat()) * size.height
                    Offset(x, y)
                }
                for (i in 0 until offsets.size - 1) {
                    drawLine(
                        color = color,
                        start = offsets[i],
                        end = offsets[i + 1],
                        strokeWidth = 4f
                    )
                }
            }
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            byFuel.keys.forEach { fuel ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .background(fuelColors[fuel] ?: Color.Gray)
                    )
                    Text(
                        "  ${fuel.label}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Text(
            "Rango: %.3f € – %.3f €".format(minPrice, maxPrice),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

package com.bpo.gasapp.ui.stats

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.bpo.gasapp.domain.model.Refuel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Writes refuels to a CSV file in the cache and opens the share sheet. */
fun exportRefuelsCsv(context: Context, refuels: List<Refuel>) {
    if (refuels.isEmpty()) return
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val csv = buildString {
        appendLine("fecha,gasolinera,combustible,litros,importe_eur,precio_eur_l,cuentakilometros")
        refuels.sortedByDescending { it.timestamp }.forEach { r ->
            val name = r.stationName.replace(",", " ")
            val price = r.pricePerLiter?.let { "%.3f".format(it) } ?: ""
            val odo = r.odometer?.let { "%.0f".format(it) } ?: ""
            appendLine(
                "${dateFormat.format(Date(r.timestamp))},$name,${r.fuel.label}," +
                    "%.2f,%.2f,$price,$odo".format(r.liters, r.amount)
            )
        }
    }

    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val file = File(dir, "repostajes.csv")
    file.writeText(csv)

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Exportar repostajes"))
}

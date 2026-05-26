package com.bpo.gasapp.ui.detail

import android.net.Uri

object StationDetailRoute {
    const val ARG_ID = "stationId"
    const val PATTERN = "station/{$ARG_ID}"

    fun build(id: String): String = "station/${Uri.encode(id)}"
}

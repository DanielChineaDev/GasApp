package com.bpo.gasapp.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Opens turn-by-turn navigation to the given coordinates, trying Google Maps
 * first, then any geo: handler, and finally the web (browser always works).
 * Never throws: silently does nothing only if even a browser is unavailable.
 */
fun openNavigation(context: Context, latitude: Double, longitude: Double) {
    val candidates = listOf(
        Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$latitude,$longitude"))
            .apply { setPackage("com.google.android.apps.maps") },
        Intent(Intent.ACTION_VIEW, Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")),
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")
        )
    )
    for (intent in candidates) {
        try {
            context.startActivity(intent)
            return
        } catch (_: ActivityNotFoundException) {
            // try next
        } catch (_: Exception) {
            // try next
        }
    }
}

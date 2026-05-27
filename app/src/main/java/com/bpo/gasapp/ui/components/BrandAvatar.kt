package com.bpo.gasapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.absoluteValue

private val brandColors: List<Pair<String, Color>> = listOf(
    "REPSOL" to Color(0xFFE65100),
    "CEPSA" to Color(0xFFC62828),
    "BP" to Color(0xFF2E7D32),
    "GALP" to Color(0xFFEF6C00),
    "SHELL" to Color(0xFFF9A825),
    "PETRONOR" to Color(0xFF1565C0),
    "DISA" to Color(0xFFAD1457),
    "AVIA" to Color(0xFF0277BD),
    "CARREFOUR" to Color(0xFF1976D2),
    "ALCAMPO" to Color(0xFFD84315),
    "BALLENOIL" to Color(0xFF0D47A1),
    "PLENOIL" to Color(0xFF00838F),
    "PETROPRIX" to Color(0xFFEF6C00),
    "MEROIL" to Color(0xFF283593),
    "TGAS" to Color(0xFF00695C),
    "EROSKI" to Color(0xFFC2185B),
    "GM" to Color(0xFF455A64)
)

private val palette = listOf(
    Color(0xFF1565C0), Color(0xFF2E7D32), Color(0xFFEF6C00), Color(0xFF6A1B9A),
    Color(0xFFC62828), Color(0xFF00838F), Color(0xFF4E342E), Color(0xFFAD1457)
)

fun brandColor(brand: String): Color {
    val upper = brand.uppercase()
    brandColors.firstOrNull { upper.contains(it.first) }?.let { return it.second }
    return palette[brand.hashCode().absoluteValue % palette.size]
}

private fun brandInitials(brand: String): String {
    val clean = brand.trim()
    if (clean.isEmpty()) return "?"
    val words = clean.split(" ", "-").filter { it.isNotBlank() }
    return when {
        words.size >= 2 -> (words[0].first().toString() + words[1].first()).uppercase()
        clean.length >= 2 -> clean.take(2).uppercase()
        else -> clean.take(1).uppercase()
    }
}

@Composable
fun BrandAvatar(brand: String, size: Int = 44, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size.dp)
            .background(brandColor(brand), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = brandInitials(brand),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size * 0.36f).sp
        )
    }
}

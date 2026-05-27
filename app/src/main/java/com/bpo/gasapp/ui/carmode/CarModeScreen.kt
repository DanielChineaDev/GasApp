package com.bpo.gasapp.ui.carmode

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarModeScreen(
    onBack: () -> Unit,
    viewModel: CarModeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Modo coche") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val station = state.cheapest
            if (station == null) {
                Text(
                    if (state.hasLocation) "Buscando gasolineras..." else "Activa la ubicación",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            } else {
                Text("MÁS BARATA · ${state.fuel.label}", style = MaterialTheme.typography.titleMedium)
                Text(
                    station.brand,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = station.priceOf(state.fuel)?.let { "%.3f".format(it) } ?: "—",
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("€/litro", style = MaterialTheme.typography.titleLarge)
                station.distanceMeters?.let {
                    Text(
                        if (it < 1000) "a ${it.toInt()} m" else "a %.1f km".format(it / 1000f),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Button(
                    onClick = {
                        val uri = Uri.parse("google.navigation:q=${station.latitude},${station.longitude}")
                        val intent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.google.android.apps.maps") }
                        runCatching { context.startActivity(intent) }.onFailure {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:${station.latitude},${station.longitude}")))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null)
                    Text("  Ir allí", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

package com.bpo.gasapp.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpo.gasapp.domain.model.FuelType
import com.bpo.gasapp.ui.settings.SettingsViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    var fuel by remember { mutableStateOf(FuelType.GASOLINA_95) }

    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("⛽", style = MaterialTheme.typography.displayLarge)
        Text(
            "Bienvenido a GasApp",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Encuentra la gasolinera más barata de España en tiempo real.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        Text(
            "¿Qué combustible usas?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FuelType.entries.forEach { f ->
                FilterChip(
                    selected = f == fuel,
                    onClick = { fuel = f },
                    label = { Text(f.label) }
                )
            }
        }

        Button(
            onClick = { viewModel.completeOnboarding(fuel) },
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp)
        ) {
            Text("Empezar")
        }
    }
}

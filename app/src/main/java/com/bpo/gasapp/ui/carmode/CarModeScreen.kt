package com.bpo.gasapp.ui.carmode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bpo.gasapp.domain.model.FuelType
import com.bpo.gasapp.domain.model.Station

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
        },
        // Banner inferior fijo, oculto automáticamente para usuarios premium.
        bottomBar = { com.bpo.gasapp.ui.ads.BannerAd() }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            val list = state.cheapestList
            when {
                list.isEmpty() -> {
                    EmptyState(hasLocation = state.hasLocation)
                }
                else -> {
                    val pagerState = rememberPagerState(pageCount = { list.size })

                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        StationPage(
                            station = list[page],
                            fuel = state.fuel,
                            rank = page,
                            onNavigate = {
                                com.bpo.gasapp.ui.components.openNavigation(
                                    context,
                                    list[page].latitude,
                                    list[page].longitude
                                )
                            }
                        )
                    }

                    // Indicador de páginas (puntos a la derecha).
                    if (list.size > 1) {
                        PageIndicator(
                            count = list.size,
                            current = pagerState.currentPage,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 12.dp)
                        )
                        // Pista visual la primera vez: "desliza para ver la siguiente".
                        if (pagerState.currentPage == 0) {
                            ScrollHint(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StationPage(
    station: Station,
    fuel: FuelType,
    rank: Int,
    onNavigate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = rankLabel(rank) + " · " + fuel.label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            station.brand,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = station.priceOf(fuel)?.let { "%.3f".format(it) } ?: "—",
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
            onClick = onNavigate,
            modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
        ) {
            Icon(Icons.Default.Navigation, contentDescription = null)
            Text("  Ir allí", style = MaterialTheme.typography.titleLarge)
        }
    }
}

private fun rankLabel(rank: Int): String = when (rank) {
    0 -> "MÁS BARATA"
    1 -> "2ª MÁS BARATA"
    2 -> "3ª MÁS BARATA"
    else -> "${rank + 1}ª MÁS BARATA"
}

@Composable
private fun PageIndicator(count: Int, current: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(count) { i ->
            Box(
                modifier = Modifier
                    .size(if (i == current) 10.dp else 7.dp)
                    .clip(CircleShape)
                    .background(
                        if (i == current) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
}

@Composable
private fun ScrollHint(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            "Desliza para ver más",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Icon(
            Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun EmptyState(hasLocation: Boolean) {
    Box(
        Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (hasLocation) "Buscando gasolineras..." else "Activa la ubicación",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
    }
}

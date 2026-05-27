package com.bpo.gasapp.ui.carmode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpo.gasapp.data.location.LocationProvider
import com.bpo.gasapp.data.settings.SettingsRepository
import com.bpo.gasapp.domain.model.FuelType
import com.bpo.gasapp.domain.model.Station
import com.bpo.gasapp.domain.model.UserLocation
import com.bpo.gasapp.domain.repository.StationRepository
import com.bpo.gasapp.domain.util.distanceMeters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CarModeUiState(
    val cheapest: Station? = null,
    val fuel: FuelType = FuelType.GASOLINA_95,
    val hasLocation: Boolean = false
)

@HiltViewModel
class CarModeViewModel @Inject constructor(
    private val repository: StationRepository,
    private val locationProvider: LocationProvider,
    settingsRepository: SettingsRepository
) : ViewModel() {

    private val fuel = MutableStateFlow(FuelType.GASOLINA_95)
    private val location = MutableStateFlow<UserLocation?>(null)

    init {
        viewModelScope.launch { fuel.value = settingsRepository.settings.first().defaultFuel }
        refreshLocation()
    }

    val uiState: StateFlow<CarModeUiState> =
        combine(repository.observeStations(), location, fuel) { stations, loc, f ->
            val cheapest = if (loc == null) {
                stations.minByOrNull { it.priceOf(f) ?: Double.MAX_VALUE }
            } else {
                stations
                    .mapNotNull { s -> s.priceOf(f)?.let { s.copy(distanceMeters = distanceMeters(loc, s.latitude, s.longitude)) } }
                    .filter { (it.distanceMeters ?: Float.MAX_VALUE) <= 15_000f }
                    .minByOrNull { it.priceOf(f) ?: Double.MAX_VALUE }
            }?.takeIf { it.priceOf(f) != null }
            CarModeUiState(cheapest = cheapest, fuel = f, hasLocation = loc != null)
        }.flowOn(Dispatchers.Default).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CarModeUiState()
        )

    fun refreshLocation() {
        viewModelScope.launch { location.value = locationProvider.currentLocation() }
    }
}

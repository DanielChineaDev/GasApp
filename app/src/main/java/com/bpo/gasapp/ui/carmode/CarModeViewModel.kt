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
    /** Top-N gasolineras más baratas cerca, ordenadas por precio asc. */
    val cheapestList: List<Station> = emptyList(),
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
            val list: List<Station> = if (loc == null) {
                stations
                    .filter { it.priceOf(f) != null }
                    .sortedBy { it.priceOf(f) }
                    .take(MAX_CARD_COUNT)
            } else {
                stations
                    .mapNotNull { s ->
                        s.priceOf(f)?.let {
                            s.copy(distanceMeters = distanceMeters(loc, s.latitude, s.longitude))
                        }
                    }
                    .filter { (it.distanceMeters ?: Float.MAX_VALUE) <= MAX_RADIUS_METERS }
                    .sortedBy { it.priceOf(f) }
                    .take(MAX_CARD_COUNT)
            }
            CarModeUiState(cheapestList = list, fuel = f, hasLocation = loc != null)
        }.flowOn(Dispatchers.Default).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CarModeUiState()
        )

    fun refreshLocation() {
        viewModelScope.launch { location.value = locationProvider.currentLocation() }
    }

    private companion object {
        const val MAX_RADIUS_METERS = 15_000f
        const val MAX_CARD_COUNT = 10
    }
}

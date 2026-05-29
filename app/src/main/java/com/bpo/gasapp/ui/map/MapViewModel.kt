package com.bpo.gasapp.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpo.gasapp.data.location.LocationProvider
import com.bpo.gasapp.data.settings.SettingsRepository
import com.bpo.gasapp.domain.model.FuelType
import com.bpo.gasapp.domain.model.Station
import com.bpo.gasapp.domain.model.StationFilters
import com.bpo.gasapp.domain.model.UserLocation
import com.bpo.gasapp.domain.repository.StationRepository
import com.bpo.gasapp.domain.util.ScheduleParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Geographic bounding box the map is currently showing. */
data class MapRegion(
    val minLat: Double,
    val maxLat: Double,
    val minLng: Double,
    val maxLng: Double
)

data class MapUiState(
    val stations: List<Station> = emptyList(),
    val filters: StationFilters = StationFilters(maxDistanceKm = null),
    val userLocation: UserLocation? = null,
    /** True when results were capped, so the UI can hint the user to zoom in. */
    val truncated: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class MapViewModel @Inject constructor(
    private val repository: StationRepository,
    private val locationProvider: LocationProvider,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // The map does not apply a distance limit (the visible region already bounds
    // results); other constraints (price, brand, open, favorites) still apply.
    private val filters = MutableStateFlow(StationFilters(maxDistanceKm = null))
    private val userLocation = MutableStateFlow<UserLocation?>(null)
    private val region = MutableStateFlow<MapRegion?>(null)

    /**
     * Stations inside the visible region, capped to [MAX_MARKERS]. Switching
     * region cancels the previous query (flatMapLatest) and the value is
     * debounced so panning/zooming doesn't spawn a query per frame.
     */
    private val stationsInRegion: Flow<List<Station>> =
        region
            .debounce(REGION_DEBOUNCE_MS)
            .distinctUntilChanged()
            .flatMapLatest { r ->
                if (r == null) flowOf(emptyList())
                else repository.observeStationsInBounds(
                    minLat = r.minLat,
                    maxLat = r.maxLat,
                    minLng = r.minLng,
                    maxLng = r.maxLng,
                    limit = MAX_MARKERS
                )
            }

    val uiState: StateFlow<MapUiState> =
        combine(stationsInRegion, filters, userLocation) { stations, f, loc ->
            val filtered = applyFilters(stations, f)
            MapUiState(
                stations = filtered,
                filters = f,
                userLocation = loc,
                truncated = stations.size >= MAX_MARKERS
            )
        }.flowOn(Dispatchers.Default).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MapUiState()
        )

    init {
        viewModelScope.launch {
            filters.value = filters.value.copy(fuel = settingsRepository.settings.first().defaultFuel)
        }
        refreshLocation()
    }

    private fun applyFilters(stations: List<Station>, f: StationFilters): List<Station> =
        stations.filter { station ->
            val brandOk = f.brands.isEmpty() || station.brand.trim().titleCase() in f.brands
            val openOk = !f.openNowOnly || ScheduleParser.isOpen(station.schedule) != false
            val favOk = !f.onlyFavorites || station.isFavorite
            val price = station.priceOf(f.fuel)
            val priceOk = f.maxPrice == null || (price != null && price <= f.maxPrice)
            brandOk && openOk && favOk && priceOk
        }

    /** Called by the UI when the visible map region settles. */
    fun setVisibleRegion(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double) {
        region.value = MapRegion(minLat, maxLat, minLng, maxLng)
    }

    /** Clears the region (e.g. zoomed too far out) so no markers are loaded. */
    fun clearRegion() {
        region.value = null
    }

    fun selectFuel(fuel: FuelType) {
        filters.value = filters.value.copy(fuel = fuel)
    }

    fun updateFilters(newFilters: StationFilters) {
        // Distance limit is irrelevant on the map; keep it null.
        filters.value = newFilters.copy(maxDistanceKm = null)
    }

    fun refreshLocation() {
        viewModelScope.launch { userLocation.value = locationProvider.currentLocation() }
    }

    fun toggleFavorite(stationId: String) {
        viewModelScope.launch { repository.toggleFavorite(stationId) }
    }

    private companion object {
        /** Hard cap on markers loaded at once to protect memory and rendering. */
        const val MAX_MARKERS = 250
        const val REGION_DEBOUNCE_MS = 250L
    }
}

/** "REPSOL S.A." -> "Repsol S.A." to match the normalized brand filter set. */
private fun String.titleCase(): String =
    lowercase().split(' ').joinToString(" ") { word ->
        word.replaceFirstChar { c -> if (c.isLetter()) c.uppercaseChar() else c }
    }

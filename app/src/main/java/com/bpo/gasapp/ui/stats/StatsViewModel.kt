package com.bpo.gasapp.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpo.gasapp.data.settings.SettingsRepository
import com.bpo.gasapp.domain.model.MonthlyStat
import com.bpo.gasapp.domain.model.Refuel
import com.bpo.gasapp.domain.repository.RefuelRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class StatsUiState(
    val months: List<MonthlyStat> = emptyList(),
    val refuels: List<Refuel> = emptyList(),
    val avgConsumption: Double? = null,
    val avgCostPerKm: Double? = null
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: RefuelRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    val uiState: StateFlow<StatsUiState> =
        combine(
            repository.observeRefuels(),
            settingsRepository.settings
        ) { allRefuels, settings ->
            val selectedId = settings.selectedVehicleId
            val refuels = if (selectedId == null) allRefuels
            else allRefuels.filter { it.vehicleId == selectedId }
            val months = refuels
                .groupBy { monthKeyFormat.format(Date(it.timestamp)) }
                .map { (month, list) ->
                    MonthlyStat(
                        month = month,
                        totalAmount = list.sumOf { it.amount },
                        totalLiters = list.sumOf { it.liters },
                        count = list.size
                    )
                }
                .sortedByDescending { it.month }

            // Consumption: full-to-full between consecutive refuels with odometer.
            val ordered = refuels.filter { it.odometer != null }.sortedBy { it.odometer }
            val consumptions = mutableListOf<Double>()
            val costsPerKm = mutableListOf<Double>()
            for (i in 1 until ordered.size) {
                val km = (ordered[i].odometer!! - ordered[i - 1].odometer!!)
                if (km > 0) {
                    consumptions += ordered[i].liters / km * 100.0
                    costsPerKm += ordered[i].amount / km
                }
            }

            StatsUiState(
                months = months,
                refuels = refuels,
                avgConsumption = consumptions.takeIf { it.isNotEmpty() }?.average(),
                avgCostPerKm = costsPerKm.takeIf { it.isNotEmpty() }?.average()
            )
        }.flowOn(Dispatchers.Default).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsUiState()
        )

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }
}

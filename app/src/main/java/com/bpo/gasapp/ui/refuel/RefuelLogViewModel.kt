package com.bpo.gasapp.ui.refuel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpo.gasapp.data.settings.SettingsRepository
import com.bpo.gasapp.domain.model.FuelType
import com.bpo.gasapp.domain.repository.RefuelRepository
import com.bpo.gasapp.domain.util.TicketParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RefuelLogUiState(
    val stationName: String = "",
    val fuel: FuelType = FuelType.GASOLINA_95,
    val liters: String = "",
    val amount: String = "",
    val odometer: String = "",
    val isProcessingPhoto: Boolean = false,
    val photoMessage: String? = null,
    val saved: Boolean = false
) {
    val pricePerLiter: Double?
        get() {
            val l = liters.replace(',', '.').toDoubleOrNull()
            val a = amount.replace(',', '.').toDoubleOrNull()
            return if (l != null && a != null && l > 0) a / l else null
        }
    val canSave: Boolean
        get() = liters.replace(',', '.').toDoubleOrNull()?.let { it > 0 } == true &&
            amount.replace(',', '.').toDoubleOrNull()?.let { it > 0 } == true
}

@HiltViewModel
class RefuelLogViewModel @Inject constructor(
    private val refuelRepository: RefuelRepository,
    private val settingsRepository: SettingsRepository,
    private val vehicleRepository: com.bpo.gasapp.domain.repository.VehicleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val stationId: String? =
        savedStateHandle.get<String>(RefuelLogRoute.ARG_STATION_ID)?.takeIf { it.isNotBlank() }
    private var vehicleId: Long? = null

    private val _uiState = MutableStateFlow(RefuelLogUiState())
    val uiState: StateFlow<RefuelLogUiState> = _uiState.asStateFlow()

    init {
        val name = savedStateHandle.get<String>(RefuelLogRoute.ARG_STATION_NAME).orEmpty()
        val fuelArg = savedStateHandle.get<String>(RefuelLogRoute.ARG_FUEL)
            ?.let { runCatching { FuelType.valueOf(it) }.getOrNull() }
        _uiState.value = _uiState.value.copy(stationName = name)
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            val vehicle = settings.selectedVehicleId?.let { vehicleRepository.getById(it) }
            vehicleId = vehicle?.id
            val fuel = fuelArg ?: vehicle?.fuel ?: settings.defaultFuel
            _uiState.value = _uiState.value.copy(fuel = fuel)
        }
    }

    fun setStationName(v: String) { _uiState.value = _uiState.value.copy(stationName = v) }
    fun setFuel(v: FuelType) { _uiState.value = _uiState.value.copy(fuel = v) }
    fun setLiters(v: String) { _uiState.value = _uiState.value.copy(liters = v) }
    fun setAmount(v: String) { _uiState.value = _uiState.value.copy(amount = v) }
    fun setOdometer(v: String) { _uiState.value = _uiState.value.copy(odometer = v) }

    fun setProcessingPhoto(processing: Boolean) {
        _uiState.value = _uiState.value.copy(isProcessingPhoto = processing, photoMessage = null)
    }

    /** Fills liters/amount from the OCR text of a ticket or pump photo. */
    fun applyOcrText(text: String) {
        val result = TicketParser.parse(text)
        val state = _uiState.value
        val message = when {
            result.liters == null && result.amount == null ->
                "No se reconocieron datos. Introdúcelos a mano."
            result.liters == null -> "Solo se detectó el importe. Revisa los litros."
            result.amount == null -> "Solo se detectaron los litros. Revisa el importe."
            else -> "Datos detectados. Revísalos antes de guardar."
        }
        _uiState.value = state.copy(
            liters = result.liters?.let { "%.2f".format(it) } ?: state.liters,
            amount = result.amount?.let { "%.2f".format(it) } ?: state.amount,
            isProcessingPhoto = false,
            photoMessage = message
        )
    }

    fun onOcrFailed() {
        _uiState.value = _uiState.value.copy(
            isProcessingPhoto = false,
            photoMessage = "No se pudo leer la imagen. Inténtalo de nuevo o introdúcelo a mano."
        )
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        viewModelScope.launch {
            refuelRepository.add(
                stationId = stationId,
                stationName = state.stationName.ifBlank { "Repostaje" },
                fuel = state.fuel,
                liters = state.liters.replace(',', '.').toDouble(),
                amount = state.amount.replace(',', '.').toDouble(),
                odometer = state.odometer.replace(',', '.').toDoubleOrNull(),
                vehicleId = vehicleId
            )
            _uiState.value = state.copy(saved = true)
        }
    }
}

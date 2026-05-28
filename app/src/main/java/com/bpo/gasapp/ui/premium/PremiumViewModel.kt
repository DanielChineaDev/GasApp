package com.bpo.gasapp.ui.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpo.gasapp.data.billing.BillingRepository
import com.bpo.gasapp.data.remote.PromoCodeRemoteDataSource
import com.bpo.gasapp.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PremiumUiState(
    val isPremium: Boolean = false,
    val priceLabel: String? = null,
    val available: Boolean = false
)

data class PromoUiState(
    val isRedeeming: Boolean = false,
    val message: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
    private val promoRemote: PromoCodeRemoteDataSource,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    init { billingRepository.refresh() }

    val uiState: StateFlow<PremiumUiState> = kotlinx.coroutines.flow.combine(
        settingsRepository.settings.map { it.isPremium },
        billingRepository.productDetails
    ) { isPremium, details ->
        val offer = details?.oneTimePurchaseOfferDetails
        PremiumUiState(
            isPremium = isPremium,
            priceLabel = offer?.formattedPrice,
            available = details != null
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PremiumUiState())

    private val _promoState = MutableStateFlow(PromoUiState())
    val promoState: StateFlow<PromoUiState> = _promoState.asStateFlow()

    fun buy(activity: Activity) {
        billingRepository.launchPurchase(activity)
    }

    fun redeemCode(code: String) {
        viewModelScope.launch {
            _promoState.value = PromoUiState(isRedeeming = true)
            val result = promoRemote.redeem(code)
            _promoState.value = when (result) {
                PromoCodeRemoteDataSource.Result.Success -> {
                    settingsRepository.setPremium(true)
                    PromoUiState(message = "¡Listo! Anuncios eliminados.", success = true)
                }
                PromoCodeRemoteDataSource.Result.NotLoggedIn ->
                    PromoUiState(message = "Inicia sesión para canjear códigos.")
                PromoCodeRemoteDataSource.Result.InvalidCode ->
                    PromoUiState(message = "El código no es válido.")
                PromoCodeRemoteDataSource.Result.AlreadyUsed ->
                    PromoUiState(message = "Este código ya se ha canjeado.")
                is PromoCodeRemoteDataSource.Result.Error ->
                    PromoUiState(message = "No se pudo canjear. Revisa tu conexión.")
            }
        }
    }

    fun clearPromoMessage() {
        _promoState.value = _promoState.value.copy(message = null)
    }
}

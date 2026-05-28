package com.bpo.gasapp.ui.achievements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpo.gasapp.data.settings.SettingsRepository
import com.bpo.gasapp.domain.model.Achievement
import com.bpo.gasapp.domain.model.FuelType
import com.bpo.gasapp.domain.repository.RefuelRepository
import com.bpo.gasapp.domain.repository.StationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

data class AchievementsUiState(
    val moneySaved: Double = 0.0,
    val refuelCount: Int = 0,
    val favoritesCount: Int = 0,
    val achievements: List<Achievement> = emptyList()
)

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    refuelRepository: RefuelRepository,
    stationRepository: StationRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<AchievementsUiState> =
        combine(
            refuelRepository.observeRefuels(),
            stationRepository.observeStations(),
            stationRepository.observeFavorites(),
            settingsRepository.settings
        ) { refuels, stations, favorites, settings ->
            val avgByFuel = FuelType.entries.associateWith { fuel ->
                val prices = stations.mapNotNull { it.priceOf(fuel) }
                if (prices.isNotEmpty()) prices.average() else null
            }
            val saved = refuels.sumOf { r ->
                val avg = avgByFuel[r.fuel]
                val price = r.pricePerLiter
                if (avg != null && price != null && avg > price) (avg - price) * r.liters else 0.0
            }
            val refuelCount = refuels.size
            val favCount = favorites.size
            val totalLiters = refuels.sumOf { it.liters }
            val uniqueStations = refuels.mapNotNull { it.stationId }.toSet().size
            val uniqueFuels = refuels.map { it.fuel }.toSet().size
            val maxSingleLiters = refuels.maxOfOrNull { it.liters } ?: 0.0
            val minPricePerLiter = refuels.mapNotNull { it.pricePerLiter }.minOrNull() ?: Double.MAX_VALUE

            val hasNightRefuel = refuels.any { r ->
                val cal = Calendar.getInstance().apply { timeInMillis = r.timestamp }
                val hour = cal.get(Calendar.HOUR_OF_DAY)
                hour >= 22 || hour < 6
            }
            val hasWeekendRefuel = refuels.any { r ->
                val cal = Calendar.getInstance().apply { timeInMillis = r.timestamp }
                val dow = cal.get(Calendar.DAY_OF_WEEK)
                dow == Calendar.SATURDAY || dow == Calendar.SUNDAY
            }

            AchievementsUiState(
                moneySaved = saved,
                refuelCount = refuelCount,
                favoritesCount = favCount,
                achievements = buildAchievements(
                    saved = saved,
                    refuelCount = refuelCount,
                    favCount = favCount,
                    totalLiters = totalLiters,
                    uniqueStations = uniqueStations,
                    uniqueFuels = uniqueFuels,
                    maxSingleLiters = maxSingleLiters,
                    minPricePerLiter = minPricePerLiter,
                    hasNightRefuel = hasNightRefuel,
                    hasWeekendRefuel = hasWeekendRefuel,
                    isPremium = settings.isPremium
                )
            )
        }.flowOn(Dispatchers.Default).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AchievementsUiState()
        )

    private fun buildAchievements(
        saved: Double,
        refuelCount: Int,
        favCount: Int,
        totalLiters: Double,
        uniqueStations: Int,
        uniqueFuels: Int,
        maxSingleLiters: Double,
        minPricePerLiter: Double,
        hasNightRefuel: Boolean,
        hasWeekendRefuel: Boolean,
        isPremium: Boolean
    ): List<Achievement> {
        fun progress(value: Number, goal: Number): Float =
            (value.toDouble() / goal.toDouble()).coerceIn(0.0, 1.0).toFloat()

        return listOf(
            // ── Primeros pasos ──────────────────────────────────────────────
            Achievement(
                id = "first_refuel",
                emoji = "⛽",
                title = "Primer repostaje",
                description = "Registra tu primer repostaje.",
                unlocked = refuelCount >= 1,
                progress = progress(refuelCount, 1)
            ),
            Achievement(
                id = "fav_first",
                emoji = "💚",
                title = "Primera favorita",
                description = "Guarda tu primera gasolinera favorita.",
                unlocked = favCount >= 1,
                progress = progress(favCount, 1)
            ),
            Achievement(
                id = "saver_first",
                emoji = "🪙",
                title = "Primer ahorro",
                description = "Ahorra tu primer euro repostando por debajo de la media.",
                unlocked = saved >= 1,
                progress = progress(saved, 1)
            ),

            // ── Repostajes ──────────────────────────────────────────────────
            Achievement(
                id = "refuel_5",
                emoji = "🔄",
                title = "Tomando el ritmo",
                description = "Registra 5 repostajes.",
                unlocked = refuelCount >= 5,
                progress = progress(refuelCount, 5)
            ),
            Achievement(
                id = "regular",
                emoji = "🔁",
                title = "Repostador habitual",
                description = "Registra 10 repostajes.",
                unlocked = refuelCount >= 10,
                progress = progress(refuelCount, 10)
            ),
            Achievement(
                id = "refuel_25",
                emoji = "📍",
                title = "Siempre en marcha",
                description = "Registra 25 repostajes.",
                unlocked = refuelCount >= 25,
                progress = progress(refuelCount, 25)
            ),
            Achievement(
                id = "refuel_50",
                emoji = "🚗",
                title = "Gran conductor",
                description = "Registra 50 repostajes.",
                unlocked = refuelCount >= 50,
                progress = progress(refuelCount, 50)
            ),
            Achievement(
                id = "refuel_100",
                emoji = "🏎️",
                title = "Veterano de la carretera",
                description = "Registra 100 repostajes.",
                unlocked = refuelCount >= 100,
                progress = progress(refuelCount, 100)
            ),

            // ── Litros ──────────────────────────────────────────────────────
            Achievement(
                id = "liters_100",
                emoji = "💧",
                title = "100 litros registrados",
                description = "Registra un total de 100 litros de combustible.",
                unlocked = totalLiters >= 100,
                progress = progress(totalLiters, 100)
            ),
            Achievement(
                id = "liters_500",
                emoji = "🌊",
                title = "500 litros registrados",
                description = "Registra un total de 500 litros de combustible.",
                unlocked = totalLiters >= 500,
                progress = progress(totalLiters, 500)
            ),
            Achievement(
                id = "liters_1000",
                emoji = "🌊🌊",
                title = "¡Mil litros!",
                description = "Registra un total de 1000 litros de combustible.",
                unlocked = totalLiters >= 1000,
                progress = progress(totalLiters, 1000)
            ),
            Achievement(
                id = "big_tank",
                emoji = "🛢️",
                title = "Depósito lleno",
                description = "Repostaje de más de 50 litros de una sola vez.",
                unlocked = maxSingleLiters >= 50,
                progress = progress(maxSingleLiters, 50)
            ),

            // ── Ahorro ──────────────────────────────────────────────────────
            Achievement(
                id = "saver_5",
                emoji = "💵",
                title = "Ahorrador principiante",
                description = "Ahorra 5 € repostando por debajo de la media.",
                unlocked = saved >= 5,
                progress = progress(saved, 5)
            ),
            Achievement(
                id = "saver_10",
                emoji = "💶",
                title = "Ahorrador",
                description = "Ahorra 10 € usando la app.",
                unlocked = saved >= 10,
                progress = progress(saved, 10)
            ),
            Achievement(
                id = "saver_50",
                emoji = "💰",
                title = "Gran ahorrador",
                description = "Ahorra 50 € usando la app.",
                unlocked = saved >= 50,
                progress = progress(saved, 50)
            ),
            Achievement(
                id = "saver_100",
                emoji = "🏆",
                title = "Maestro del ahorro",
                description = "Ahorra 100 € usando la app.",
                unlocked = saved >= 100,
                progress = progress(saved, 100)
            ),
            Achievement(
                id = "saver_200",
                emoji = "💎",
                title = "Leyenda del ahorro",
                description = "Ahorra 200 € usando la app.",
                unlocked = saved >= 200,
                progress = progress(saved, 200)
            ),
            Achievement(
                id = "saver_500",
                emoji = "👑",
                title = "Élite del ahorro",
                description = "Ahorra 500 € usando la app. ¡Increíble!",
                unlocked = saved >= 500,
                progress = progress(saved, 500)
            ),
            Achievement(
                id = "bargain_hunter",
                emoji = "🎯",
                title = "Cazagangas",
                description = "Repostaje por debajo de 1.45 €/L.",
                unlocked = minPricePerLiter < 1.45,
                progress = if (minPricePerLiter < 1.45) 1f else 0f
            ),

            // ── Favoritas ──────────────────────────────────────────────────
            Achievement(
                id = "collector",
                emoji = "💚",
                title = "Coleccionista",
                description = "Guarda 5 gasolineras favoritas.",
                unlocked = favCount >= 5,
                progress = progress(favCount, 5)
            ),
            Achievement(
                id = "fav_10",
                emoji = "💙",
                title = "Gran coleccionista",
                description = "Guarda 10 gasolineras favoritas.",
                unlocked = favCount >= 10,
                progress = progress(favCount, 10)
            ),

            // ── Explorador ──────────────────────────────────────────────────
            Achievement(
                id = "explorer",
                emoji = "🗺️",
                title = "Explorador",
                description = "Repostaje en 5 gasolineras distintas.",
                unlocked = uniqueStations >= 5,
                progress = progress(uniqueStations, 5)
            ),
            Achievement(
                id = "globetrotter",
                emoji = "🌍",
                title = "Trotamundos",
                description = "Repostaje en 10 gasolineras distintas.",
                unlocked = uniqueStations >= 10,
                progress = progress(uniqueStations, 10)
            ),
            Achievement(
                id = "multi_fuel",
                emoji = "⚗️",
                title = "Multifuel",
                description = "Usa 2 tipos de combustible distintos.",
                unlocked = uniqueFuels >= 2,
                progress = progress(uniqueFuels, 2)
            ),

            // ── Momentos especiales ─────────────────────────────────────────
            Achievement(
                id = "night_owl",
                emoji = "🦉",
                title = "Búho nocturno",
                description = "Repostaje entre las 22:00 y las 6:00.",
                unlocked = hasNightRefuel,
                progress = if (hasNightRefuel) 1f else 0f
            ),
            Achievement(
                id = "weekend",
                emoji = "🎉",
                title = "Fin de semana",
                description = "Repostaje en sábado o domingo.",
                unlocked = hasWeekendRefuel,
                progress = if (hasWeekendRefuel) 1f else 0f
            ),

            // ── Apoyo ──────────────────────────────────────────────────────
            Achievement(
                id = "supporter",
                emoji = "⭐",
                title = "Mecenas",
                description = "Apoya el proyecto quitando los anuncios.",
                unlocked = isPremium,
                progress = if (isPremium) 1f else 0f
            )
        )
    }
}

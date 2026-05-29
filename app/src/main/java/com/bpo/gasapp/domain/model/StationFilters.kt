package com.bpo.gasapp.domain.model

enum class SortMode(val label: String) {
    PRICE("Precio"),
    DISTANCE("Distancia"),
    VALUE("Valor"),
    SAVING("Ahorro")
}

data class StationFilters(
    val fuel: FuelType = FuelType.GASOLINA_95,
    /** Max distance in km. Null = no distance limit. Requires user location. */
    val maxDistanceKm: Int? = 5,
    /** Max price per liter. Null = no price limit. */
    val maxPrice: Double? = null,
    /** Empty = all brands. */
    val brands: Set<String> = emptySet(),
    val openNowOnly: Boolean = false,
    /** Show only stations the user has marked as favorite. */
    val onlyFavorites: Boolean = false,
    val sortMode: SortMode = SortMode.PRICE
) {
    /** Whether any non-default constraint is active (drives the "filters on" badge). */
    val hasActiveConstraints: Boolean
        get() = maxPrice != null || brands.isNotEmpty() || openNowOnly || onlyFavorites

    companion object {
        val DISTANCE_OPTIONS = listOf(1, 5, 10, 25)
        const val DISTANCE_MIN_KM = 1
        const val DISTANCE_MAX_KM = 30
        const val PRICE_MIN = 1.0
        const val PRICE_MAX = 2.5
    }
}

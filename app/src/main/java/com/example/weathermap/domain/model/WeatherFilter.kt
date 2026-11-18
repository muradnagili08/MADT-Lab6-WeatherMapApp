package com.example.weathermap.domain.model

enum class TemperatureRange {
    ALL,
    WARM, // > 15°C
    MILD, // 0°C to 15°C
    COLD  // < 0°C
}

data class WeatherFilter(
    val searchQuery: String = "",
    val selectedCondition: WeatherCondition? = null,
    val temperatureRange: TemperatureRange = TemperatureRange.ALL
) {
    fun matches(station: WeatherStation): Boolean {
        // 1. Search Query filter (matches station name or administrative division or code)
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.trim().lowercase()
            val matchesName = station.name.lowercase().contains(query)
            val matchesDivision = station.administrativeDivision.lowercase().contains(query)
            val matchesCode = station.code.lowercase().contains(query)
            if (!matchesName && !matchesDivision && !matchesCode) {
                return false
            }
        }

        val current = station.currentWeather ?: return false

        // 2. Weather Condition filter
        if (selectedCondition != null && current.condition != selectedCondition) {
            return false
        }

        // 3. Temperature Range filter
        val temp = current.airTemperature
        val matchesTemp = when (temperatureRange) {
            TemperatureRange.ALL -> true
            TemperatureRange.WARM -> temp > 15.0
            TemperatureRange.MILD -> temp in 0.0..15.0
            TemperatureRange.COLD -> temp < 0.0
        }

        return matchesTemp
    }

    val isActive: Boolean
        get() = searchQuery.isNotBlank() || selectedCondition != null || temperatureRange != TemperatureRange.ALL
}

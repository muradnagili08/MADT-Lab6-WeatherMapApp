package com.example.weathermap.presentation.map

import com.example.weathermap.domain.model.WeatherFilter
import com.example.weathermap.domain.model.WeatherStation

data class MapUiState(
    val stations: List<WeatherStation> = emptyList(),
    val filteredStations: List<WeatherStation> = emptyList(),
    val selectedStation: WeatherStation? = null,
    val filter: WeatherFilter = WeatherFilter(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val errorMessageRes: Int? = null,
    val isAuthError: Boolean = false,
    val isMapsKeyConfigured: Boolean = true
)

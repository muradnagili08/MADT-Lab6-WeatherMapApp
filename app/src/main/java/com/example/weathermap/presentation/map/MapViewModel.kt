package com.example.weathermap.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathermap.BuildConfig
import com.example.weathermap.R
import com.example.weathermap.domain.model.TemperatureRange
import com.example.weathermap.domain.model.WeatherCondition
import com.example.weathermap.domain.model.WeatherFilter
import com.example.weathermap.domain.model.WeatherStation
import com.example.weathermap.domain.repository.AuthRepository
import com.example.weathermap.domain.repository.WeatherRepository
import com.example.weathermap.domain.util.AuthRequiredException
import com.example.weathermap.domain.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    private val weatherRepository: WeatherRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        checkMapsApiKey()
        loadWeatherData()
    }

    private fun checkMapsApiKey() {
        val key = BuildConfig.MAPS_API_KEY
        val isConfigured = key.isNotBlank() && key != "YOUR_MAPS_API_KEY_HERE"
        _uiState.update { it.copy(isMapsKeyConfigured = isConfigured) }
    }

    fun loadWeatherData(forceRefresh: Boolean = false) {
        if (!authRepository.isUserAuthenticated) {
            _uiState.update {
                it.copy(
                    isAuthError = true,
                    errorMessageRes = R.string.error_auth_required,
                    isLoading = false
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    errorMessageRes = null,
                    isAuthError = false
                )
            }

            when (val resource = weatherRepository.getMonitoredStations(forceRefresh = forceRefresh)) {
                is Resource.Success -> {
                    val stations = resource.data
                    _uiState.update { current ->
                        current.copy(
                            stations = stations,
                            filteredStations = applyFilters(stations, current.filter),
                            isLoading = false
                        )
                    }
                }
                is Resource.Error -> {
                    val isAuth = resource.cause is AuthRequiredException
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            errorMessage = resource.message,
                            errorMessageRes = if (isAuth) R.string.error_auth_required else null,
                            isAuthError = isAuth
                        )
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            val updatedFilter = current.filter.copy(searchQuery = query)
            current.copy(
                filter = updatedFilter,
                filteredStations = applyFilters(current.stations, updatedFilter)
            )
        }
    }

    fun onConditionFilterSelected(condition: WeatherCondition?) {
        _uiState.update { current ->
            val newCondition = if (current.filter.selectedCondition == condition) null else condition
            val updatedFilter = current.filter.copy(selectedCondition = newCondition)
            current.copy(
                filter = updatedFilter,
                filteredStations = applyFilters(current.stations, updatedFilter)
            )
        }
    }

    fun onTemperatureRangeSelected(range: TemperatureRange) {
        _uiState.update { current ->
            val newRange = if (current.filter.temperatureRange == range) TemperatureRange.ALL else range
            val updatedFilter = current.filter.copy(temperatureRange = newRange)
            current.copy(
                filter = updatedFilter,
                filteredStations = applyFilters(current.stations, updatedFilter)
            )
        }
    }

    fun onResetFilters() {
        _uiState.update { current ->
            val resetFilter = WeatherFilter()
            current.copy(
                filter = resetFilter,
                filteredStations = applyFilters(current.stations, resetFilter)
            )
        }
    }

    fun onStationSelected(station: WeatherStation?) {
        _uiState.update { it.copy(selectedStation = station) }
    }

    fun onDismissStationDetails() {
        _uiState.update { it.copy(selectedStation = null) }
    }

    private fun applyFilters(
        stations: List<WeatherStation>,
        filter: WeatherFilter
    ): List<WeatherStation> {
        return stations.filter { filter.matches(it) }
    }
}

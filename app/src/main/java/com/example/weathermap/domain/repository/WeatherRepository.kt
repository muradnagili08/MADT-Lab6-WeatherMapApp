package com.example.weathermap.domain.repository

import com.example.weathermap.domain.model.WeatherStation
import com.example.weathermap.domain.util.Resource

interface WeatherRepository {
    /**
     * Fetches monitored weather stations with their current forecasts from Meteo.lt.
     * Note: Must strictly verify that the user is authenticated before dispatching the request.
     */
    suspend fun getMonitoredStations(forceRefresh: Boolean = false): Resource<List<WeatherStation>>

    /**
     * Fetches full multi-day hourly forecast for a specific place from Meteo.lt.
     * Note: Must strictly verify that the user is authenticated before dispatching the request.
     */
    suspend fun getStationForecast(placeCode: String): Resource<WeatherStation>

    /**
     * Retrieves currently cached stations without initiating a network request.
     */
    fun getCachedStations(): List<WeatherStation>

    /**
     * Clears local in-memory weather cache (e.g., upon user logout).
     */
    fun clearCache()
}

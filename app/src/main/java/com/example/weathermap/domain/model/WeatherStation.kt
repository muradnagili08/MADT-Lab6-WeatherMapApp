package com.example.weathermap.domain.model

data class WeatherStation(
    val code: String,
    val name: String,
    val administrativeDivision: String,
    val latitude: Double,
    val longitude: Double,
    val currentWeather: WeatherReport?,
    val hourlyForecast: List<WeatherReport> = emptyList()
)

package com.example.weathermap.domain.model

data class WeatherReport(
    val forecastTimeUtc: String,
    val airTemperature: Double,
    val feelsLikeTemperature: Double,
    val windSpeed: Double,
    val windGust: Double,
    val cloudCover: Int,
    val seaLevelPressure: Int,
    val relativeHumidity: Int,
    val totalPrecipitation: Double,
    val condition: WeatherCondition,
    val rawConditionCode: String
)

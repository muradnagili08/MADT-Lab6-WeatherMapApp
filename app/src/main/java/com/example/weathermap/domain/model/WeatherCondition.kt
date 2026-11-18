package com.example.weathermap.domain.model

enum class WeatherCondition {
    CLEAR,
    CLOUDY,
    RAIN,
    SNOW,
    FOG,
    UNKNOWN;

    companion object {
        fun fromMeteoCode(code: String?): WeatherCondition {
            return when (code?.lowercase()) {
                "clear" -> CLEAR
                "isolated-clouds", "scattered-clouds", "overcast" -> CLOUDY
                "light-rain", "moderate-rain", "heavy-rain" -> RAIN
                "sleet", "light-snow", "moderate-snow", "heavy-snow" -> SNOW
                "fog" -> FOG
                else -> UNKNOWN
            }
        }
    }
}

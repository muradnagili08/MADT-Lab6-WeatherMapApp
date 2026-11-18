package com.example.weathermap.data.api.dto

import com.google.gson.annotations.SerializedName

data class ForecastResponseDto(
    @SerializedName("place")
    val place: PlaceDto?,
    @SerializedName("forecastType")
    val forecastType: String?,
    @SerializedName("forecastCreationTimeUtc")
    val forecastCreationTimeUtc: String?,
    @SerializedName("forecastTimestamps")
    val forecastTimestamps: List<ForecastTimestampDto>?
)

data class ForecastTimestampDto(
    @SerializedName("forecastTimeUtc")
    val forecastTimeUtc: String?,
    @SerializedName("airTemperature")
    val airTemperature: Double?,
    @SerializedName("feelsLikeTemperature")
    val feelsLikeTemperature: Double?,
    @SerializedName("windSpeed")
    val windSpeed: Double?,
    @SerializedName("windGust")
    val windGust: Double?,
    @SerializedName("windDirection")
    val windDirection: Int?,
    @SerializedName("cloudCover")
    val cloudCover: Int?,
    @SerializedName("seaLevelPressure")
    val seaLevelPressure: Int?,
    @SerializedName("relativeHumidity")
    val relativeHumidity: Int?,
    @SerializedName("totalPrecipitation")
    val totalPrecipitation: Double?,
    @SerializedName("conditionCode")
    val conditionCode: String?
)

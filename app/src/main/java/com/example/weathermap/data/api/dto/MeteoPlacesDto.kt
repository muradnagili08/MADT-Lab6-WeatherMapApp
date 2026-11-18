package com.example.weathermap.data.api.dto

import com.google.gson.annotations.SerializedName

data class CoordinatesDto(
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?
) {
    fun isValid(): Boolean {
        val lat = latitude ?: return false
        val lng = longitude ?: return false
        return lat in -90.0..90.0 && lng in -180.0..180.0
    }
}

data class PlaceDto(
    @SerializedName("code")
    val code: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("administrativeDivision")
    val administrativeDivision: String?,
    @SerializedName("countryCode")
    val countryCode: String?,
    @SerializedName("coordinates")
    val coordinates: CoordinatesDto?
)

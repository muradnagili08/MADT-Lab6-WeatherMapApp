package com.example.weathermap.data.api

import com.example.weathermap.data.api.dto.ForecastResponseDto
import com.example.weathermap.data.api.dto.PlaceDto
import retrofit2.http.GET
import retrofit2.http.Path

interface MeteoApiService {

    @GET("v1/places")
    suspend fun getPlaces(): List<PlaceDto>

    @GET("v1/places/{placeCode}/forecasts/long-term")
    suspend fun getPlaceForecast(
        @Path("placeCode") placeCode: String
    ): ForecastResponseDto

    companion object {
        const val BASE_URL = "https://api.meteo.lt/"
    }
}

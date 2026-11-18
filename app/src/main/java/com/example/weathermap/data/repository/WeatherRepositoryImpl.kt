package com.example.weathermap.data.repository

import com.example.weathermap.data.api.MeteoApiService
import com.example.weathermap.data.api.dto.ForecastResponseDto
import com.example.weathermap.data.api.dto.ForecastTimestampDto
import com.example.weathermap.domain.model.WeatherCondition
import com.example.weathermap.domain.model.WeatherReport
import com.example.weathermap.domain.model.WeatherStation
import com.example.weathermap.domain.repository.AuthRepository
import com.example.weathermap.domain.repository.WeatherRepository
import com.example.weathermap.domain.util.AuthRequiredException
import com.example.weathermap.domain.util.Resource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.io.IOException

class WeatherRepositoryImpl(
    private val apiService: MeteoApiService,
    private val authRepository: AuthRepository
) : WeatherRepository {

    // Default major Lithuanian cities monitored on the map
    private val defaultPlaceCodes = listOf(
        "vilnius",
        "kaunas",
        "klaipeda",
        "siauliai",
        "panevezys",
        "alytus",
        "marijampole",
        "mazeikiai",
        "utena",
        "taurage",
        "telsiai",
        "palanga",
        "nida",
        "druskininkai",
        "anyksciai"
    )

    // Known fallback coordinates for Lithuanian centers in case API omits them
    private val fallbackCoordinates = mapOf(
        "vilnius" to Pair(54.6872, 25.2797),
        "kaunas" to Pair(54.8985, 23.9036),
        "klaipeda" to Pair(55.7033, 21.1443),
        "siauliai" to Pair(55.9349, 23.3137),
        "panevezys" to Pair(55.7348, 24.3575),
        "alytus" to Pair(54.3963, 24.0414),
        "marijampole" to Pair(54.5599, 23.3541),
        "mazeikiai" to Pair(56.3134, 22.3418),
        "utena" to Pair(55.4984, 25.6021),
        "taurage" to Pair(55.2522, 22.2897),
        "telsiai" to Pair(55.9814, 22.2473),
        "palanga" to Pair(55.9175, 21.0686),
        "nida" to Pair(55.3087, 21.0044),
        "druskininkai" to Pair(54.0167, 23.9667),
        "anyksciai" to Pair(55.5261, 25.1028)
    )

    // Thread-safe in-memory cache
    private val cache = mutableMapOf<String, WeatherStation>()

    /**
     * Requirement 3: Explicit authentication guard verified before ANY API interaction.
     */
    private fun verifyAuthentication() {
        if (!authRepository.isUserAuthenticated) {
            throw AuthRequiredException("Operation blocked: User must be authenticated to send requests to Meteo.lt API.")
        }
    }

    override suspend fun getMonitoredStations(forceRefresh: Boolean): Resource<List<WeatherStation>> =
        withContext(Dispatchers.IO) {
            try {
                // Strict authentication check immediately before sending requests
                verifyAuthentication()

                val cachedList = synchronized(cache) {
                    if (!forceRefresh && cache.isNotEmpty()) cache.values.toList() else null
                }
                if (cachedList != null) {
                    return@withContext Resource.Success(cachedList)
                }

                var networkException: IOException? = null

                // Fetch monitored stations in parallel
                val deferredList = defaultPlaceCodes.map { placeCode ->
                    async {
                        try {
                            val response = apiService.getPlaceForecast(placeCode)
                            mapToWeatherStation(placeCode, response)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: IOException) {
                            networkException = e
                            null
                        } catch (e: Exception) {
                            null
                        }
                    }
                }

                val results = deferredList.awaitAll().filterNotNull()

                if (results.isEmpty()) {
                    val netEx = networkException
                    return@withContext if (netEx != null) {
                        Resource.Error("Network failure connecting to Meteo.lt: ${netEx.localizedMessage}", netEx)
                    } else {
                        Resource.Error("Empty weather response received from Meteo.lt")
                    }
                }

                // Prevent post-logout cache race condition: verify user is still authenticated before persisting
                if (!authRepository.isUserAuthenticated) {
                    return@withContext Resource.Error("Operation cancelled: User signed out.")
                }

                synchronized(cache) {
                    cache.clear()
                    results.forEach { cache[it.code] = it }
                }

                Resource.Success(results)
            } catch (e: AuthRequiredException) {
                Resource.Error(e.message ?: "Authentication required", e)
            } catch (e: IOException) {
                Resource.Error("Network failure connecting to Meteo.lt: ${e.localizedMessage}", e)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Resource.Error("Failed to fetch weather data: ${e.localizedMessage ?: "Unknown error"}", e)
            }
        }

    override suspend fun getStationForecast(placeCode: String): Resource<WeatherStation> =
        withContext(Dispatchers.IO) {
            try {
                // Strict authentication check immediately before sending request
                verifyAuthentication()

                val response = apiService.getPlaceForecast(placeCode)
                val station = mapToWeatherStation(placeCode, response)
                    ?: return@withContext Resource.Error("Station '$placeCode' could not be found or has invalid coordinates.")

                if (!authRepository.isUserAuthenticated) {
                    throw AuthRequiredException("Operation blocked: User must be authenticated to save weather data.")
                }

                synchronized(cache) {
                    cache[station.code] = station
                }

                Resource.Success(station)
            } catch (e: AuthRequiredException) {
                Resource.Error(e.message ?: "Authentication required", e)
            } catch (e: IOException) {
                Resource.Error("Network failure connecting to Meteo.lt: ${e.localizedMessage}", e)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Resource.Error("Failed to load forecast: ${e.localizedMessage}", e)
            }
        }

    private suspend fun fetchSingleStation(placeCode: String): WeatherStation? {
        return try {
            val response = apiService.getPlaceForecast(placeCode)
            mapToWeatherStation(placeCode, response)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Requirement 2: Strict coordinate verification for map markers.
     */
    fun mapToWeatherStation(placeCode: String, response: ForecastResponseDto): WeatherStation? {
        val place = response.place ?: return null
        val code = place.code ?: placeCode
        val name = place.name ?: placeCode.replaceFirstChar { it.uppercase() }
        val admin = place.administrativeDivision ?: "Lietuva"

        // Verify latitude and longitude
        val apiCoords = place.coordinates
        val lat: Double
        val lng: Double

        if (apiCoords != null && apiCoords.isValid()) {
            lat = apiCoords.latitude!!
            lng = apiCoords.longitude!!
        } else {
            // Coordinate fallback for known Lithuanian locations if API omits them
            val fallback = fallbackCoordinates[code.lowercase()] ?: return null
            lat = fallback.first
            lng = fallback.second
        }

        // Validate final coordinates boundary: Lithuania sits roughly between Lat 53.8-56.5, Lng 20.9-26.9
        if (lat !in 53.0..57.0 || lng !in 20.0..27.5) {
            return null
        }

        val timestamps = response.forecastTimestamps.orEmpty().mapNotNull { it.toWeatherReport() }
        val current = timestamps.firstOrNull()

        return WeatherStation(
            code = code,
            name = name,
            administrativeDivision = admin,
            latitude = lat,
            longitude = lng,
            currentWeather = current,
            hourlyForecast = timestamps
        )
    }

    private fun ForecastTimestampDto.toWeatherReport(): WeatherReport? {
        val time = forecastTimeUtc ?: return null
        val temp = airTemperature ?: 0.0
        val feelsLike = feelsLikeTemperature ?: temp
        val wind = windSpeed ?: 0.0
        val gust = windGust ?: wind
        val clouds = cloudCover ?: 0
        val pressure = seaLevelPressure ?: 1013
        val humidity = relativeHumidity ?: 50
        val precip = totalPrecipitation ?: 0.0
        val cond = WeatherCondition.fromMeteoCode(conditionCode)

        return WeatherReport(
            forecastTimeUtc = time,
            airTemperature = temp,
            feelsLikeTemperature = feelsLike,
            windSpeed = wind,
            windGust = gust,
            cloudCover = clouds,
            seaLevelPressure = pressure,
            relativeHumidity = humidity,
            totalPrecipitation = precip,
            condition = cond,
            rawConditionCode = conditionCode ?: "unknown"
        )
    }

    override fun getCachedStations(): List<WeatherStation> {
        return synchronized(cache) { cache.values.toList() }
    }

    override fun clearCache() {
        synchronized(cache) { cache.clear() }
    }
}

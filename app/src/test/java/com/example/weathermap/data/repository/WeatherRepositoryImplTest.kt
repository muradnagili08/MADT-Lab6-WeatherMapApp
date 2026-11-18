package com.example.weathermap.data.repository

import com.example.weathermap.data.api.dto.CoordinatesDto
import com.example.weathermap.data.api.dto.ForecastResponseDto
import com.example.weathermap.data.api.dto.ForecastTimestampDto
import com.example.weathermap.data.api.dto.PlaceDto
import com.example.weathermap.domain.model.WeatherCondition
import com.example.weathermap.domain.repository.AuthRepository
import com.example.weathermap.domain.util.AuthRequiredException
import com.example.weathermap.domain.util.Resource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherRepositoryImplTest {

    // ==============================
    //  Fakes / Test Doubles
    // ==============================

    private class FakeAuthRepository(private val authenticated: Boolean) : AuthRepository {
        override val currentUser get() = null
        override val isUserAuthenticated: Boolean get() = authenticated
        override val authState get() = kotlinx.coroutines.flow.flowOf(null)
        override suspend fun signIn(email: String, password: String) = Result.failure<com.example.weathermap.domain.model.User>(Exception())
        override suspend fun register(email: String, password: String) = Result.failure<com.example.weathermap.domain.model.User>(Exception())
        override fun signOut() {}
    }

    private class FakeMeteoApiService(
        private val shouldThrow: Boolean = false,
        private val hasValidCoords: Boolean = true,
        private val hasTimestamps: Boolean = true
    ) : com.example.weathermap.data.api.MeteoApiService {

        override suspend fun getPlaces(): List<PlaceDto> = emptyList()

        override suspend fun getPlaceForecast(placeCode: String): ForecastResponseDto {
            if (shouldThrow) throw java.io.IOException("Simulated network failure")
            return ForecastResponseDto(
                place = PlaceDto(
                    code = placeCode,
                    name = "Vilnius",
                    administrativeDivision = "Vilniaus apskritis",
                    countryCode = "LT",
                    coordinates = if (hasValidCoords) {
                        CoordinatesDto(latitude = 54.6872, longitude = 25.2797)
                    } else {
                        CoordinatesDto(latitude = null, longitude = null)
                    }
                ),
                forecastType = "long-term",
                forecastCreationTimeUtc = "2024-01-01 00:00:00",
                forecastTimestamps = if (hasTimestamps) {
                    listOf(
                        ForecastTimestampDto(
                            forecastTimeUtc = "2024-01-01 12:00:00",
                            airTemperature = 5.5,
                            feelsLikeTemperature = 3.0,
                            windSpeed = 4.0,
                            windGust = 7.0,
                            windDirection = 180,
                            cloudCover = 60,
                            seaLevelPressure = 1010,
                            relativeHumidity = 75,
                            totalPrecipitation = 0.5,
                            conditionCode = "light-rain"
                        )
                    )
                } else emptyList()
            )
        }
    }

    private lateinit var authenticatedAuth: FakeAuthRepository
    private lateinit var unauthenticatedAuth: FakeAuthRepository

    @Before
    fun setUp() {
        authenticatedAuth = FakeAuthRepository(authenticated = true)
        unauthenticatedAuth = FakeAuthRepository(authenticated = false)
    }

    // ==============================
    //  Authentication Guard Tests
    // ==============================

    @Test
    fun `getMonitoredStations returns auth error when user is not authenticated`() = runTest {
        val repo = WeatherRepositoryImpl(FakeMeteoApiService(), unauthenticatedAuth)
        val result = repo.getMonitoredStations()
        assertTrue(result is Resource.Error)
        val error = result as Resource.Error
        assertTrue(error.cause is AuthRequiredException)
    }

    @Test
    fun `getStationForecast returns auth error when user is not authenticated`() = runTest {
        val repo = WeatherRepositoryImpl(FakeMeteoApiService(), unauthenticatedAuth)
        val result = repo.getStationForecast("vilnius")
        assertTrue(result is Resource.Error)
        val error = result as Resource.Error
        assertTrue(error.cause is AuthRequiredException)
    }

    @Test
    fun `getMonitoredStations proceeds when user is authenticated`() = runTest {
        val repo = WeatherRepositoryImpl(FakeMeteoApiService(), authenticatedAuth)
        val result = repo.getMonitoredStations()
        // Authenticated so API call should proceed (some stations may resolve)
        assertTrue(result is Resource.Success || result is Resource.Error)
        // Confirm it is NOT an auth error
        if (result is Resource.Error) {
            assertTrue("Auth error unexpected", result.cause !is AuthRequiredException)
        }
    }

    // ==============================
    //  Coordinate Validation Tests
    // ==============================

    @Test
    fun `mapToWeatherStation returns station with valid API coordinates`() {
        val repo = WeatherRepositoryImpl(FakeMeteoApiService(hasValidCoords = true), authenticatedAuth)
        val response = ForecastResponseDto(
            place = PlaceDto(
                code = "vilnius",
                name = "Vilnius",
                administrativeDivision = "Vilniaus apskritis",
                countryCode = "LT",
                coordinates = CoordinatesDto(latitude = 54.6872, longitude = 25.2797)
            ),
            forecastType = "long-term",
            forecastCreationTimeUtc = "2024-01-01 00:00:00",
            forecastTimestamps = emptyList()
        )
        val station = repo.mapToWeatherStation("vilnius", response)
        assertNotNull(station)
        assertEquals(54.6872, station!!.latitude, 0.001)
        assertEquals(25.2797, station.longitude, 0.001)
    }

    @Test
    fun `mapToWeatherStation uses fallback coordinates when API provides null lat-lng`() {
        val repo = WeatherRepositoryImpl(FakeMeteoApiService(hasValidCoords = false), authenticatedAuth)
        val response = ForecastResponseDto(
            place = PlaceDto(
                code = "vilnius",
                name = "Vilnius",
                administrativeDivision = "Vilniaus apskritis",
                countryCode = "LT",
                coordinates = CoordinatesDto(latitude = null, longitude = null)
            ),
            forecastType = "long-term",
            forecastCreationTimeUtc = "2024-01-01 00:00:00",
            forecastTimestamps = emptyList()
        )
        val station = repo.mapToWeatherStation("vilnius", response)
        assertNotNull(station)
        // Expects fallback Vilnius coords: 54.6872, 25.2797
        assertEquals(54.6872, station!!.latitude, 0.001)
        assertEquals(25.2797, station.longitude, 0.001)
    }

    @Test
    fun `mapToWeatherStation returns null for unknown code with no fallback and no valid coordinates`() {
        val repo = WeatherRepositoryImpl(FakeMeteoApiService(hasValidCoords = false), authenticatedAuth)
        val response = ForecastResponseDto(
            place = PlaceDto(
                code = "unknown-place-xyz",
                name = "Unknown",
                administrativeDivision = "Unknown",
                countryCode = "LT",
                coordinates = CoordinatesDto(latitude = null, longitude = null)
            ),
            forecastType = "long-term",
            forecastCreationTimeUtc = "2024-01-01 00:00:00",
            forecastTimestamps = emptyList()
        )
        val station = repo.mapToWeatherStation("unknown-place-xyz", response)
        assertNull(station)
    }

    // ==============================
    //  Weather Condition Mapping Tests
    // ==============================

    @Test
    fun `mapToWeatherStation correctly maps light-rain to RAIN condition`() {
        val repo = WeatherRepositoryImpl(FakeMeteoApiService(), authenticatedAuth)
        val response = ForecastResponseDto(
            place = PlaceDto(
                code = "vilnius",
                name = "Vilnius",
                administrativeDivision = "Vilniaus apskritis",
                countryCode = "LT",
                coordinates = CoordinatesDto(latitude = 54.6872, longitude = 25.2797)
            ),
            forecastType = "long-term",
            forecastCreationTimeUtc = "2024-01-01 00:00:00",
            forecastTimestamps = listOf(
                ForecastTimestampDto(
                    forecastTimeUtc = "2024-01-01 12:00:00",
                    airTemperature = 3.0,
                    feelsLikeTemperature = 1.0,
                    windSpeed = 3.0,
                    windGust = 5.0,
                    windDirection = 90,
                    cloudCover = 90,
                    seaLevelPressure = 1005,
                    relativeHumidity = 95,
                    totalPrecipitation = 2.5,
                    conditionCode = "light-rain"
                )
            )
        )
        val station = repo.mapToWeatherStation("vilnius", response)
        assertNotNull(station)
        assertEquals(WeatherCondition.RAIN, station!!.currentWeather?.condition)
    }

    // ==============================
    //  Network Failure Tests
    // ==============================

    @Test
    fun `getStationForecast returns error on network failure when authenticated`() = runTest {
        val repo = WeatherRepositoryImpl(FakeMeteoApiService(shouldThrow = true), authenticatedAuth)
        val result = repo.getStationForecast("vilnius")
        // The fallback null result wraps into a Resource.Error
        assertTrue(result is Resource.Error)
        val error = result as Resource.Error
        // Should NOT be auth error
        assertTrue(error.cause !is AuthRequiredException)
        assertTrue(error.cause is java.io.IOException)
    }

    @Test
    fun `getMonitoredStations returns network error cause on simulated IOException`() = runTest {
        val repo = WeatherRepositoryImpl(FakeMeteoApiService(shouldThrow = true), authenticatedAuth)
        val result = repo.getMonitoredStations()
        assertTrue(result is Resource.Error)
        val error = result as Resource.Error
        assertTrue(error.cause is java.io.IOException)
    }

    // ==============================
    //  Lithuania Boundary Validation Tests
    // ==============================

    @Test
    fun `mapToWeatherStation rejects coordinates outside Lithuania boundary`() {
        val repo = WeatherRepositoryImpl(FakeMeteoApiService(), authenticatedAuth)
        val responseOutside = ForecastResponseDto(
            place = PlaceDto(
                code = "foreign-city",
                name = "Foreign City",
                administrativeDivision = "Abroad",
                countryCode = "XX",
                coordinates = CoordinatesDto(latitude = 10.0, longitude = 10.0) // Far outside Lithuania (Lat ~53-57, Lng ~20-27.5)
            ),
            forecastType = "long-term",
            forecastCreationTimeUtc = "2024-01-01 00:00:00",
            forecastTimestamps = emptyList()
        )
        val station = repo.mapToWeatherStation("foreign-city", responseOutside)
        assertNull(station)
    }

    // ==============================
    //  Cache Tests
    // ==============================

    @Test
    fun `getCachedStations returns empty when no data has been fetched`() {
        val repo = WeatherRepositoryImpl(FakeMeteoApiService(), authenticatedAuth)
        val cached = repo.getCachedStations()
        assertTrue(cached.isEmpty())
    }

    @Test
    fun `clearCache empties the station cache`() = runTest {
        val repo = WeatherRepositoryImpl(FakeMeteoApiService(), authenticatedAuth)
        repo.getStationForecast("vilnius")
        repo.clearCache()
        assertTrue(repo.getCachedStations().isEmpty())
    }

    @Test
    fun `getMonitoredStations does not repopulate cache if user logs out before completion`() = runTest {
        var authStatus = true
        val dynamicAuth = object : AuthRepository {
            override val currentUser get() = null
            override val isUserAuthenticated get() = authStatus
            override val authState get() = kotlinx.coroutines.flow.flowOf(null)
            override suspend fun signIn(e: String, p: String) = Result.failure<com.example.weathermap.domain.model.User>(Exception())
            override suspend fun register(e: String, p: String) = Result.failure<com.example.weathermap.domain.model.User>(Exception())
            override fun signOut() { authStatus = false }
        }

        val delayedApiService = object : com.example.weathermap.data.api.MeteoApiService {
            override suspend fun getPlaces() = emptyList<PlaceDto>()
            override suspend fun getPlaceForecast(placeCode: String): ForecastResponseDto {
                // Simulate user logging out mid-flight
                authStatus = false
                return ForecastResponseDto(
                    place = PlaceDto(
                        code = placeCode,
                        name = "Vilnius",
                        administrativeDivision = "Vilniaus apskritis",
                        countryCode = "LT",
                        coordinates = CoordinatesDto(latitude = 54.6872, longitude = 25.2797)
                    ),
                    forecastType = "long-term",
                    forecastCreationTimeUtc = "2024-01-01 00:00:00",
                    forecastTimestamps = emptyList()
                )
            }
        }

        val repo = WeatherRepositoryImpl(delayedApiService, dynamicAuth)
        val result = repo.getMonitoredStations()

        // Cache must remain empty because user logged out
        assertTrue(repo.getCachedStations().isEmpty())
        assertTrue(result is Resource.Error)
    }
}

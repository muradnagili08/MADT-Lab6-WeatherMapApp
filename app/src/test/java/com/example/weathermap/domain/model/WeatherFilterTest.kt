package com.example.weathermap.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherFilterTest {

    private fun makeStation(
        name: String,
        code: String = name.lowercase(),
        adminDivision: String = "Lietuva",
        lat: Double = 55.0,
        lng: Double = 25.0,
        tempC: Double = 10.0,
        condition: WeatherCondition = WeatherCondition.CLEAR
    ): WeatherStation {
        val report = WeatherReport(
            forecastTimeUtc = "2024-01-01 12:00:00",
            airTemperature = tempC,
            feelsLikeTemperature = tempC - 2,
            windSpeed = 3.5,
            windGust = 5.0,
            cloudCover = 20,
            seaLevelPressure = 1012,
            relativeHumidity = 70,
            totalPrecipitation = 0.0,
            condition = condition,
            rawConditionCode = "clear"
        )
        return WeatherStation(
            code = code,
            name = name,
            administrativeDivision = adminDivision,
            latitude = lat,
            longitude = lng,
            currentWeather = report,
            hourlyForecast = listOf(report)
        )
    }

    // ==============================
    //  Empty / Default Filter Tests
    // ==============================

    @Test
    fun `default filter matches all stations`() {
        val filter = WeatherFilter()
        val station = makeStation("Vilnius")
        assertTrue(filter.matches(station))
    }

    @Test
    fun `default filter isActive is false`() {
        assertFalse(WeatherFilter().isActive)
    }

    // ==============================
    //  Search Query Filter Tests
    // ==============================

    @Test
    fun `search by partial city name matches correctly`() {
        val filter = WeatherFilter(searchQuery = "vil")
        val vilnius = makeStation("Vilnius")
        val kaunas = makeStation("Kaunas")
        assertTrue(filter.matches(vilnius))
        assertFalse(filter.matches(kaunas))
    }

    @Test
    fun `search is case-insensitive`() {
        val filter = WeatherFilter(searchQuery = "KAUNAS")
        val kaunas = makeStation("Kaunas")
        assertTrue(filter.matches(kaunas))
    }

    @Test
    fun `search by administrative division matches`() {
        val filter = WeatherFilter(searchQuery = "Vilniaus")
        val station = makeStation("Nemenčinė", adminDivision = "Vilniaus apskritis")
        assertTrue(filter.matches(station))
    }

    @Test
    fun `blank search query matches all stations`() {
        val filter = WeatherFilter(searchQuery = "   ")
        val station = makeStation("Palanga")
        assertTrue(filter.matches(station))
    }

    @Test
    fun `search query with isActive true when non-blank`() {
        assertTrue(WeatherFilter(searchQuery = "v").isActive)
    }

    // ==============================
    //  Condition Filter Tests
    // ==============================

    @Test
    fun `condition filter matches correct condition`() {
        val filter = WeatherFilter(selectedCondition = WeatherCondition.RAIN)
        val rainyStation = makeStation("Klaipėda", condition = WeatherCondition.RAIN)
        val clearStation = makeStation("Vilnius", condition = WeatherCondition.CLEAR)
        assertTrue(filter.matches(rainyStation))
        assertFalse(filter.matches(clearStation))
    }

    @Test
    fun `null condition filter matches any condition`() {
        val filter = WeatherFilter(selectedCondition = null)
        val snowy = makeStation("Utena", condition = WeatherCondition.SNOW)
        val rainy = makeStation("Šiauliai", condition = WeatherCondition.RAIN)
        assertTrue(filter.matches(snowy))
        assertTrue(filter.matches(rainy))
    }

    // ==============================
    //  Temperature Range Filter Tests
    // ==============================

    @Test
    fun `WARM temperature range matches stations above 15C`() {
        val filter = WeatherFilter(temperatureRange = TemperatureRange.WARM)
        val warm = makeStation("Palanga", tempC = 20.0)
        val cold = makeStation("Nida", tempC = -5.0)
        assertTrue(filter.matches(warm))
        assertFalse(filter.matches(cold))
    }

    @Test
    fun `MILD temperature range matches stations in 0 to 15C`() {
        val filter = WeatherFilter(temperatureRange = TemperatureRange.MILD)
        val mild = makeStation("Alytus", tempC = 8.0)
        val warm = makeStation("Druskininkai", tempC = 18.0)
        val cold = makeStation("Utena", tempC = -3.0)
        assertTrue(filter.matches(mild))
        assertFalse(filter.matches(warm))
        assertFalse(filter.matches(cold))
    }

    @Test
    fun `COLD temperature range matches stations below 0C`() {
        val filter = WeatherFilter(temperatureRange = TemperatureRange.COLD)
        val freezing = makeStation("Zarasai", tempC = -10.0)
        val mild = makeStation("Kaunas", tempC = 5.0)
        assertTrue(filter.matches(freezing))
        assertFalse(filter.matches(mild))
    }

    @Test
    fun `ALL temperature range matches any temperature`() {
        val filter = WeatherFilter(temperatureRange = TemperatureRange.ALL)
        assertTrue(filter.matches(makeStation("Hot", tempC = 35.0)))
        assertTrue(filter.matches(makeStation("Cold", tempC = -25.0)))
    }

    // ==============================
    //  Combined Filter Tests
    // ==============================

    @Test
    fun `combined condition and temperature filter works correctly`() {
        val filter = WeatherFilter(
            selectedCondition = WeatherCondition.SNOW,
            temperatureRange = TemperatureRange.COLD
        )
        val correctStation = makeStation("Zarasai", tempC = -8.0, condition = WeatherCondition.SNOW)
        val wrongTemp = makeStation("Kaunas", tempC = 2.0, condition = WeatherCondition.SNOW)
        val wrongCondition = makeStation("Vilnius", tempC = -5.0, condition = WeatherCondition.CLEAR)

        assertTrue(filter.matches(correctStation))
        assertFalse(filter.matches(wrongTemp))
        assertFalse(filter.matches(wrongCondition))
    }

    @Test
    fun `combined filter isActive when any filter is set`() {
        assertTrue(WeatherFilter(selectedCondition = WeatherCondition.RAIN).isActive)
        assertTrue(WeatherFilter(temperatureRange = TemperatureRange.COLD).isActive)
    }

    @Test
    fun `station with null currentWeather does not match any active filter`() {
        val filter = WeatherFilter(selectedCondition = WeatherCondition.CLEAR)
        val station = WeatherStation(
            code = "test",
            name = "Test",
            administrativeDivision = "Lietuva",
            latitude = 55.0,
            longitude = 25.0,
            currentWeather = null
        )
        assertFalse(filter.matches(station))
    }
}

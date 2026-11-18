package com.example.weathermap.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherConditionTest {

    @Test
    fun `fromMeteoCode returns CLEAR for 'clear' code`() {
        assertEquals(WeatherCondition.CLEAR, WeatherCondition.fromMeteoCode("clear"))
    }

    @Test
    fun `fromMeteoCode returns CLOUDY for 'isolated-clouds'`() {
        assertEquals(WeatherCondition.CLOUDY, WeatherCondition.fromMeteoCode("isolated-clouds"))
    }

    @Test
    fun `fromMeteoCode returns CLOUDY for 'scattered-clouds'`() {
        assertEquals(WeatherCondition.CLOUDY, WeatherCondition.fromMeteoCode("scattered-clouds"))
    }

    @Test
    fun `fromMeteoCode returns CLOUDY for 'overcast'`() {
        assertEquals(WeatherCondition.CLOUDY, WeatherCondition.fromMeteoCode("overcast"))
    }

    @Test
    fun `fromMeteoCode returns RAIN for 'light-rain'`() {
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromMeteoCode("light-rain"))
    }

    @Test
    fun `fromMeteoCode returns RAIN for 'heavy-rain'`() {
        assertEquals(WeatherCondition.RAIN, WeatherCondition.fromMeteoCode("heavy-rain"))
    }

    @Test
    fun `fromMeteoCode returns SNOW for 'light-snow'`() {
        assertEquals(WeatherCondition.SNOW, WeatherCondition.fromMeteoCode("light-snow"))
    }

    @Test
    fun `fromMeteoCode returns SNOW for 'sleet'`() {
        assertEquals(WeatherCondition.SNOW, WeatherCondition.fromMeteoCode("sleet"))
    }

    @Test
    fun `fromMeteoCode returns FOG for 'fog'`() {
        assertEquals(WeatherCondition.FOG, WeatherCondition.fromMeteoCode("fog"))
    }

    @Test
    fun `fromMeteoCode returns UNKNOWN for null`() {
        assertEquals(WeatherCondition.UNKNOWN, WeatherCondition.fromMeteoCode(null))
    }

    @Test
    fun `fromMeteoCode returns UNKNOWN for unrecognized code`() {
        assertEquals(WeatherCondition.UNKNOWN, WeatherCondition.fromMeteoCode("hailstorm"))
    }

    @Test
    fun `fromMeteoCode handles uppercase input gracefully`() {
        // Should normalize via lowercase() internally
        assertEquals(WeatherCondition.CLEAR, WeatherCondition.fromMeteoCode("CLEAR"))
    }
}

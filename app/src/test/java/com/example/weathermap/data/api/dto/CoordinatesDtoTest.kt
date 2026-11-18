package com.example.weathermap.data.api.dto

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoordinatesDtoTest {

    @Test
    fun `isValid returns true for valid Lithuanian coordinates`() {
        val coords = CoordinatesDto(latitude = 54.6872, longitude = 25.2797)
        assertTrue(coords.isValid())
    }

    @Test
    fun `isValid returns false when latitude is null`() {
        val coords = CoordinatesDto(latitude = null, longitude = 25.2797)
        assertFalse(coords.isValid())
    }

    @Test
    fun `isValid returns false when longitude is null`() {
        val coords = CoordinatesDto(latitude = 54.6872, longitude = null)
        assertFalse(coords.isValid())
    }

    @Test
    fun `isValid returns false when both are null`() {
        val coords = CoordinatesDto(latitude = null, longitude = null)
        assertFalse(coords.isValid())
    }

    @Test
    fun `isValid returns false for out of range latitude`() {
        val coords = CoordinatesDto(latitude = 100.0, longitude = 25.0)
        assertFalse(coords.isValid())
    }

    @Test
    fun `isValid returns false for out of range longitude`() {
        val coords = CoordinatesDto(latitude = 54.0, longitude = 200.0)
        assertFalse(coords.isValid())
    }

    @Test
    fun `isValid returns true for edge case latitude -90`() {
        val coords = CoordinatesDto(latitude = -90.0, longitude = 0.0)
        assertTrue(coords.isValid())
    }

    @Test
    fun `isValid returns true for edge case longitude 180`() {
        val coords = CoordinatesDto(latitude = 0.0, longitude = 180.0)
        assertTrue(coords.isValid())
    }
}

package com.example.weathermap

import android.app.Application
import android.util.Log
import com.example.weathermap.di.AppContainer
import com.example.weathermap.di.DefaultAppContainer
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback

class WeatherMapApplication : Application(), OnMapsSdkInitializedCallback {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
        MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)
    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        when (renderer) {
            MapsInitializer.Renderer.LATEST -> Log.d("WeatherMapApp", "Google Maps SDK initialized with LATEST renderer.")
            MapsInitializer.Renderer.LEGACY -> Log.d("WeatherMapApp", "Google Maps SDK initialized with LEGACY renderer.")
        }
    }
}

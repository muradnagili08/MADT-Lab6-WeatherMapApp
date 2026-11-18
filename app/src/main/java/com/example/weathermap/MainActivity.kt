package com.example.weathermap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.weathermap.di.ViewModelFactory
import com.example.weathermap.presentation.navigation.NavGraph
import com.example.weathermap.presentation.theme.WeatherMapTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as WeatherMapApplication).container
        val viewModelFactory = ViewModelFactory(appContainer)

        setContent {
            WeatherMapTheme {
                NavGraph(viewModelFactory = viewModelFactory)
            }
        }
    }
}

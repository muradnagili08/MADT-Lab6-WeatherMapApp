package com.example.weathermap.di

import com.example.weathermap.data.api.MeteoApiService
import com.example.weathermap.data.repository.AuthRepositoryImpl
import com.example.weathermap.data.repository.WeatherRepositoryImpl
import com.example.weathermap.domain.repository.AuthRepository
import com.example.weathermap.domain.repository.WeatherRepository
import com.google.firebase.auth.FirebaseAuth
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

interface AppContainer {
    val authRepository: AuthRepository
    val weatherRepository: WeatherRepository
}

class DefaultAppContainer : AppContainer {

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val meteoApiService: MeteoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(MeteoApiService.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MeteoApiService::class.java)
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(FirebaseAuth.getInstance())
    }

    override val weatherRepository: WeatherRepository by lazy {
        WeatherRepositoryImpl(
            apiService = meteoApiService,
            authRepository = authRepository
        )
    }
}

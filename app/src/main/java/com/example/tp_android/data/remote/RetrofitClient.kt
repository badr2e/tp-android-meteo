package com.example.tp_android.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Client Retrofit pour les appels API
 */
object RetrofitClient {

    private const val TIMEOUT_SECONDS = 30L

    /**
     * OkHttpClient avec timeout et logging
     */
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        )
        .build()

    /**
     * Retrofit pour l'API Geocoding
     */
    private val geocodingRetrofit = Retrofit.Builder()
        .baseUrl(WeatherApi.GEOCODING_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * Retrofit pour l'API Météo
     */
    private val weatherRetrofit = Retrofit.Builder()
        .baseUrl(WeatherApi.WEATHER_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * Instance de l'API Geocoding
     */
    val geocodingApi: WeatherApi by lazy {
        geocodingRetrofit.create(WeatherApi::class.java)
    }

    /**
     * Instance de l'API Météo
     */
    val weatherApi: WeatherApi by lazy {
        weatherRetrofit.create(WeatherApi::class.java)
    }
}

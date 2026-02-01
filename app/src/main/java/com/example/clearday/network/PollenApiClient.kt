package com.example.clearday.network

import com.example.clearday.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton client for the Google Pollen API.
 * Provides lazy initialization for the PollenApiService and access to API credentials.
 */
object PollenApiClient {

    private const val BASE_URL = "https://pollen.googleapis.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val pollenApiService: PollenApiService by lazy {
        retrofit.create(PollenApiService::class.java)
    }

    fun getApiKey(): String = BuildConfig.POLLEN_API_KEY
}
package com.example.clearday.network


import com.example.clearday.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    private val logging = HttpLoggingInterceptor().apply() {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Optional: we could add API key as query param via interceptor,
    // but here we’ll pass it explicitly in @Query("appid")
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {
    // Primary: world.openfoodfacts.org (stable 200 OK)
    private const val PRIMARY_URL = "https://world.openfoodfacts.org/"
    // Fallback: fr.openfoodfacts.org
    private const val FALLBACK_URL = "https://fr.openfoodfacts.org/"

    private val userAgentInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestWithHeaders = originalRequest.newBuilder()
            .header("User-Agent", "JournalCalorieApp - Android - Version 1.0 (contact: moogli@free.fr)")
            .header("Accept", "application/json")
            .build()
        chain.proceed(requestWithHeaders)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(userAgentInterceptor)
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val api: OpenFoodFactsApi by lazy {
        Retrofit.Builder()
            .baseUrl(PRIMARY_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenFoodFactsApi::class.java)
    }

    val fallbackApi: OpenFoodFactsApi by lazy {
        Retrofit.Builder()
            .baseUrl(FALLBACK_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenFoodFactsApi::class.java)
    }
}

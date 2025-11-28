// RetrofitClient.kt
package com.example.salontenexapp.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://salonestenex.b-corpsolutions.com/APIMobil/"

    // Cookie manager mejorado
    private val cookieManager = mutableMapOf<String, String>()

    // Interceptor para guardar cookies de las respuestas
    private val responseInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())

        // Guardar todas las cookies de la respuesta
        val cookies = response.headers("Set-Cookie")
        cookies.forEach { cookie ->
            val parts = cookie.split(';').first().split('=')
            if (parts.size == 2) {
                cookieManager[parts[0]] = parts[1]
            }
        }
        response
    }

    // Interceptor para agregar cookies a las requests
    private val requestInterceptor = Interceptor { chain ->
        val original = chain.request()

        val requestBuilder = original.newBuilder()

        // Agregar todas las cookies guardadas
        if (cookieManager.isNotEmpty()) {
            val cookieHeader = cookieManager.entries.joinToString("; ") { "${it.key}=${it.value}" }
            requestBuilder.header("Cookie", cookieHeader)
        }

        // Agregar headers importantes
        requestBuilder
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")

        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(requestInterceptor)
        .addInterceptor(responseInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: APIService by lazy {
        retrofit.create(APIService::class.java)
    }

    // Método para limpiar cookies (logout)
    fun clearCookies() {
        cookieManager.clear()
    }

    // Método para verificar si hay sesión activa
    fun hasSession(): Boolean {
        return cookieManager.isNotEmpty()
    }
}
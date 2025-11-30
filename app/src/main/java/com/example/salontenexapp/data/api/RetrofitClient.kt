package com.example.salontenexapp.data.api
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Body

object RetrofitClient {
    private const val BASE_URL = "https://salonestenex.b-corpsolutions.com/APIMobil/"
    var sessionCookie: String? = null
        private set

    private var onSessionExpired: (() -> Unit)? = null
    private var onNewCookieReceived: ((String) -> Unit)? = null

    fun restoreSession(cookie: String?) {
        sessionCookie = cookie
    }

    private val cookieInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)

        // Guardar cualquier cookie que recibamos del servidor
        val cookies = response.headers.values("Set-Cookie")
        val session = cookies.firstOrNull { it.startsWith("PHPSESSID") }

        session?.let { cookie ->
            val cleanCookie = cookie.split(';').first()
            sessionCookie = cleanCookie
            onNewCookieReceived?.invoke(cleanCookie)
        }

        response
    }

    private val sessionExpiredInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 401) {
            sessionCookie = null
            onSessionExpired?.invoke()
        }
        response
    }

    fun setOnNewCookieListener(listener: (String) -> Unit) {
        onNewCookieReceived = listener
    }



    private val authInterceptor = Interceptor { chain ->
        var request = chain.request()

        // Primero intentar con la cookie de sesión activa
        sessionCookie?.let { cookie ->
            request = request.newBuilder()
                .header("Cookie", cookie)
                .build()
        }
        chain.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(cookieInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(sessionExpiredInterceptor)
        .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun setOnSessionExpiredListener(listener: () -> Unit) {
        onSessionExpired = listener
    }

    fun clearSession() {
        sessionCookie = null
    }

    val apiService: APIService by lazy {
        retrofit.create(APIService::class.java)
    }

    fun <T> getService(service: Class<T>): T {
        return retrofit.create(service)
    }
}
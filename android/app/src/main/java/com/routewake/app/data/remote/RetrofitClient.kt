package com.routewake.app.data.remote

import com.routewake.app.data.remote.api.NominatimApi
import com.routewake.app.data.remote.api.OsrmApi
import com.routewake.app.data.remote.api.RouteWakeApi
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // 10.0.2.2 connects from Android emulator to host machine localhost:8081
    // Can be changed dynamically via updateBaseUrl if running on physical device with LAN IP
    private var backendBaseUrl: String = "http://10.0.2.2:8081/"

    // In-memory cookie store so JSESSIONID is automatically attached to requests
    private val cookieStore = ConcurrentHashMap<String, MutableList<Cookie>>()

    private val sessionCookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host] = cookies.toMutableList()
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host] ?: emptyList()
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .cookieJar(sessionCookieJar)
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private var retrofitBackend: Retrofit = buildBackendRetrofit()

    private fun buildBackendRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(backendBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun updateBaseUrl(newUrl: String) {
        val formatted = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        backendBaseUrl = formatted
        retrofitBackend = buildBackendRetrofit()
        routeWakeApi = retrofitBackend.create(RouteWakeApi::class.java)
    }

    fun getBackendUrl(): String = backendBaseUrl

    var routeWakeApi: RouteWakeApi = retrofitBackend.create(RouteWakeApi::class.java)
        private set

    val nominatimApi: NominatimApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NominatimApi::class.java)
    }

    val osrmApi: OsrmApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://router.project-osrm.org/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OsrmApi::class.java)
    }

    fun clearCookies() {
        cookieStore.clear()
    }
}

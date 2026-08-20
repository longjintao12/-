package com.novabox.app.net

import com.novabox.app.util.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

object ApiClient {
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("User-Agent", Prefs.userAgent)
                    .apply {
                        val ref = Prefs.referer
                        if (ref.isNotBlank()) header("Referer", ref)
                        val cookie = Prefs.cookie
                        if (cookie.isNotBlank()) header("Cookie", cookie)
                    }
                    .build()
                chain.proceed(req)
            }
            .build()
    }

    suspend fun getString(url: String): String = withContext(Dispatchers.IO) {
        client.newCall(Request.Builder().url(url).build()).execute().use { resp ->
            if (!resp.isSuccessful) throw IOException("HTTP ${resp.code}")
            resp.body?.string() ?: ""
        }
    }

    fun getHttpClient(): OkHttpClient = client
}

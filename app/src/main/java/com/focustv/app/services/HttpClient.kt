package com.focustv.app.services

import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object HttpClient {
    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .build()

    fun request(url: String, userAgent: String = "FocusTV/1.0 AndroidTV"): Request {
        return Request.Builder()
            .url(url)
            .header("User-Agent", userAgent)
            .header("Accept", "*/*")
            .header("Connection", "keep-alive")
            .build()
    }
}

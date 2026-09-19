package com.example.core.network

import android.content.Context
import com.example.core.datastore.SessionManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: RetrofitClient? = null

        fun getInstance(context: Context): RetrofitClient {
            return INSTANCE ?: synchronized(this) {
                val instance = RetrofitClient(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val authInterceptor = Interceptor { chain ->
        val sessionManager = SessionManager.getInstance(context)
        val token = runBlocking { sessionManager.authToken.firstOrNull() }
        
        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        
        val response = chain.proceed(requestBuilder.build())
        if (response.code == 401) {
            runBlocking {
                sessionManager.clearSession()
            }
        }
        response
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Disable logging in release, enable in debug
        level = if (com.example.BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: ApiService by lazy {
        val baseUrl = com.example.BuildConfig.API_BASE_URL
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }
}

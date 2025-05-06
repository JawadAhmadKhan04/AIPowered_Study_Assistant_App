package com.musketeers_and_me.ai_powered_study_assistant_app.Services

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://us-central1-i220791.cloudfunctions.net/"
    private const val TAG = "RetrofitClient"

    val instance: FirebaseFunctionService by lazy {
        Log.d(TAG, "Initializing RetrofitClient with BASE_URL: $BASE_URL")

        // Create logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d(TAG, "OkHttp: $message")
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Create OkHttpClient with logging and timeout
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        Log.d(TAG, "Creating Retrofit instance with base URL: $BASE_URL")

        try {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FirebaseFunctionService::class.java)
                .also {
                    Log.d(TAG, "Retrofit instance created successfully")
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating Retrofit instance", e)
            Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Exception message: ${e.message}")
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
            throw e
        }
    }
}
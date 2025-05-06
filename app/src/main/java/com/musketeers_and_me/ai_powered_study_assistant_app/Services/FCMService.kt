package com.musketeers_and_me.ai_powered_study_assistant_app.Services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FCMService {
    fun sendNotification(token: String, dataPayload: Map<String, String>) {
        Log.d("FCMService", "Starting notification send process")
        Log.d("FCMService", "Token: ${token.take(10)}... (truncated)")
        Log.d("FCMService", "Data payload: $dataPayload")

        GlobalScope.launch(Dispatchers.IO) {
            try {
                Log.d("FCMService", "Creating notification request")
                // Create the request body
                val request = NotificationRequest(
                    to = token,
                    data = dataPayload
                )
                Log.d("FCMService", "Request created: $request")

                Log.d("FCMService", "Attempting to execute Retrofit call")
                // Call the Firebase Function
                val response = RetrofitClient.instance.sendNotification(request).execute()
                Log.d("FCMService", "Response received: ${response.code()} - ${response.message()}")

                if (response.isSuccessful) {
                    Log.d("FCMService", "Notification sent successfully via Firebase Function")
                    Log.d("FCMService", "Response body: ${response.body()}")
                } else {
                    Log.e("FCMService", "Failed to send notification via Firebase Function: ${response.code()} - ${response.message()}")
                    Log.e("FCMService", "Error body: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("FCMService", "Error sending notification", e)
                Log.e("FCMService", "Exception type: ${e.javaClass.simpleName}")
                Log.e("FCMService", "Exception message: ${e.message}")
                Log.e("FCMService", "Stack trace: ${e.stackTraceToString()}")
                // Handle network errors or other exceptions
            }
        }
    }
}
package com.musketeers_and_me.ai_powered_study_assistant_app.Services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FCMService {
    private val TAG = "FCMService"
    
    fun sendNotification(token: String, dataPayload: Map<String, String>) {
        Log.d(TAG, "Starting notification send process")
        Log.d(TAG, "Token: ${token.take(10)}... (truncated)")
        Log.d(TAG, "Data payload: $dataPayload")

        // Validate token
        if (token.isBlank()) {
            Log.e(TAG, "Cannot send notification: FCM token is empty")
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Creating notification request")
                // Create the request body
                val request = NotificationRequest(
                    to = token,
                    data = dataPayload
                )
                Log.d(TAG, "Request created: $request")

                // Call the Firebase Function using callback pattern for better error handling
                RetrofitClient.instance.sendNotification(request).enqueue(object : Callback<NotificationResponse> {
                    override fun onResponse(call: Call<NotificationResponse>, response: Response<NotificationResponse>) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            if (responseBody != null) {
                                Log.d(TAG, "Notification sent successfully: ${responseBody.success}")
                                Log.d(TAG, "Message ID: ${responseBody.messageId}")
                                Log.d(TAG, "Success count: ${responseBody.successCount}, Failure count: ${responseBody.failureCount}")
                                
                                if (responseBody.failureCount > 0) {
                                    Log.w(TAG, "Some messages failed to send")
                                }
                            } else {
                                Log.w(TAG, "Response successful but body is null")
                            }
                        } else {
                            val errorBody = response.errorBody()?.string() ?: "Unknown error"
                            Log.e(TAG, "Failed to send notification: ${response.code()} - ${response.message()}")
                            Log.e(TAG, "Error body: $errorBody")
                        }
                    }

                    override fun onFailure(call: Call<NotificationResponse>, t: Throwable) {
                        Log.e(TAG, "Network error when sending notification", t)
                        Log.e(TAG, "Error type: ${t.javaClass.simpleName}")
                        Log.e(TAG, "Error message: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "Exception when preparing notification request", e)
                Log.e(TAG, "Exception type: ${e.javaClass.simpleName}")
                Log.e(TAG, "Exception message: ${e.message}")
            }
        }
    }
}
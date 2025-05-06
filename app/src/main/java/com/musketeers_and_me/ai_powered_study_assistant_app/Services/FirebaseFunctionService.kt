package com.musketeers_and_me.ai_powered_study_assistant_app.Services


import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface FirebaseFunctionService {
    @POST("sendNotification")
    fun sendNotification(@Body request: NotificationRequest): Call<NotificationResponse>
}
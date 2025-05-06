package com.musketeers_and_me.ai_powered_study_assistant_app.Services


data class NotificationRequest(
    val to: String,
    val data: Map<String, String>
)
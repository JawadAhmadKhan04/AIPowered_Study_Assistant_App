package com.musketeers_and_me.ai_powered_study_assistant_app.Services

data class NotificationResponse(
    val success: Boolean,
    val messageId: String? = null,
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val error: String? = null,
    val errorCode: String? = null,
    val errorDetails: String? = null
)
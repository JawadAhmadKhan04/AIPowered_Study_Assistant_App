package com.musketeers_and_me.ai_powered_study_assistant_app.Services

import android.content.Context
import android.util.Log
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel

class OneSignalService(private val context: Context) {
    private val TAG = "OneSignalService"
    
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "study-smart"
    }

    init {
        // Enable verbose logging for debugging
        OneSignal.Debug.logLevel = LogLevel.VERBOSE
        
        // Initialize OneSignal with app ID
        OneSignal.initWithContext(context, "5b2a135d-53c4-4047-bea1-1b038ef697f5")
        
        Log.d(TAG, "OneSignal initialized successfully")
    }

    fun setExternalUserId(userId: String) {
        // Use login instead of setExternalUserId
        OneSignal.login(userId)
        Log.d(TAG, "Set external user ID: $userId")
    }

    fun logout() {
        OneSignal.logout()
        Log.d(TAG, "OneSignal logout successful")
    }
}
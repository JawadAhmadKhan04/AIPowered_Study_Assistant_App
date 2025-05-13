package com.musketeers_and_me.ai_powered_study_assistant_app.Services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
//import com.musketeers_and_me.ai_powered_study_assistant_app.Notification.NotificationActivity
//import com.musketeers_and_me.ai_powered_study_assistant_app.Chats.ChatActivity

class FirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "FirebaseMessagingService"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "FirebaseMessagingService onCreate")
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "New FCM token generated: $token")
        
        // Get current user ID
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        
        if (currentUserId != null) {
            Log.d(TAG, "Saving token for user: $currentUserId")
            
            // Save token to Firebase using the correct field name "FCMToken"
            FirebaseDatabase.getInstance().getReference("users")
                .child(currentUserId)
                .child("FCMToken")
                .setValue(token)
                .addOnSuccessListener {
                    Log.d(TAG, "Token saved successfully for user $currentUserId")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to save token for user $currentUserId", e)
                }
        } else {
            Log.d(TAG, "Current user ID is null, token will be saved when user logs in")
            // Store token locally for later use when user logs in
            // This could be done using SharedPreferences
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        Log.d(TAG, "Message data: ${remoteMessage.data}")

        val channelId = remoteMessage.data["channelId"] ?: "default_channel"
        Log.d(TAG, "Using channel ID: $channelId")

        // Check for group message notifications
        if (remoteMessage.data["notificationType"] == "GROUP_MESSAGE") {
            val groupId = remoteMessage.data["groupId"]
            val title = remoteMessage.data["title"] ?: "New Group Message"
            val body = remoteMessage.data["body"] ?: ""
            
            Log.d(TAG, "Received group message notification for group: $groupId")
            sendNotification("messages", title, body, remoteMessage.data)
            return
        }

        // Handle the notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Notification payload: ${notification.title} - ${notification.body}")
            val title = notification.title ?: "New Notification"
            val body = notification.body ?: ""
            val screen = remoteMessage.data["screen"]
            val data = remoteMessage.data
            Log.d(TAG, "Sending notification with title: $title, body: $body")
            sendNotification(channelId, title, body, data)
        } ?: run {
            Log.d(TAG, "No notification payload in message")
        }

        // Handle data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Data payload: ${remoteMessage.data}")
            val title = remoteMessage.data["title"] ?: "New Notification"
            val body = remoteMessage.data["body"] ?: ""
            val screen = remoteMessage.data["screen"]
            val data = remoteMessage.data
            Log.d(TAG, "Sending notification from data payload with title: $title, body: $body")
            sendNotification(channelId, title, body, data)
        } else {
            Log.d(TAG, "No data payload in message")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Creating notification channels")
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Create follow requests channel
            val followRequestsChannel = NotificationChannel(
                "follow_requests",
                "Follow Requests",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(followRequestsChannel)
            Log.d(TAG, "Notification channel created: follow_requests")

            // Create messages channel
            val messagesChannel = NotificationChannel(
                "messages",
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            )
            messagesChannel.description = "Group and direct messages"
            messagesChannel.enableVibration(true)
            notificationManager.createNotificationChannel(messagesChannel)
            Log.d(TAG, "Notification channel created: messages")
            
            // Create default channel
            val defaultChannel = NotificationChannel(
                "default_channel",
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(defaultChannel)
            Log.d(TAG, "Notification channel created: default_channel")
        } else {
            Log.d(TAG, "Skipping notification channel creation (Android version < O)")
        }
    }

    private fun sendNotification(channelId: String, title: String, messageBody: String, data: Map<String, String>) {
        Log.d(TAG, "Preparing to send notification: $title")
        Log.d(TAG, "Channel ID: $channelId, Message: $messageBody")
        Log.d(TAG, "Data: $data")

        // Create an intent based on notification type
        val intent = when {
            data["notificationType"] == "GROUP_MESSAGE" -> {
                // Create intent for group chat activity
                val groupIntent = Intent(this, MainActivity::class.java).apply {
                    putExtra("OPEN_GROUP_CHAT", true)
                    putExtra("GROUP_ID", data["groupId"])
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                groupIntent
            }
            else -> {
                // Default to MainActivity
                Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
            }
        }

        // Create a PendingIntent for the notification
        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, pendingIntentFlag)
        Log.d(TAG, "PendingIntent created successfully")

        // Create the notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notifications_navbar)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        Log.d(TAG, "Notification builder created")

        // Show the notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
        Log.d(TAG, "Notification displayed with ID: $notificationId")
    }
}
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
//import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.DatabaseService
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
        //val databaseService = DatabaseService()
        //val currentUserId = databaseService.currentUserId
//        if (currentUserId != null) {
//            Log.d(TAG, "Saving token for user: $currentUserId")
//            databaseService.usersRef.child(currentUserId).child("fcmToken").setValue(token)
//                .addOnSuccessListener {
//                    Log.d(TAG, "Token saved successfully for user $currentUserId")
//                }
//                .addOnFailureListener { e ->
//                    Log.e(TAG, "Failed to save token for user $currentUserId", e)
//                }
//        } else {
//            Log.e(TAG, "Current user ID is null, cannot save token")
//        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        Log.d(TAG, "Message data: ${remoteMessage.data}")

        val channelId = remoteMessage.data["channelId"] ?: "default_channel"
        Log.d(TAG, "Using channel ID: $channelId")

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
            notificationManager.createNotificationChannel(messagesChannel)
            Log.d(TAG, "Notification channel created: messages")
        } else {
            Log.d(TAG, "Skipping notification channel creation (Android version < O)")
        }
    }

    private fun sendNotification(channelId: String, title: String, messageBody: String, data: Map<String, String>) {
        Log.d(TAG, "Preparing to send notification: $title")
        Log.d(TAG, "Channel ID: $channelId, Message: $messageBody")
        Log.d(TAG, "Data: $data")

        // Create an intent to open the appropriate activity
        val userId = data["userId"] // Extract userId from the data payload
        Log.d(TAG, "User ID from data: $userId")

        val intent = when (channelId) {
            "follow_requests" -> {
                Log.d(TAG, "Creating intent for NotificationActivity")
                //Intent(this, NotificationActivity::class.java)
            }
            "messages" -> {
                Log.d(TAG, "Creating intent for ChatActivity with user ID: $userId")
                //val chatIntent = Intent(this, ChatActivity::class.java)
//                chatIntent.putExtra("CHAT_USER_ID", userId)
//                chatIntent
            }
            else -> {
                Log.d(TAG, "Creating default intent for MainActivity")
                Intent(this, MainActivity::class.java)
            }
        }
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

        // Create a PendingIntent for the notification
        val pendingIntentFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        //val pendingIntent = PendingIntent.getActivity(this, 0, intent, pendingIntentFlag)
        Log.d(TAG, "PendingIntent created successfully")

//        // Create the notification
//        val notificationBuilder = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.notification)
//            .setContentTitle(title)
//            .setContentText(messageBody)
//            .setAutoCancel(true)
//            .setContentIntent(pendingIntent)

        Log.d(TAG, "Notification builder created")

        // Show the notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
       // notificationManager.notify(notificationId, notificationBuilder.build())
        Log.d(TAG, "Notification displayed with ID: $notificationId")
    }
}
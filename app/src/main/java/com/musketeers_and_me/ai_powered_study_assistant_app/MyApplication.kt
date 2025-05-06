package com.musketeers_and_me.ai_powered_study_assistant_app

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

class MyApplication : Application(), DefaultLifecycleObserver {
    private lateinit var auth: FirebaseAuth
    private val handler = Handler(Looper.getMainLooper())
    private val offlineRunnable = Runnable {
        setUserOffline()
    }
    
    override fun onStart(owner: LifecycleOwner) {
        // App comes to foreground
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            setUserOnline() // Only set status if the user is logged in
            handler.removeCallbacks(offlineRunnable)
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        // App goes to background
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            handler.postDelayed(offlineRunnable, 5000) // Delay setting offline
        }
    }

    private fun setUserOnline() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("UserStatus", "User ID is null, cannot update status")
            return
        }

        Log.d("UserStatus", "Updating status to ONLINE for UserID: $userId")

        val userStatusRef = FirebaseDatabase.getInstance().getReference("users")
            .child(userId).child("profile").child("status")

        userStatusRef.setValue("online")
        userStatusRef.onDisconnect().setValue("offline")
    }

    private fun setUserOffline() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("UserStatus", "User ID is null, cannot update status")
            return
        }

        Log.d("UserStatus", "Updating status to OFFLINE for UserID: $userId")

        val userStatusRef = FirebaseDatabase.getInstance().getReference("users")
            .child(userId).child("profile").child("status")

        userStatusRef.setValue("offline")
    }

    override fun onCreate() {
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // Initialize Firebase (use the standard initialization for Android apps)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Enable Firebase Messaging Auto Initialization
        FirebaseMessaging.getInstance().isAutoInitEnabled = true

        // Fetch FCM token
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }
                val token = task.result
                Log.d("FCM", "Firebase Cloud Messaging Token: $token")

                // Save token to Firebase Realtime Database
                val currentUserId = auth.currentUser?.uid
                if (currentUserId != null) {
                    val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId)
                    databaseRef.child("fcmToken").setValue(token)
                        .addOnSuccessListener { Log.d("FCM", "Token saved successfully in Firebase.") }
                        .addOnFailureListener { Log.w("FCM", "Failed to save token", it) }
                }
            }
    }
}
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
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.OfflineFirstDataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.cancel

class MyApplication : Application(), DefaultLifecycleObserver {
    private lateinit var auth: FirebaseAuth
    private val handler = Handler(Looper.getMainLooper())
    private val offlineRunnable = Runnable {
        setUserOffline()
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val TAG = "MyApplication"

    // Data manager for SQLite/Firebase operations
    lateinit var dataManager: OfflineFirstDataManager
        private set

    override fun onStart(owner: LifecycleOwner) {
        // App comes to foreground
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            setUserOnline() // Only set status if the user is logged in
            handler.removeCallbacks(offlineRunnable)
            
            // Initialize data manager if needed
            scope.launch {
                try {
                    dataManager.initialize()
                } catch (e: Exception) {
                    Log.e("MyApplication", "Error initializing data manager", e)
                }
            }
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
        Log.d(TAG, "Application onCreate")
        
        // Initialize Firebase first
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        // Enable Firebase persistence before any other Firebase operations
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize DataManager (core components are initialized in constructor)
        dataManager = OfflineFirstDataManager.getInstance(this)

        // Start network monitoring and Firebase sync in background
        scope.launch {
            try {
                dataManager.initialize()
                Log.d(TAG, "Data manager initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing data manager", e)
            }
        }

        // Add lifecycle observer after initialization
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        // Fetch FCM token in background
        scope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                Log.d("FCM", "Firebase Cloud Messaging Token: $token")

                // Save token to Firebase Realtime Database
                val currentUserId = auth.currentUser?.uid
                if (currentUserId != null) {
                    val databaseRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId)
                    databaseRef.child("fcmToken").setValue(token)
                        .addOnSuccessListener { Log.d("FCM", "Token saved successfully in Firebase.") }
                        .addOnFailureListener { Log.w("FCM", "Failed to save token", it) }
                }
            } catch (e: Exception) {
                Log.e("FCM", "Error fetching FCM token", e)
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        scope.cancel()
    }
}
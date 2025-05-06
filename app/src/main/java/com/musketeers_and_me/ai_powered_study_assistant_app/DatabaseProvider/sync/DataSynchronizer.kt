package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.repository.UserProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Manages data synchronization between local SQLite database and Firebase.
 * Handles network connectivity checks and coordinates sync operations.
 */
class DataSynchronizer(
    private val context: Context,
    private val userProfileRepository: UserProfileRepository
    // Add other repositories as needed
) {
    private val TAG = "DataSynchronizer"
    private val scope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Start listening for changes from Firebase for real-time updates
     */
    fun startListeningForRemoteChanges(userId: String) {
        userProfileRepository.listenForUserChanges(userId)
        // Add listeners for other data types
    }
    
    /**
     * Stop listening for Firebase changes (e.g. on logout)
     */
    fun stopListeningForRemoteChanges() {
        // Remove all listeners
        // Note: In a real implementation, you would need to store the listener
        // references and remove them explicitly
    }
    
    /**
     * Synchronize all pending local changes to Firebase
     */
    fun syncPendingChangesToCloud() {
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Network unavailable, skipping sync")
            return
        }
        
        scope.launch {
            syncUserProfiles()
            // Sync other data types
        }
    }
    
    /**
     * Fetch all data for a user from Firebase
     */
    fun fetchAllUserDataFromCloud(userId: String, onComplete: () -> Unit) {
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Network unavailable, cannot fetch user data")
            onComplete()
            return
        }
        
        scope.launch {
            try {
                // Fetch user profile from Firebase
                userProfileRepository.fetchCurrentUserFromFirebase { user ->
                    // Fetch other data types if needed
                    
                    // Switch to main thread for completion callback
                    scope.launch(Dispatchers.Main) {
                        onComplete()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching data from cloud", e)
                // Switch to main thread for completion callback even on error
                scope.launch(Dispatchers.Main) {
                    onComplete()
                }
            }
        }
    }
    
    /**
     * Clear all local data (e.g. when user logs out)
     */
    fun clearLocalData() {
        // Clear all local SQLite data
        // In a real implementation, you would delete all tables or reset the database
    }
    
    /**
     * Sync all pending user profiles to Firebase
     */
    private suspend fun syncUserProfiles() {
        // Get all users with pending sync flag
        val pendingUsers = withContext(Dispatchers.IO) {
            userProfileRepository.getPendingSyncUsers()
        }
        
        pendingUsers.forEach { user ->
            userProfileRepository.syncUserToFirebase(user) { success ->
                if (success) {
                    Log.d(TAG, "Successfully synced user ${user.id} to Firebase")
                } else {
                    Log.e(TAG, "Failed to sync user ${user.id} to Firebase")
                }
            }
        }
    }
    
    /**
     * Check if network connection is available
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            return networkInfo != null && networkInfo.isConnected
        }
    }
} 
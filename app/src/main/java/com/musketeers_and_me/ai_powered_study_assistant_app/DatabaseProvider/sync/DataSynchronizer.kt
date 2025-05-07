package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBReadOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBWriteOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.repository.UserProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Handles synchronization between local SQLite database and Firebase.
 * Manages both one-way sync (Firebase -> SQLite) and two-way sync (pending changes).
 */
class DataSynchronizer(
    private val context: Context,
    private val userProfileRepository: UserProfileRepository
    // Add other repositories as needed
) {
    private val TAG = "DataSynchronizer"
    private val databaseService = FBDataBaseService()
    private val fbReadOperations = FBReadOperations(databaseService)
    private val fbWriteOperations = FBWriteOperations(databaseService)
    private val scope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Start listening for Firebase changes and sync them to local database
     */
    fun startListening() {
        scope.launch {
            try {
                // Listen for user changes
                fbReadOperations.listenForUserChanges { user ->
                    userProfileRepository.saveUserToLocalDatabase(user, false)
                }

                // Listen for course changes
                fbReadOperations.listenForCourseChanges { course ->
                    userProfileRepository.saveCourseToLocalDatabase(course, false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting Firebase listeners", e)
            }
        }
    }
    
    /**
     * Sync pending local changes to Firebase
     */
    suspend fun syncPendingChangesToCloud() {
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Network unavailable, skipping sync")
            return
        }

        try {
            // Get pending users
            val pendingUsers = userProfileRepository.getPendingSyncUsers()
            for (user in pendingUsers) {
                fbWriteOperations.saveUser(user)
                userProfileRepository.markUserAsSynchronized(user.id)
            }

            // Get pending courses
            val pendingCourses = userProfileRepository.getPendingSyncCourses()
            for (course in pendingCourses) {
                fbWriteOperations.saveCourse(course)
                userProfileRepository.markCourseAsSynchronized(course.courseId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing pending changes to Firebase", e)
            throw e
        }
    }
    
    /**
     * Stop listening for Firebase changes
     */
    fun stopListening() {
        fbReadOperations.removeAllListeners()
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
                fbReadOperations.getUser(userId) { user ->
                    if (user != null) {
                        userProfileRepository.saveUserToLocalDatabase(user, false)
                    }
                    
                    // Fetch user's courses
                    fbReadOperations.getUserCourses(userId) { courses ->
                        courses.forEach { course ->
                            userProfileRepository.saveCourseToLocalDatabase(course, false)
                        }
                        
                        // Switch to main thread for completion callback
                        scope.launch(Dispatchers.Main) {
                            onComplete()
                        }
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
        userProfileRepository.clearAllData()
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
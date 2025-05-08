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
        Log.d(TAG, "Starting Firebase listeners")
        scope.launch {
            try {
                // Listen for user changes
                Log.d(TAG, "Setting up user changes listener")
                fbReadOperations.listenForUserChanges { user ->
                    Log.d(TAG, "Received user change from Firebase: ${user.id}")
                    userProfileRepository.saveUserToLocalDatabase(user, false)
                }

                // Listen for course changes
                Log.d(TAG, "Setting up course changes listener")
                fbReadOperations.listenForCourseChanges { course ->
                    Log.d(TAG, "Received course change from Firebase: ${course.courseId}")
                    userProfileRepository.saveCourseToLocalDatabase(course, false)
                }
                Log.d(TAG, "Firebase listeners setup completed")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting Firebase listeners", e)
            }
        }
    }
    
    /**
     * Sync pending local changes to Firebase
     */
    suspend fun syncPendingChangesToCloud() {
        Log.d(TAG, "Starting sync of pending changes to cloud")
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Network unavailable, sync postponed")
            return
        }

        try {
            // Get pending users
            val pendingUsers = userProfileRepository.getPendingSyncUsers()
            Log.d(TAG, "Found ${pendingUsers.size} pending users to sync")
            for (user in pendingUsers) {
                Log.d(TAG, "Syncing user to Firebase: ${user.id}")
                fbWriteOperations.saveUser(user)
                userProfileRepository.markUserAsSynchronized(user.id)
                Log.d(TAG, "User synced successfully: ${user.id}")
            }

            // Get pending courses
            val pendingCourses = userProfileRepository.getPendingSyncCourses()
            Log.d(TAG, "Found ${pendingCourses.size} pending courses to sync")
            for (course in pendingCourses) {
                Log.d(TAG, "Syncing course to Firebase: ${course.courseId}")
                fbWriteOperations.saveCourse(course)
                userProfileRepository.markCourseAsSynchronized(course.courseId)
                Log.d(TAG, "Course synced successfully: ${course.courseId}")
            }
            Log.d(TAG, "Sync completed successfully")
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
        Log.d(TAG, "Fetching all user data from cloud for user: $userId")
        if (!isNetworkAvailable()) {
            Log.d(TAG, "Network unavailable, cannot fetch user data")
            onComplete()
            return
        }
        
        scope.launch {
            try {
                // Fetch user profile from Firebase
                Log.d(TAG, "Fetching user profile from Firebase")
                fbReadOperations.getUser(userId) { user ->
                    if (user != null) {
                        Log.d(TAG, "User profile fetched successfully")
                        userProfileRepository.saveUserToLocalDatabase(user, false)
                    } else {
                        Log.d(TAG, "No user profile found in Firebase")
                    }
                    
                    // Fetch user's courses
                    Log.d(TAG, "Fetching user's courses from Firebase")
                    fbReadOperations.getUserCourses(userId) { courses ->
                        Log.d(TAG, "Fetched ${courses.size} courses from Firebase")
                        courses.forEach { course ->
                            userProfileRepository.saveCourseToLocalDatabase(course, false)
                        }
                        
                        // Switch to main thread for completion callback
                        scope.launch(Dispatchers.Main) {
                            Log.d(TAG, "User data fetch completed")
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
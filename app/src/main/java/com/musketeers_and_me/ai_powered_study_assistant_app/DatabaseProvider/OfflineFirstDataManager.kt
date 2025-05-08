package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.dao.UserLocalDao
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.repository.UserProfileRepository
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.sync.BackgroundSyncWorker
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.sync.DataSynchronizer
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Central data manager for the application that implements an offline-first architecture.
 * Provides a unified access point to all data operations in the app.
 * Coordinates between local SQLite storage and remote Firebase operations.
 */
class OfflineFirstDataManager private constructor(private val context: Context) {
    
    private val dbHelper = AppDatabase(context)
    private val db = dbHelper.writableDatabase
    private val scope = CoroutineScope(Dispatchers.IO)
    
    // DAOs
    private val userLocalDao = UserLocalDao(db)
    // Add other DAOs here
    
    // Repositories
    private val userProfileRepository = UserProfileRepository(userLocalDao)
    // Add other repositories here
    
    // Sync manager
    private val dataSynchronizer = DataSynchronizer(context, userProfileRepository)
    
    // Firebase
    private val auth = FirebaseAuth.getInstance()
    
    // Current user
    var currentUser: UserProfile? = null
        private set
    
    /**
     * Initialize data manager components
     */
    fun initialize() {
        Log.d("OfflineFirstDataManager", "Initializing data manager")
        // Check if a user is logged in
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            Log.d("OfflineFirstDataManager", "User is logged in: ${firebaseUser.uid}")
            // Try to get user from local database first
            currentUser = userProfileRepository.getUserLocally(firebaseUser.uid)
            Log.d("OfflineFirstDataManager", "Local user data: ${currentUser != null}")
            
            // Start listening for Firebase changes
            dataSynchronizer.startListening()
            Log.d("OfflineFirstDataManager", "Started Firebase listeners")
            
            // Schedule periodic sync
            schedulePeriodicSync()
            Log.d("OfflineFirstDataManager", "Scheduled periodic sync")
            
            // If not found locally or out of date, fetch from Firebase
            if (currentUser == null) {
                Log.d("OfflineFirstDataManager", "User not found locally, fetching from Firebase")
                dataSynchronizer.fetchAllUserDataFromCloud(firebaseUser.uid) {
                    // Reload current user from local database after sync
                    currentUser = userProfileRepository.getUserLocally(firebaseUser.uid)
                    Log.d("OfflineFirstDataManager", "User data fetched from Firebase: ${currentUser != null}")
                }
            }
        } else {
            Log.d("OfflineFirstDataManager", "No user logged in")
        }
    }
    
    /**
     * Start a new user session after login
     */
    fun initializeUserSession(userId: String, onComplete: () -> Unit) {
        Log.d("OfflineFirstDataManager", "Initializing user session for: $userId")
        // Clear any existing data (in case of user switch)
        dataSynchronizer.clearLocalData()
        Log.d("OfflineFirstDataManager", "Cleared existing local data")
        
        // Fetch user data from Firebase
        dataSynchronizer.fetchAllUserDataFromCloud(userId) {
            Log.d("OfflineFirstDataManager", "User data fetched from Firebase")
            // Start listening for changes
            dataSynchronizer.startListening()
            Log.d("OfflineFirstDataManager", "Started Firebase listeners")
            
            // Schedule periodic sync
            schedulePeriodicSync()
            Log.d("OfflineFirstDataManager", "Scheduled periodic sync")
            
            // Update current user
            currentUser = userProfileRepository.getUserLocally(userId)
            Log.d("OfflineFirstDataManager", "Current user updated: ${currentUser != null}")
            
            onComplete()
        }
    }
    
    /**
     * End current user session on logout
     */
    fun endUserSession() {
        // Stop listening for Firebase changes
        dataSynchronizer.stopListening()
        
        // Clear local data
        dataSynchronizer.clearLocalData()
        
        // Cancel sync worker
        WorkManager.getInstance(context).cancelUniqueWork(BackgroundSyncWorker.WORK_NAME)
        
        // Clear current user
        currentUser = null
    }
    
    /**
     * Schedule periodic background sync
     */
    private fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncWorkRequest = PeriodicWorkRequestBuilder<BackgroundSyncWorker>(
            15, TimeUnit.MINUTES, // Sync every 15 minutes
            5, TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            BackgroundSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )
    }
    
    /**
     * Trigger immediate sync
     */
    fun syncNow() {
        Log.d("OfflineFirstDataManager", "Triggering immediate sync")
        scope.launch {
            try {
                if (!isNetworkAvailable()) {
                    Log.d("OfflineFirstDataManager", "Network unavailable, sync postponed")
                    return@launch
                }
                dataSynchronizer.syncPendingChangesToCloud()
                Log.d("OfflineFirstDataManager", "Sync completed successfully")
            } catch (e: Exception) {
                Log.e("OfflineFirstDataManager", "Error during sync", e)
            }
        }
    }
    
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
    
    /**
     * Get user profile repository for direct access
     */
    fun getUserProfileRepository(): UserProfileRepository {
        return userProfileRepository
    }
    
    /**
     * Get user local DAO for direct access
     */
    fun getUserLocalDao(): UserLocalDao {
        return userLocalDao
    }
    
    /**
     * Check if there are any pending sync items
     */
    fun hasPendingSyncItems(): Boolean {
        val pendingUsers = userProfileRepository.getPendingSyncUsers()
        val pendingCourses = userProfileRepository.getPendingSyncCourses()
        val hasPending = pendingUsers.isNotEmpty() || pendingCourses.isNotEmpty()
        Log.d("OfflineFirstDataManager", "Checking pending sync items - Users: ${pendingUsers.size}, Courses: ${pendingCourses.size}")
        return hasPending
    }
    
    companion object {
        @Volatile
        private var INSTANCE: OfflineFirstDataManager? = null
        
        fun getInstance(context: Context): OfflineFirstDataManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: OfflineFirstDataManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
} 
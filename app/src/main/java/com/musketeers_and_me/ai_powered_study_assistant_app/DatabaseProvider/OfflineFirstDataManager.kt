package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider

import android.content.Context
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
        // Check if a user is logged in
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            // Try to get user from local database first
            currentUser = userProfileRepository.getUserLocally(firebaseUser.uid)
            
            // Start listening for Firebase changes
            dataSynchronizer.startListening()
            
            // Schedule periodic sync
            schedulePeriodicSync()
            
            // If not found locally or out of date, fetch from Firebase
            if (currentUser == null) {
                dataSynchronizer.fetchAllUserDataFromCloud(firebaseUser.uid) {
                    // Reload current user from local database after sync
                    currentUser = userProfileRepository.getUserLocally(firebaseUser.uid)
                }
            }
        }
    }
    
    /**
     * Start a new user session after login
     */
    fun initializeUserSession(userId: String, onComplete: () -> Unit) {
        // Clear any existing data (in case of user switch)
        dataSynchronizer.clearLocalData()
        
        // Fetch user data from Firebase
        dataSynchronizer.fetchAllUserDataFromCloud(userId) {
            // Start listening for changes
            dataSynchronizer.startListening()
            
            // Schedule periodic sync
            schedulePeriodicSync()
            
            // Update current user
            currentUser = userProfileRepository.getUserLocally(userId)
            
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
        scope.launch {
            try {
                dataSynchronizer.syncPendingChangesToCloud()
            } catch (e: Exception) {
                Log.e("OfflineFirstDataManager", "Error during sync", e)
                // You might want to notify the user or handle the error in some way
            }
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
        return pendingUsers.isNotEmpty() || pendingCourses.isNotEmpty()
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
package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.dao.UserLocalDao
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.UserProfile
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first

/**
 * Central data manager for the application that implements an offline-first architecture.
 * Provides a unified access point to all data operations in the app.
 * Coordinates between local SQLite storage and remote Firebase operations.
 */
class OfflineFirstDataManager private constructor(private val context: Context) {
    private val TAG = "OfflineFirstDataManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val db = AppDatabase.getInstance(context)
    private val firebaseSyncManager = FirebaseSyncManager(db)
    private val networkMonitor = NetworkConnectivityMonitor(context)
    
    // DAOs
    private val userLocalDao = UserLocalDao(db.writableDatabase)
    
    // Firebase
    private val auth = FirebaseAuth.getInstance()
    
    // Current user
    var currentUser: UserProfile? = null
        private set

    private var isInitialized = false
    
    suspend fun initialize() {
        if (isInitialized) {
            Log.d(TAG, "Already initialized, skipping")
            return
        }

        Log.d(TAG, "Initializing OfflineFirstDataManager")
        try {
            withContext(Dispatchers.IO) {
                // Start network monitoring
                networkMonitor.startMonitoring()
                Log.d(TAG, "Network monitoring started")
                
                // Initialize Firebase sync
                firebaseSyncManager.initialize()
                Log.d(TAG, "Firebase sync initialized")

                // Start listening for network changes
                scope.launch {
                    networkMonitor.isOnline.collect { isOnline ->
                        if (isOnline) {
                            // When online, sync any pending changes
                            syncPendingChanges()
                        }
                    }
                }

                isInitialized = true
                Log.d(TAG, "Initialization completed successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing", e)
            isInitialized = false
            throw e
        }
    }
    
    private fun syncPendingChanges() {
        val userId = auth.currentUser?.uid ?: return
        if (!isInitialized) {
            Log.w(TAG, "Cannot sync changes: manager not initialized")
            return
        }

        scope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting sync of pending changes for user: $userId")
                // Start real-time sync with Firebase
                firebaseSyncManager.startSync(userId)
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing pending changes", e)
            }
        }
    }
    
    suspend fun saveCourse(userId: String, course: Course) {
        if (!isInitialized) {
            Log.e(TAG, "Cannot save course: manager not initialized")
            throw IllegalStateException("OfflineFirstDataManager not initialized")
        }

        Log.d(TAG, "Saving course: ${course.courseId}")
        try {
            withContext(Dispatchers.IO) {
                // Save to SQLite
                userLocalDao.insertCourse(userId, course)
                userLocalDao.markCourseForSync(course.courseId)
                Log.d(TAG, "Course saved to SQLite and marked for sync")

                // If online, sync to Firebase
                if (networkMonitor.isOnline.first()) {
                    Log.d(TAG, "Network available, syncing to Firebase")
                    firebaseSyncManager.syncCourse(course)
                } else {
                    Log.d(TAG, "Network unavailable, course will be synced when online")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving course", e)
            throw e
        }
    }
    
    suspend fun getCourses(userId: String): List<Course> {
        if (!isInitialized) {
            Log.e(TAG, "Cannot get courses: manager not initialized")
            throw IllegalStateException("OfflineFirstDataManager not initialized")
        }

        Log.d(TAG, "Getting courses for user: $userId")
        return withContext(Dispatchers.IO) {
            try {
                // First try to get from SQLite
                val courses = userLocalDao.getCoursesByUserId(userId)
                Log.d(TAG, "Retrieved ${courses.size} courses from SQLite")

                // If online, sync with Firebase
                if (networkMonitor.isOnline.first()) {
                    Log.d(TAG, "Network available, syncing with Firebase")
                    firebaseSyncManager.syncCoursesFromFirebase(userId)
                } else {
                    Log.d(TAG, "Network unavailable, using local data only")
                }

                courses
            } catch (e: Exception) {
                Log.e(TAG, "Error getting courses", e)
                throw e
            }
        }
    }
    
    suspend fun cleanup() {
        Log.d(TAG, "Cleaning up OfflineFirstDataManager")
        try {
            withContext(Dispatchers.IO) {
                networkMonitor.stopMonitoring()
                Log.d(TAG, "Network monitoring stopped")
                firebaseSyncManager.cleanup()
                Log.d(TAG, "Firebase sync cleaned up")
                isInitialized = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
            throw e
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: OfflineFirstDataManager? = null
        
        fun getInstance(context: Context): OfflineFirstDataManager {
            return INSTANCE ?: synchronized(this) {
                val instance = OfflineFirstDataManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
} 
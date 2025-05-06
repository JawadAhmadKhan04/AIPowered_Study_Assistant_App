package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.AppDatabase
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.dao.UserLocalDao
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.repository.UserProfileRepository

/**
 * Background worker for periodic synchronization between local database and Firebase.
 * Runs on a schedule or when network becomes available.
 */
class BackgroundSyncWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val TAG = "BackgroundSyncWorker"
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting background sync")
        
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.d(TAG, "No user logged in, skipping sync")
            return Result.success()
        }
        
        try {
            // Create database connection
            val dbHelper = AppDatabase(context)
            val db = dbHelper.writableDatabase
            
            // Initialize DAOs
            val userLocalDao = UserLocalDao(db)
            
            // Initialize repositories
            val userProfileRepository = UserProfileRepository(userLocalDao)
            
            // Create data synchronizer
            val dataSynchronizer = DataSynchronizer(context, userProfileRepository)
            
            // Perform sync
            dataSynchronizer.syncPendingChangesToCloud()
            
            // Clean up
            db.close()
            
            Log.d(TAG, "Background sync completed successfully")
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error during background sync", e)
            return Result.retry()
        }
    }
    
    companion object {
        const val WORK_NAME = "background_sync_worker"
    }
} 
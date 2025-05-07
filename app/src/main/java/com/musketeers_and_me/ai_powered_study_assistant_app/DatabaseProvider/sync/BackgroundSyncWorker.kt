package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.OfflineFirstDataManager

/**
 * Background worker that handles periodic synchronization of local data with Firebase.
 * Runs every 15 minutes when network is available.
 */
class BackgroundSyncWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting background sync")
            
            // Get data manager instance
            val dataManager = OfflineFirstDataManager.getInstance(context)
            
            // Sync pending changes to Firebase
            dataManager.syncNow()
            
            Log.d(TAG, "Background sync completed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Background sync failed", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "BackgroundSyncWorker"
        const val WORK_NAME = "background_sync_work"
    }
} 
package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.dao

import android.content.ContentValues

/**
 * Base interface for all local data access objects.
 * Defines common CRUD operations and sync status management.
 */
interface LocalDataAccessObject<T> {
    /**
     * Insert a new item into the local database
     */
    fun insert(item: T): Long
    
    /**
     * Update an existing item in the database
     */
    fun update(item: T): Int
    
    /**
     * Delete an item from the database by its ID
     */
    fun delete(id: String): Int
    
    /**
     * Retrieve a single item by its ID
     */
    fun getById(id: String): T?
    
    /**
     * Retrieve all items of this type
     */
    fun getAll(): List<T>
    
    /**
     * Retrieve all items marked for synchronization
     */
    fun getPendingSyncItems(): List<T>
    
    /**
     * Mark an item as synchronized with the remote database
     */
    fun markSynchronized(id: String)
    
    /**
     * Utility method for setting common fields in ContentValues
     */
    fun setCommonFields(values: ContentValues, pendingSync: Boolean = true) {
        val currentTime = System.currentTimeMillis()
        values.put("updated_at", currentTime)
        values.put("pending_sync", if (pendingSync) 1 else 0)
    }
} 
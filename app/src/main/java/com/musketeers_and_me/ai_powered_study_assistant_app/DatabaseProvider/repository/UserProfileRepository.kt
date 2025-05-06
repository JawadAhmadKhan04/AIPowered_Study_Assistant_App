package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.dao.UserLocalDao
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.UserProfile

/**
 * Repository for user profile data that bridges between local database and Firebase.
 * Handles synchronization between local and remote data sources.
 */
class UserProfileRepository(private val userLocalDao: UserLocalDao) {
    
    private val TAG = "UserProfileRepository"
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")
    
    // Local operations
    fun saveUserLocally(user: UserProfile): Long {
        return userLocalDao.insert(user)
    }
    
    fun updateUserLocally(user: UserProfile): Int {
        return userLocalDao.update(user)
    }
    
    fun getUserLocally(userId: String): UserProfile? {
        return userLocalDao.getById(userId)
    }
    
    fun getCurrentUserLocally(): UserProfile? {
        val currentUserId = auth.currentUser?.uid ?: return null
        return userLocalDao.getById(currentUserId)
    }
    
    /**
     * Get all users with pending sync flag
     */
    fun getPendingSyncUsers(): List<UserProfile> {
        return userLocalDao.getPendingSyncItems()
    }
    
    // Firebase operations
    /**
     * Fetch current user data from Firebase and save to local database
     */
    fun fetchCurrentUserFromFirebase(onComplete: (UserProfile?) -> Unit) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            onComplete(null)
            return
        }
        
        usersRef.child(currentUserId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val user = snapshotToUser(snapshot)
                // Save to SQLite with pendingSync=false (since we're fetching from Firebase)
                saveUserToLocalDatabase(user, false)
                onComplete(user)
            } else {
                onComplete(null)
            }
        }.addOnFailureListener {
            Log.e(TAG, "Error fetching user from Firebase", it)
            onComplete(null)
        }
    }
    
    /**
     * Sync a user to Firebase and update the sync status locally
     */
    fun syncUserToFirebase(user: UserProfile, onComplete: (Boolean) -> Unit) {
        val userId = user.id
        val userMap = mapOf(
            "name" to user.name,
            "email" to user.email,
            "profileImage" to (user.profileImage ?: ""),
            "fcmToken" to (user.fcmToken ?: ""),
            "status" to (user.status ?: "offline")
        )
        
        usersRef.child(userId).updateChildren(userMap)
            .addOnSuccessListener {
                // Mark as synced in SQLite
                userLocalDao.markSynchronized(userId)
                onComplete(true)
            }
            .addOnFailureListener {
                Log.e(TAG, "Failed to sync user to Firebase", it)
                onComplete(false)
            }
    }
    
    /**
     * Listen for remote changes to a user profile
     */
    fun listenForUserChanges(userId: String) {
        usersRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshotToUser(snapshot)
                    // Save to SQLite with pendingSync=false (since we're fetching from Firebase)
                    saveUserToLocalDatabase(user, false)
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase listener cancelled", error.toException())
            }
        })
    }
    
    // Helper methods
    private fun saveUserToLocalDatabase(user: UserProfile, pendingSync: Boolean) {
        val existingUser = userLocalDao.getById(user.id)
        if (existingUser == null) {
            // Insert new user
            userLocalDao.insert(user)
        } else {
            // Update existing user
            userLocalDao.update(user)
        }
    }
    
    private fun snapshotToUser(snapshot: DataSnapshot): UserProfile {
        val id = snapshot.key ?: ""
        val name = snapshot.child("name").getValue(String::class.java) ?: ""
        val email = snapshot.child("email").getValue(String::class.java) ?: ""
        val profileImage = snapshot.child("profileImage").getValue(String::class.java)
        val fcmToken = snapshot.child("fcmToken").getValue(String::class.java)
        val status = snapshot.child("status").getValue(String::class.java) ?: "offline"
        
        return UserProfile(
            id = id,
            name = name,
            email = email,
            profileImage = profileImage,
            fcmToken = fcmToken,
            status = status,
            pendingSync = false // Since this is from Firebase, it's considered synced
        )
    }
} 
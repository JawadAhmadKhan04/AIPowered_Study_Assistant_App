package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.dao.UserLocalDao
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
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
        Log.d(TAG, "Saving user locally: ${user.id}")
        return userLocalDao.insert(user)
    }
    
    fun updateUserLocally(user: UserProfile): Int {
        Log.d(TAG, "Updating user locally: ${user.id}")
        return userLocalDao.update(user)
    }
    
    fun getUserLocally(userId: String): UserProfile? {
        Log.d(TAG, "Getting user locally: $userId")
        return userLocalDao.getUserById(userId)
    }
    
    fun getCurrentUserLocally(): UserProfile? {
        val currentUserId = auth.currentUser?.uid ?: return null
        Log.d(TAG, "Getting current user locally: $currentUserId")
        return userLocalDao.getUserById(currentUserId)
    }
    
    /**
     * Get all users with pending sync flag
     */
    fun getPendingSyncUsers(): List<UserProfile> {
        Log.d(TAG, "Getting pending sync users")
        return userLocalDao.getPendingSyncItems()
    }

    /**
     * Get all courses with pending sync flag
     */
    fun getPendingSyncCourses(): List<Course> {
        Log.d(TAG, "Getting pending sync courses")
        return userLocalDao.getPendingSyncCourses()
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
            "email" to user.email,
            "username" to user.username,
            "createdAt" to user.createdAt,
            "lastLogin" to user.lastLogin,
            "fcmToken" to (user.fcmToken ?: "")
        )
        
        usersRef.child(userId).updateChildren(userMap)
            .addOnSuccessListener {
                // Mark as synced in SQLite
                markUserAsSynchronized(userId)
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
    fun listenForRemoteChanges(userId: String) {
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
    fun saveUserToLocalDatabase(user: UserProfile, pendingSync: Boolean) {
        Log.d(TAG, "Saving user to local database: ${user.id}, pendingSync: $pendingSync")
        val existingUser = userLocalDao.getUserById(user.id)
        if (existingUser == null) {
            Log.d(TAG, "Inserting new user: ${user.id}")
            userLocalDao.insert(user)
        } else {
            Log.d(TAG, "Updating existing user: ${user.id}")
            userLocalDao.update(user)
        }
    }

    fun saveCourseToLocalDatabase(course: Course, pendingSync: Boolean) {
        Log.d(TAG, "Saving course to local database: ${course.courseId}, pendingSync: $pendingSync")
        val existingCourse = userLocalDao.getCourseById(course.courseId)
        if (existingCourse == null) {
            Log.d(TAG, "Inserting new course: ${course.courseId}")
            userLocalDao.insertCourse(auth.currentUser?.uid ?: "", course)
        } else {
            Log.d(TAG, "Updating existing course: ${course.courseId}")
            // Check if course is pending sync in the database
            val isPendingSync = userLocalDao.isCoursePendingSync(course.courseId)
            if (!isPendingSync) {
                userLocalDao.updateCourse(course)
            } else {
                Log.d(TAG, "Skipping update for pending sync course: ${course.courseId}")
            }
        }
    }

    fun markUserAsSynchronized(userId: String) {
        Log.d(TAG, "Marking user as synchronized: $userId")
        userLocalDao.markSynchronized(userId)
    }

    fun markCourseAsSynchronized(courseId: String) {
        Log.d(TAG, "Marking course as synchronized: $courseId")
        userLocalDao.markCourseSynchronized(courseId)
    }

    fun clearAllData() {
        Log.d(TAG, "Clearing all local data")
        userLocalDao.clearAllData()
    }
    
    private fun snapshotToUser(snapshot: DataSnapshot): UserProfile {
        val id = snapshot.key ?: ""
        val email = snapshot.child("email").getValue(String::class.java) ?: ""
        val username = snapshot.child("username").getValue(String::class.java) ?: ""
        val createdAt = snapshot.child("createdAt").getValue(Long::class.java) ?: System.currentTimeMillis()
        val lastLogin = snapshot.child("lastLogin").getValue(Long::class.java) ?: System.currentTimeMillis()
        val fcmToken = snapshot.child("fcmToken").getValue(String::class.java)
        
        return UserProfile(
            id = id,
            email = email,
            username = username,
            createdAt = createdAt,
            lastLogin = lastLogin,
            fcmToken = fcmToken,
            pendingSync = false // Since this is from Firebase, it's considered synced
        )
    }
} 
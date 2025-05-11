package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.dao.UserLocalDao
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import kotlinx.coroutines.withContext

class FirebaseSyncManager(private val db: AppDatabase) {
    private val TAG = "FirebaseSyncManager"
    private val databaseService = FBDataBaseService()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val listeners = mutableListOf<ValueEventListener>()
    private val auth = FirebaseAuth.getInstance()
    private var database: FirebaseDatabase? = null
    private var usersRef: DatabaseReference? = null
    private var coursesRef: DatabaseReference? = null

    private fun setupFirebaseListeners() {
        Log.d(TAG, "Setting up Firebase listeners")
        database?.let { db ->
            usersRef = db.getReference("users")
            coursesRef = db.getReference("courses")
            Log.d(TAG, "Firebase references initialized")
        } ?: run {
            Log.e(TAG, "Database is null, cannot setup listeners")
        }
    }

    private fun logDatabaseState() {
        val sqliteDb = db.readableDatabase
        Log.d(TAG, "=== Current SQLite Database State ===")
        
        // Log Users table
        val usersCursor = sqliteDb.query(AppDatabase.TABLE_USERS, null, null, null, null, null, null)
        Log.d(TAG, "Users table entries: ${usersCursor.count}")
        while (usersCursor.moveToNext()) {
            Log.d(TAG, "User: ${usersCursor.getString(usersCursor.getColumnIndexOrThrow("id"))}")
        }
        usersCursor.close()

        // Log Courses table
        val coursesCursor = sqliteDb.query(AppDatabase.TABLE_COURSES, null, null, null, null, null, null)
        Log.d(TAG, "Courses table entries: ${coursesCursor.count}")
        while (coursesCursor.moveToNext()) {
            Log.d(TAG, "Course: ${coursesCursor.getString(coursesCursor.getColumnIndexOrThrow("title"))}")
        }
        coursesCursor.close()

        // Log Course Members table
        val membersCursor = sqliteDb.query(AppDatabase.TABLE_COURSE_MEMBERS, null, null, null, null, null, null)
        Log.d(TAG, "Course Members table entries: ${membersCursor.count}")
        membersCursor.close()

        // Log User Settings table
        val settingsCursor = sqliteDb.query(AppDatabase.TABLE_USER_SETTINGS, null, null, null, null, null, null)
        Log.d(TAG, "User Settings table entries: ${settingsCursor.count}")
        settingsCursor.close()

        // Log User Profile table
        val profileCursor = sqliteDb.query(AppDatabase.TABLE_USER_PROFILE, null, null, null, null, null, null)
        Log.d(TAG, "User Profile table entries: ${profileCursor.count}")
        profileCursor.close()

        Log.d(TAG, "=== End Database State ===")
    }

    fun startSync(userId: String) {
        Log.d(TAG, "Starting sync for user: $userId")
        scope.launch(Dispatchers.IO) {
            try {
                // First, sync local changes to Firebase
                syncLocalChangesToFirebase(userId)
                
                // Then, sync from Firebase to local
                syncFromFirebaseToLocal(userId)
            } catch (e: Exception) {
                Log.e(TAG, "Error during sync process", e)
            }
        }
    }

    private suspend fun syncLocalChangesToFirebase(userId: String) {
        Log.d(TAG, "Syncing local changes to Firebase for user: $userId")
        val sqliteDb = db.readableDatabase

        // Sync pending courses
        val pendingCoursesCursor = sqliteDb.query(
            AppDatabase.TABLE_COURSES,
            null,
            "${AppDatabase.COLUMN_PENDING_SYNC} = ?",
            arrayOf("1"),
            null,
            null,
            null
        )

        try {
            while (pendingCoursesCursor.moveToNext()) {
                val courseId = pendingCoursesCursor.getString(pendingCoursesCursor.getColumnIndexOrThrow(AppDatabase.COLUMN_ID))
                val title = pendingCoursesCursor.getString(pendingCoursesCursor.getColumnIndexOrThrow("title"))
                val description = pendingCoursesCursor.getString(pendingCoursesCursor.getColumnIndexOrThrow("description"))
                val color = pendingCoursesCursor.getInt(pendingCoursesCursor.getColumnIndexOrThrow("color"))

                coursesRef?.let { ref ->
                    val courseRef = ref.child(courseId)
                    val courseData = mapOf(
                        "title" to title,
                        "description" to description,
                        "createdBy" to userId,
                        "color" to color,
                        "members" to mapOf(
                            userId to mapOf(
                                "lastModified" to System.currentTimeMillis()
                            )
                        )
                    )

                    try {
                        courseRef.setValue(courseData).await()
                        // Mark as synced in SQLite
                        val updateValues = ContentValues().apply {
                            put(AppDatabase.COLUMN_PENDING_SYNC, 0)
                            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
                        }
                        sqliteDb.update(
                            AppDatabase.TABLE_COURSES,
                            updateValues,
                            "${AppDatabase.COLUMN_ID} = ?",
                            arrayOf(courseId)
                        )
                        Log.d(TAG, "Course $courseId synced to Firebase successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing course $courseId to Firebase", e)
                    }
                }
            }
        } finally {
            pendingCoursesCursor.close()
        }

        // Sync pending user settings
        val pendingSettingsCursor = sqliteDb.query(
            AppDatabase.TABLE_USER_SETTINGS,
            null,
            "${AppDatabase.COLUMN_PENDING_SYNC} = ?",
            arrayOf("1"),
            null,
            null,
            null
        )

        try {
            while (pendingSettingsCursor.moveToNext()) {
                val settingsData = mapOf(
                    "quizNotifications" to (pendingSettingsCursor.getInt(pendingSettingsCursor.getColumnIndexOrThrow("quiz_notifications")) == 1),
                    "studyReminders" to (pendingSettingsCursor.getInt(pendingSettingsCursor.getColumnIndexOrThrow("study_reminders")) == 1),
                    "addInGroups" to (pendingSettingsCursor.getInt(pendingSettingsCursor.getColumnIndexOrThrow("add_in_groups")) == 1),
                    "autoLogin" to (pendingSettingsCursor.getInt(pendingSettingsCursor.getColumnIndexOrThrow("auto_login")) == 1),
                    "autoSync" to (pendingSettingsCursor.getInt(pendingSettingsCursor.getColumnIndexOrThrow("auto_sync")) == 1)
                )

                usersRef?.let { ref ->
                    try {
                        ref.child(userId).child("settings").setValue(settingsData).await()
                        // Mark as synced in SQLite
                        val updateValues = ContentValues().apply {
                            put(AppDatabase.COLUMN_PENDING_SYNC, 0)
                            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
                        }
                        sqliteDb.update(
                            AppDatabase.TABLE_USER_SETTINGS,
                            updateValues,
                            "user_id = ?",
                            arrayOf(userId)
                        )
                        Log.d(TAG, "User settings synced to Firebase successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing user settings to Firebase", e)
                    }
                }
            }
        } finally {
            pendingSettingsCursor.close()
        }

        // Sync pending user profile
        val pendingProfileCursor = sqliteDb.query(
            AppDatabase.TABLE_USER_PROFILE,
            null,
            "${AppDatabase.COLUMN_PENDING_SYNC} = ?",
            arrayOf("1"),
            null,
            null,
            null
        )

        try {
            while (pendingProfileCursor.moveToNext()) {
                val profileData = mapOf(
                    "courses" to pendingProfileCursor.getInt(pendingProfileCursor.getColumnIndexOrThrow("courses")),
                    "lectures" to pendingProfileCursor.getInt(pendingProfileCursor.getColumnIndexOrThrow("lectures")),
                    "smartDigests" to pendingProfileCursor.getInt(pendingProfileCursor.getColumnIndexOrThrow("smart_digests")),
                    "quizzes" to pendingProfileCursor.getInt(pendingProfileCursor.getColumnIndexOrThrow("quizzes")),
                    "groups" to pendingProfileCursor.getInt(pendingProfileCursor.getColumnIndexOrThrow("group_count")),
                    "timeSpent" to pendingProfileCursor.getString(pendingProfileCursor.getColumnIndexOrThrow("time_spent"))
                )

                usersRef?.let { ref ->
                    try {
                        ref.child(userId).child("profile").setValue(profileData).await()
                        // Mark as synced in SQLite
                        val updateValues = ContentValues().apply {
                            put(AppDatabase.COLUMN_PENDING_SYNC, 0)
                            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
                        }
                        sqliteDb.update(
                            AppDatabase.TABLE_USER_PROFILE,
                            updateValues,
                            "user_id = ?",
                            arrayOf(userId)
                        )
                        Log.d(TAG, "User profile synced to Firebase successfully")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing user profile to Firebase", e)
                    }
                }
            }
        } finally {
            pendingProfileCursor.close()
        }
    }

    private suspend fun syncFromFirebaseToLocal(userId: String) {
        Log.d(TAG, "Syncing from Firebase to local for user: $userId")
        // Sync users
        syncUsers(userId)
        // Sync courses
        syncCourses(userId)
        // Sync notes
        //syncNotes(userId)
        // Sync study groups
        //syncStudyGroups(userId)
        // Sync quizzes
        //syncQuizzes(userId)
    }

    private fun syncUsers(userId: String) {
        Log.d(TAG, "Starting user sync for: $userId")
        val userRef = databaseService.usersRef.child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "User data received from Firebase")
                    scope.launch(Dispatchers.IO) {
                        try {
                            val sqliteDb = db.writableDatabase
                            val values = ContentValues().apply {
                                put(AppDatabase.COLUMN_ID, userId)
                                put("email", snapshot.child("email").getValue(String::class.java) ?: "")
                                put("username", snapshot.child("username").getValue(String::class.java) ?: "")
                                put(AppDatabase.COLUMN_CREATED_AT, snapshot.child("createdAt").getValue(Long::class.java) ?: System.currentTimeMillis())
                                put("last_login", System.currentTimeMillis())
                                put("fcm_token", snapshot.child("FCMToken").getValue(String::class.java) ?: "")
                                put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
                                put(AppDatabase.COLUMN_PENDING_SYNC, 0)
                            }
                            sqliteDb.insertWithOnConflict(AppDatabase.TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
                            Log.d(TAG, "User data synced to SQLite")

                            // Sync user settings
                            val settingsSnapshot = snapshot.child("settings")
                            if (settingsSnapshot.exists()) {
                                Log.d(TAG, "Syncing user settings")
                                val settingsValues = ContentValues().apply {
                                    put("user_id", userId)
                                    put("quiz_notifications", if (settingsSnapshot.child("quizNotifications").getValue(Boolean::class.java) == true) 1 else 0)
                                    put("study_reminders", if (settingsSnapshot.child("studyReminders").getValue(Boolean::class.java) == true) 1 else 0)
                                    put("add_in_groups", if (settingsSnapshot.child("addInGroups").getValue(Boolean::class.java) == true) 1 else 0)
                                    put("auto_login", if (settingsSnapshot.child("autoLogin").getValue(Boolean::class.java) == true) 1 else 0)
                                    put("auto_sync", if (settingsSnapshot.child("autoSync").getValue(Boolean::class.java) == true) 1 else 0)
                                    put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
                                    put(AppDatabase.COLUMN_PENDING_SYNC, 0)
                                }
                                sqliteDb.insertWithOnConflict(AppDatabase.TABLE_USER_SETTINGS, null, settingsValues, SQLiteDatabase.CONFLICT_REPLACE)
                                Log.d(TAG, "User settings synced to SQLite")
                            }

                            // Sync user profile
                            val profileSnapshot = snapshot.child("profile")
                            if (profileSnapshot.exists()) {
                                Log.d(TAG, "Syncing user profile")
                                val profileValues = ContentValues().apply {
                                    put("user_id", userId)
                                    put("courses", profileSnapshot.child("courses").getValue(Int::class.java) ?: 0)
                                    put("lectures", profileSnapshot.child("lectures").getValue(Int::class.java) ?: 0)
                                    put("smart_digests", profileSnapshot.child("smartDigests").getValue(Int::class.java) ?: 0)
                                    put("quizzes", profileSnapshot.child("quizzes").getValue(Int::class.java) ?: 0)
                                    put("group_count", profileSnapshot.child("groups").getValue(Int::class.java) ?: 0)
                                    put("time_spent", profileSnapshot.child("timeSpent").getValue(String::class.java) ?: "00:00:00")
                                    put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
                                    put(AppDatabase.COLUMN_PENDING_SYNC, 0)
                                }
                                sqliteDb.insertWithOnConflict(AppDatabase.TABLE_USER_PROFILE, null, profileValues, SQLiteDatabase.CONFLICT_REPLACE)
                                Log.d(TAG, "User profile synced to SQLite")
                            }

                            logDatabaseState()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error syncing user data", e)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error syncing user data", error.toException())
            }
        }
        userRef.addValueEventListener(listener)
        listeners.add(listener)
    }

    private fun syncCourses(userId: String) {
        Log.d(TAG, "Starting course sync for user: $userId")
        val coursesRef = databaseService.coursesRef
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Received ${snapshot.childrenCount} courses from Firebase")
                scope.launch(Dispatchers.IO) {
                    try {
                        val sqliteDb = db.writableDatabase
                        sqliteDb.beginTransaction()
                        try {
                            // Clear existing courses
                            sqliteDb.delete(AppDatabase.TABLE_COURSES, null, null)
                            sqliteDb.delete(AppDatabase.TABLE_COURSE_MEMBERS, null, null)
                            Log.d(TAG, "Cleared existing courses from SQLite")

                            // Sync new courses
                            for (courseSnapshot in snapshot.children) {
                                val courseId = courseSnapshot.key ?: continue
                                val createdBy = courseSnapshot.child("createdBy").getValue(String::class.java)
                                val members = courseSnapshot.child("members").children

                                if (createdBy == userId || members.any { it.key == userId }) {
                                    Log.d(TAG, "Syncing course: $courseId")
                                    val values = ContentValues().apply {
                                        put(AppDatabase.COLUMN_ID, courseId)
                                        put("title", courseSnapshot.child("title").getValue(String::class.java) ?: "")
                                        put("description", courseSnapshot.child("description").getValue(String::class.java) ?: "")
                                        put("created_by", createdBy)
                                        put("color", courseSnapshot.child("color").getValue(Int::class.java) ?: 0)
                                        put("note_count", courseSnapshot.child("noteCount").getValue(Int::class.java) ?: 0)
                                        put("is_bookmarked", 0)
                                        put(AppDatabase.COLUMN_CREATED_AT, courseSnapshot.child("createdAt").getValue(Long::class.java) ?: System.currentTimeMillis())
                                        put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
                                        put(AppDatabase.COLUMN_PENDING_SYNC, 0)
                                    }
                                    sqliteDb.insertWithOnConflict(AppDatabase.TABLE_COURSES, null, values, SQLiteDatabase.CONFLICT_REPLACE)

                                    // Sync course members
                                    for (memberSnapshot in members) {
                                        val memberId = memberSnapshot.key ?: continue
                                        val memberValues = ContentValues().apply {
                                            put("course_id", courseId)
                                            put("user_id", memberId)
                                            put("last_modified", memberSnapshot.child("lastModified").getValue(Long::class.java) ?: System.currentTimeMillis())
                                            put(AppDatabase.COLUMN_PENDING_SYNC, 0)
                                        }
                                        sqliteDb.insertWithOnConflict(AppDatabase.TABLE_COURSE_MEMBERS, null, memberValues, SQLiteDatabase.CONFLICT_REPLACE)
                                    }
                                    Log.d(TAG, "Course $courseId synced successfully")
                                }
                            }
                            sqliteDb.setTransactionSuccessful()
                            Log.d(TAG, "All courses synced successfully")
                        } finally {
                            sqliteDb.endTransaction()
                        }
                        logDatabaseState()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing courses", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error syncing courses", error.toException())
            }
        }
        coursesRef.addValueEventListener(listener)
        listeners.add(listener)
    }

    // Similar implementations for syncNotes(), syncStudyGroups(), and syncQuizzes()
    // ... (implement these methods following the same pattern)

    fun stopSync() {
        listeners.forEach { listener ->
            databaseService.usersRef.removeEventListener(listener)
            databaseService.coursesRef.removeEventListener(listener)
            // Remove listeners for other references
        }
        listeners.clear()
    }

    suspend fun initialize() {
        Log.d(TAG, "Initializing FirebaseSyncManager")
        try {
            withContext(Dispatchers.IO) {
                // Remove persistence configuration since it's now in Application class
                database = FirebaseDatabase.getInstance()
                
                // Set up Firebase listeners
                setupFirebaseListeners()
                
                Log.d(TAG, "FirebaseSyncManager initialized successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing", e)
            throw e
        }
    }

    suspend fun syncCourse(course: Course) {
        Log.d(TAG, "Syncing single course: ${course.courseId}")
        withContext(Dispatchers.IO) {
            try {
                val userId = auth.currentUser?.uid ?: return@withContext
                coursesRef?.let { ref ->
                    val courseRef = ref.child(course.courseId)
                    
                    val courseData = mapOf(
                        "title" to course.title,
                        "description" to course.description,
                        "createdBy" to userId,
                        "color" to course.buttonColorResId,
                        "members" to mapOf(
                            userId to mapOf(
                                "lastModified" to System.currentTimeMillis()
                            )
                        )
                    )

                    courseRef.setValue(courseData)
                        .addOnSuccessListener {
                            Log.d(TAG, "Course ${course.courseId} synced to Firebase successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error syncing course to Firebase", e)
                        }
                } ?: run {
                    Log.e(TAG, "Courses reference is null, cannot sync course")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing course", e)
                throw e
            }
        }
    }

    suspend fun syncCoursesFromFirebase(userId: String) {
        Log.d(TAG, "Starting one-time course sync from Firebase for user: $userId")
        withContext(Dispatchers.IO) {
            try {
                usersRef?.let { ref ->
                    val userCoursesRef = ref.child(userId).child("courses")
                    userCoursesRef.get()
                        .addOnSuccessListener { snapshot ->
                            Log.d(TAG, "Received ${snapshot.childrenCount} courses from Firebase")
                            snapshot.children.forEach { courseSnapshot ->
                                val courseId = courseSnapshot.key ?: return@forEach
                                val course = courseSnapshot.getValue(Course::class.java)
                                if (course != null) {
                                    val userLocalDao = UserLocalDao(db.writableDatabase)
                                    userLocalDao.insertCourse(userId, course)
                                    Log.d(TAG, "Course $courseId synced to SQLite")
                                }
                            }
                            logDatabaseState()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error syncing courses from Firebase", e)
                        }
                } ?: run {
                    Log.e(TAG, "Users reference is null, cannot sync courses")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing courses from Firebase", e)
                throw e
            }
        }
    }

    fun cleanup() {
        Log.d(TAG, "Cleaning up FirebaseSyncManager")
        try {
            database?.goOffline()
            Log.d(TAG, "Firebase went offline")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
} 
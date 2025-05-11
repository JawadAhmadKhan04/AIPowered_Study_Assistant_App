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
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBWriteOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.dao.UserLocalDao
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import kotlinx.coroutines.withContext
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBReadOperations
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseSyncManager(private val db: AppDatabase) {
    private val TAG = "FirebaseSyncManager"
    private val databaseService = FBDataBaseService()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val listeners = mutableListOf<ValueEventListener>()
    private val auth = FirebaseAuth.getInstance()
    private var database: FirebaseDatabase? = null
    private var usersRef: DatabaseReference? = null
    private var coursesRef: DatabaseReference? = null
    private val userLocalDao = UserLocalDao(db.writableDatabase)
    private val fbReadOps = FBReadOperations(databaseService)
    private val context: Context = db.context

    private fun getContext(): Context {
        return context
    }

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

        // Sync pending notes
        val pendingNotes = userLocalDao.getPendingSyncNotes()
        val fbWriteOps = FBWriteOperations(databaseService)
        
        for (note in pendingNotes) {
            try {
                // Get note content and tag from SQLite
                val noteCursor = sqliteDb.query(
                    AppDatabase.TABLE_NOTES,
                    arrayOf("content", "type"),
                    "${AppDatabase.COLUMN_ID} = ?",
                    arrayOf(note.note_id),
                    null,
                    null,
                    null
                )
                
                val tagCursor = sqliteDb.query(
                    AppDatabase.TABLE_NOTE_TAGS,
                    arrayOf("tag"),
                    "note_id = ?",
                    arrayOf(note.note_id),
                    null,
                    null,
                    null
                )

                if (noteCursor.moveToFirst() && tagCursor.moveToFirst()) {
                    val content = noteCursor.getString(noteCursor.getColumnIndexOrThrow("content"))
                    val type = noteCursor.getString(noteCursor.getColumnIndexOrThrow("type"))
                    val tag = tagCursor.getInt(tagCursor.getColumnIndexOrThrow("tag"))

                    // Get course ID for the note
                    val courseIdCursor = sqliteDb.query(
                        AppDatabase.TABLE_NOTES,
                        arrayOf("course_id"),
                        "${AppDatabase.COLUMN_ID} = ?",
                        arrayOf(note.note_id),
                        null,
                        null,
                        null
                    )

                    if (courseIdCursor.moveToFirst()) {
                        val courseId = courseIdCursor.getString(courseIdCursor.getColumnIndexOrThrow("course_id"))
                        
                        // Save note to Firebase
                        fbWriteOps.saveNotes(
                            courseId = courseId,
                            noteTitle = note.title,
                            noteContent = content,
                            type = type,
                            tag = tag
                        )

                        // Mark note as synchronized
                        userLocalDao.markNoteSynchronized(note.note_id)
                        Log.d(TAG, "Note ${note.note_id} synced to Firebase successfully")
                    }
                    courseIdCursor.close()
                }
                noteCursor.close()
                tagCursor.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing note ${note.note_id} to Firebase", e)
            }
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
        syncNotes(userId)

        // Sync study groups
        //syncStudyGroups(userId)
        // Sync quizzes
        //syncQuizzes(userId)
        // Sync bookmarks
        syncBookmarks(userId)
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

    suspend fun syncNotes(userId: String, courseId: String? = null) {
        Log.d(TAG, "Syncing notes from Firebase for user: $userId${courseId?.let { ", course: $it" } ?: ""}")
        try {
            val fbReadOps = FBReadOperations(databaseService)
            val sqliteDb = db.writableDatabase
            
            // Get courses to sync - either all user courses or just the specified course
            val coursesToSync = if (courseId != null) {
                listOf(userLocalDao.getCourseById(courseId) ?: return)
            } else {
                userLocalDao.getCoursesByUserId(userId)
            }
            
            for (course in coursesToSync) {
                fbReadOps.getNotes(course.courseId) { textNotes, voiceNotes ->
                    sqliteDb.beginTransaction()
                    try {
                        // Combine text and voice notes
                        val allNotes = textNotes + voiceNotes
                        
                        for (note in allNotes) {
                            // Get note details from Firebase
                            fbReadOps.getDigest(note.note_id) { content, audio, type, summary, tag, keyPoints, conceptList ->
                                // Insert or update note in SQLite
                                val noteValues = ContentValues().apply {
                                    put(AppDatabase.COLUMN_ID, note.note_id)
                                    put("course_id", course.courseId)
                                    put("title", note.title)
                                    put("content", content)
                                    put("audio", audio)
                                    put("type", type)
                                    put("created_by", userId)
                                    put(AppDatabase.COLUMN_CREATED_AT, note.createdAt)
                                    put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
                                    put("summary", summary)
                                    put(AppDatabase.COLUMN_PENDING_SYNC, 0)
                                }

                                sqliteDb.insertWithOnConflict(
                                    AppDatabase.TABLE_NOTES,
                                    null,
                                    noteValues,
                                    SQLiteDatabase.CONFLICT_REPLACE
                                )

                                // Insert or update note tag
                                val tagValues = ContentValues().apply {
                                    put("note_id", note.note_id)
                                    put("tag", tag)
                                    put(AppDatabase.COLUMN_PENDING_SYNC, 0)
                                }
                                sqliteDb.insertWithOnConflict(
                                    AppDatabase.TABLE_NOTE_TAGS,
                                    null,
                                    tagValues,
                                    SQLiteDatabase.CONFLICT_REPLACE
                                )

                                // Insert or update key points
                                if (keyPoints.isNotEmpty()) {
                                    val keyPointsList = keyPoints.split(",")
                                    for (keyPoint in keyPointsList) {
                                        val keyPointValues = ContentValues().apply {
                                            put("note_id", note.note_id)
                                            put("key_point", keyPoint.trim())
                                            put(AppDatabase.COLUMN_PENDING_SYNC, 0)
                                        }
                                        sqliteDb.insertWithOnConflict(
                                            AppDatabase.TABLE_NOTE_KEY_POINTS,
                                            null,
                                            keyPointValues,
                                            SQLiteDatabase.CONFLICT_REPLACE
                                        )
                                    }
                                }

                                // Insert or update concepts
                                if (conceptList.isNotEmpty()) {
                                    val conceptsList = conceptList.split(",")
                                    for (concept in conceptsList) {
                                        val conceptValues = ContentValues().apply {
                                            put("note_id", note.note_id)
                                            put("concept", concept.trim())
                                            put(AppDatabase.COLUMN_PENDING_SYNC, 0)
                                        }
                                        sqliteDb.insertWithOnConflict(
                                            AppDatabase.TABLE_NOTE_CONCEPTS,
                                            null,
                                            conceptValues,
                                            SQLiteDatabase.CONFLICT_REPLACE
                                        )
                                    }
                                }
                            }
                        }
                        sqliteDb.setTransactionSuccessful()
                        Log.d(TAG, "Successfully synced notes from Firebase for course: ${course.courseId}")
                    } finally {
                        sqliteDb.endTransaction()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing notes from Firebase", e)
            throw e
        }
    }

    private fun syncBookmarks(userId: String) {
        Log.d(TAG, "Syncing bookmarks for user: $userId")
        try {
            // Get all pending bookmarks from SQLite
            val pendingBookmarks = userLocalDao.getPendingSyncBookmarks()
            
            // Create FBWriteOperations instance
            val fbWriteOps = FBWriteOperations(databaseService)
            
            // Sync each bookmark to Firebase
            pendingBookmarks.forEach { (userId, courseId) ->
                // Check if the bookmark exists in SQLite
                val isBookmarked = userLocalDao.isCourseBookmarked(userId, courseId)
                
                // Use FBWriteOperations to update Firebase
                fbWriteOps.bookmark_course(courseId, isBookmarked)
                
                // Mark as synchronized in SQLite
                userLocalDao.markBookmarkSynchronized(userId, courseId)
            }

            // Also sync bookmarks from Firebase to ensure consistency
            //syncBookmarksFromFirebase(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing bookmarks", e)
        }
    }

    private fun syncBookmarksFromFirebase(userId: String) {
        Log.d(TAG, "Syncing bookmarks from Firebase for user: $userId")
        try {
            val bookmarksRef = databaseService.usersRef.child(userId).child("bookmarks")
            bookmarksRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Get all bookmarked course IDs from Firebase
                    val firebaseBookmarkedCourseIds = mutableSetOf<String>()
                    snapshot.children.forEach { bookmarkSnapshot ->
                        val courseId = bookmarkSnapshot.key ?: return@forEach
                        val isBookmarked = bookmarkSnapshot.getValue(Boolean::class.java) ?: false
                        if (isBookmarked) {
                            firebaseBookmarkedCourseIds.add(courseId)
                        }
                    }

                    // Get all bookmarked course IDs from SQLite
                    val sqliteBookmarkedCourseIds = userLocalDao.getBookmarksByUserId(userId)
                        .map { it.courseId }
                        .toSet()

                    // Add bookmarks that exist in Firebase but not in SQLite
                    firebaseBookmarkedCourseIds.forEach { courseId ->
                        if (!sqliteBookmarkedCourseIds.contains(courseId)) {
                            userLocalDao.toggleBookmark(userId, courseId, true)
                        }
                    }

                    // Remove bookmarks that exist in SQLite but not in Firebase
                    sqliteBookmarkedCourseIds.forEach { courseId ->
                        if (!firebaseBookmarkedCourseIds.contains(courseId)) {
                            userLocalDao.toggleBookmark(userId, courseId, false)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error syncing bookmarks from Firebase", error.toException())
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing bookmarks from Firebase", e)
        }
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

    suspend fun syncSingleNoteFromFirebase(userId: String, noteId: String) {
        try {
            // Get the note data from Firebase
            val noteData = suspendCoroutine<Map<String, Any>> { continuation ->
                fbReadOps.getDigest(noteId) { content, audio, type, summary, tag, keyPoints, conceptList ->
                    val data = mapOf(
                        "content" to content,
                        "audio" to audio,
                        "type" to type,
                        "summary" to summary,
                        "tag" to tag,
                        "keyPoints" to keyPoints,
                        "conceptList" to conceptList
                    )
                    continuation.resume(data)
                }
            }

            // Update the local database
            val db = AppDatabase.getInstance(context).writableDatabase
            
            // Start a transaction for all updates
            db.beginTransaction()
            try {
                // Update the main note (without tag)
                val noteValues = ContentValues().apply {
                    put("content", noteData["content"] as String)
                    put("audio", noteData["audio"] as String)
                    put("type", noteData["type"] as String)
                    put("summary", noteData["summary"] as String)
                    put(AppDatabase.COLUMN_PENDING_SYNC, 0)
                }

                // Update the main note
                db.update(
                    AppDatabase.TABLE_NOTES,
                    noteValues,
                    "${AppDatabase.COLUMN_ID} = ?",
                    arrayOf(noteId)
                )

                // Update tag in the note_tags table
                val tag = noteData["tag"] as Int
                // First delete existing tag
                db.delete(
                    AppDatabase.TABLE_NOTE_TAGS,
                    "note_id = ?",
                    arrayOf(noteId)
                )
                // Then insert the new tag
                val tagValues = ContentValues().apply {
                    put("note_id", noteId)
                    put("tag", tag)
                    put(AppDatabase.COLUMN_PENDING_SYNC, 0)
                }
                db.insert(AppDatabase.TABLE_NOTE_TAGS, null, tagValues)

                // Update key points
                val keyPoints = (noteData["keyPoints"] as String).split(",").filter { it.isNotEmpty() }
                db.delete(
                    AppDatabase.TABLE_NOTE_KEY_POINTS,
                    "note_id = ?",
                    arrayOf(noteId)
                )
                keyPoints.forEach { keyPoint ->
                    val keyPointValues = ContentValues().apply {
                        put("note_id", noteId)
                        put("key_point", keyPoint.trim())
                        put(AppDatabase.COLUMN_PENDING_SYNC, 0)
                    }
                    db.insert(AppDatabase.TABLE_NOTE_KEY_POINTS, null, keyPointValues)
                }

                // Update concepts
                val concepts = (noteData["conceptList"] as String).split(",").filter { it.isNotEmpty() }
                db.delete(
                    AppDatabase.TABLE_NOTE_CONCEPTS,
                    "note_id = ?",
                    arrayOf(noteId)
                )
                concepts.forEach { concept ->
                    val conceptValues = ContentValues().apply {
                        put("note_id", noteId)
                        put("concept", concept.trim())
                        put(AppDatabase.COLUMN_PENDING_SYNC, 0)
                    }
                    db.insert(AppDatabase.TABLE_NOTE_CONCEPTS, null, conceptValues)
                }
                
                // Mark the transaction as successful
                db.setTransactionSuccessful()
                Log.d(TAG, "Successfully synced note $noteId from Firebase")
            } finally {
                // End the transaction
                db.endTransaction()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing note $noteId from Firebase", e)
            throw e
        }
    }
} 
package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.dao.UserLocalDao
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.UserProfile
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.NoteItem
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.StudyGroup
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBWriteOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.GroupMessage
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.MessageType
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBReadOperations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
    private val fbDatabaseService = FBDataBaseService(context)
    private val fbWriteOperations = FBWriteOperations(fbDatabaseService)
    private val fbReadOps = FBReadOperations(fbDatabaseService)
    
    // Current user
    var currentUser: UserProfile? = null
        private set

    @Volatile
    private var _isInitialized = false
    val isInitialized: Boolean
        get() = _isInitialized

    private val initializationLock = Any()
    
    suspend fun initialize() {
        if (_isInitialized) {
            Log.d(TAG, "Data manager already initialized")
            return
        }

        // Use double-checked locking pattern without suspend functions inside synchronized block
        synchronized(initializationLock) {
            if (_isInitialized) {
                Log.d(TAG, "Data manager already initialized (double-check)")
                return
            }
        }

        try {
            Log.d(TAG, "Starting data manager initialization")
            
            // Start network monitoring
            networkMonitor.startMonitoring()
            
            // Initialize Firebase sync
            firebaseSyncManager.initialize()
            
            // Load current user if authenticated
            auth.currentUser?.let { firebaseUser ->
                try {
                    val userProfile = userLocalDao.getUserById(firebaseUser.uid)
                    if (userProfile != null) {
                        currentUser = userProfile
                        Log.d(TAG, "Current user loaded: ${userProfile.username}")
                    } else {
                        Log.w(TAG, "User profile not found in local database")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading current user", e)
                }
            }
            
            synchronized(initializationLock) {
                _isInitialized = true
            }
            Log.d(TAG, "Data manager initialization completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during data manager initialization", e)
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
                
                // Also sync any pending messages
                firebaseSyncManager.syncPendingMessages()
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

                // If online, trigger sync
                if (networkMonitor.isOnline.first()) {
                    Log.d(TAG, "Network available, triggering sync")
                    firebaseSyncManager.startSync(userId)
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

    suspend fun toggleBookmark(userId: String, courseId: String, isBookmarked: Boolean) {
        if (!isInitialized) {
            Log.e(TAG, "Cannot toggle bookmark: manager not initialized")
            throw IllegalStateException("OfflineFirstDataManager not initialized")
        }

        Log.d(TAG, "Toggling bookmark for course: $courseId, isBookmarked: $isBookmarked")
        try {
            withContext(Dispatchers.IO) {
                // Update in SQLite
                userLocalDao.toggleBookmark(userId, courseId, isBookmarked)
                Log.d(TAG, "Bookmark updated in SQLite")

                // If online, trigger sync
                if (networkMonitor.isOnline.first()) {
                    Log.d(TAG, "Network available, triggering sync")
                    firebaseSyncManager.startSync(userId)
                } else {
                    Log.d(TAG, "Network unavailable, bookmark will be synced when online")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling bookmark", e)
            throw e
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
                _isInitialized = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
            throw e
        }
    }
    
    suspend fun getNotes(courseId: String): List<NoteItem> {
        if (!isInitialized) {
            Log.e(TAG, "Cannot get notes: manager not initialized")
            throw IllegalStateException("OfflineFirstDataManager not initialized")
        }

        Log.d(TAG, "Getting notes for course: $courseId")
        return withContext(Dispatchers.IO) {
            try {
                // First try to get from SQLite
                val notes = userLocalDao.getNotesByCourseId(courseId)
                Log.d(TAG, "Retrieved ${notes.size} notes from SQLite")

                // If online, sync with Firebase
                if (networkMonitor.isOnline.first()) {
                    Log.d(TAG, "Network available, syncing with Firebase")
                    val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
                    firebaseSyncManager.syncNotes(userId, courseId)
                } else {
                    Log.d(TAG, "Network unavailable, using local data only")
                }

                notes
            } catch (e: Exception) {
                Log.e(TAG, "Error getting notes", e)
                throw e
            }
        }
    }

    suspend fun saveNote(
        courseId: String,
        title: String,
        content: String,
        type: String,
        tag: Int
    ) {
        if (!isInitialized) {
            Log.e(TAG, "Cannot save note: manager not initialized")
            throw IllegalStateException("OfflineFirstDataManager not initialized")
        }

        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        Log.d(TAG, "Saving note for course: $courseId")

        try {
            withContext(Dispatchers.IO) {
                // Create note item
                val note = NoteItem(
                    title = title,
                    createdAt = System.currentTimeMillis(),
                    age = "Just now",
                    type = type,
                    note_id = "" // Will be set by the DAO
                )

                // Save to SQLite
                userLocalDao.insertNote(courseId, note, content, tag)
                Log.d(TAG, "Note saved to SQLite")

                // If online, trigger sync
                if (networkMonitor.isOnline.first()) {
                    Log.d(TAG, "Network available, triggering sync")
                    firebaseSyncManager.startSync(userId)
                } else {
                    Log.d(TAG, "Network unavailable, note will be synced when online")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving note", e)
            throw e
        }
    }

    suspend fun updateNote(
        noteId: String,
        content: String,
        type: String,
        tag: Int
    ) {
        if (!isInitialized) {
            Log.e(TAG, "Cannot update note: manager not initialized")
            throw IllegalStateException("OfflineFirstDataManager not initialized")
        }

        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        Log.d(TAG, "Updating note: $noteId")

        try {
            withContext(Dispatchers.IO) {
                // Start a transaction
                db.writableDatabase.beginTransaction()
                try {
                    // Update main note content
                    val noteValues = ContentValues().apply {
                        put("content", content)
                        put("type", type.lowercase())
                        put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
                        put(AppDatabase.COLUMN_PENDING_SYNC, 1)
                    }

                    db.writableDatabase.update(
                        AppDatabase.TABLE_NOTES,
                        noteValues,
                        "${AppDatabase.COLUMN_ID} = ?",
                        arrayOf(noteId)
                    )

                    // Update tag in note_tags table
                    // First delete existing tag
                    db.writableDatabase.delete(
                        AppDatabase.TABLE_NOTE_TAGS,
                        "note_id = ?",
                        arrayOf(noteId)
                    )
                    
                    // Then insert new tag
                    val tagValues = ContentValues().apply {
                        put("note_id", noteId)
                        put("tag", tag)
                        put(AppDatabase.COLUMN_PENDING_SYNC, 1)
                    }
                    db.writableDatabase.insert(AppDatabase.TABLE_NOTE_TAGS, null, tagValues)
                    
                    // Mark transaction as successful
                    db.writableDatabase.setTransactionSuccessful()
                } finally {
                    // End transaction
                    db.writableDatabase.endTransaction()
                }

                // If online, trigger sync
                if (networkMonitor.isOnline.first()) {
                    Log.d(TAG, "Network available, triggering sync")
                    firebaseSyncManager.startSync(userId)
                } else {
                    Log.d(TAG, "Network unavailable, note will be synced when online")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating note", e)
            throw e
        }
    }

    data class NoteDigest(
        val content: String,
        val audio: String,
        val type: String,
        val summary: String,
        val tag: Int,
        val keyPoints: String,
        val conceptList: String
    )

    suspend fun getNoteDigest(noteId: String): NoteDigest {
        return withContext(Dispatchers.IO) {
            try {
                // Check if we're online and sync if needed
                if (networkMonitor.isOnline.first()) {
                    val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
                    firebaseSyncManager.syncSingleNoteFromFirebase(userId, noteId)
                }

                // Read from local database
                val db = AppDatabase.getInstance(context).readableDatabase
                
                // Get main note data
                val cursor = db.query(
                    AppDatabase.TABLE_NOTES,
                    arrayOf("content", "audio", "type", "summary"),
                    "${AppDatabase.COLUMN_ID} = ?",
                    arrayOf(noteId),
                    null,
                    null,
                    null
                )

                // Get tag from note_tags table
                val tagCursor = db.query(
                    AppDatabase.TABLE_NOTE_TAGS,
                    arrayOf("tag"),
                    "note_id = ?",
                    arrayOf(noteId),
                    null,
                    null,
                    null
                )

                // Get key points
                val keyPointsCursor = db.query(
                    AppDatabase.TABLE_NOTE_KEY_POINTS,
                    arrayOf("key_point"),
                    "note_id = ?",
                    arrayOf(noteId),
                    null,
                    null,
                    null
                )

                // Get concepts
                val conceptsCursor = db.query(
                    AppDatabase.TABLE_NOTE_CONCEPTS,
                    arrayOf("concept"),
                    "note_id = ?",
                    arrayOf(noteId),
                    null,
                    null,
                    null
                )

                cursor.use { mainCursor ->
                    if (mainCursor.moveToFirst()) {
                        val content = mainCursor.getString(mainCursor.getColumnIndexOrThrow("content")) ?: ""
                        val audio = mainCursor.getString(mainCursor.getColumnIndexOrThrow("audio")) ?: ""
                        val type = mainCursor.getString(mainCursor.getColumnIndexOrThrow("type")) ?: ""
                        val summary = mainCursor.getString(mainCursor.getColumnIndexOrThrow("summary")) ?: ""
                        
                        // Get tag from tag cursor
                        val tag = tagCursor.use { tCursor ->
                            if (tCursor.moveToFirst()) {
                                tCursor.getInt(tCursor.getColumnIndexOrThrow("tag"))
                            } else {
                                0 // Default tag value if not found
                            }
                        }

                        // Get key points
                        val keyPoints = keyPointsCursor.use { kpCursor ->
                            val points = mutableListOf<String>()
                            while (kpCursor.moveToNext()) {
                                points.add(kpCursor.getString(kpCursor.getColumnIndexOrThrow("key_point")))
                            }
                            points.joinToString("\n")
                        }

                        // Get concepts
                        val conceptList = conceptsCursor.use { cCursor ->
                            val concepts = mutableListOf<String>()
                            while (cCursor.moveToNext()) {
                                concepts.add(cCursor.getString(cCursor.getColumnIndexOrThrow("concept")))
                            }
                            concepts.joinToString("\n")
                        }

                        NoteDigest(
                            content = content,
                            audio = audio,
                            type = type,
                            summary = summary,
                            tag = tag,
                            keyPoints = keyPoints,
                            conceptList = conceptList
                        )
                    } else {
                        NoteDigest("", "", "", "", 0, "", "")
                    }
                }
            } catch (e: Exception) {
                Log.e("OfflineFirstDataManager", "Error getting note digest", e)
                NoteDigest("", "", "", "", 0, "", "")
            }
        }
    }
    
    suspend fun getStudyGroups(userId: String): List<StudyGroup> {
        if (!isInitialized) {
            Log.e(TAG, "Cannot get study groups: manager not initialized")
            throw IllegalStateException("OfflineFirstDataManager not initialized")
        }

        Log.d(TAG, "Getting study groups for user: $userId")
        return withContext(Dispatchers.IO) {
            try {
                // First try to get from SQLite
                val groups = getStudyGroupsFromLocal(userId)
                Log.d(TAG, "Retrieved ${groups.size} study groups from SQLite")

                // If online, sync with Firebase
                if (networkMonitor.isOnline.first()) {
                    Log.d(TAG, "Network available, syncing with Firebase")
                    syncStudyGroupsFromFirebase(userId)
                    // Get updated groups after sync
                    val updatedGroups = getStudyGroupsFromLocal(userId)
                    return@withContext updatedGroups
                } else {
                    Log.d(TAG, "Network unavailable, using local data only")
                    return@withContext groups
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting study groups", e)
                throw e
            }
        }
    }

    private fun getStudyGroupsFromLocal(userId: String): List<StudyGroup> {
        val sqliteDb = db.readableDatabase
        val groups = mutableListOf<StudyGroup>()

        // Query study groups where the user is a member
        val cursor = sqliteDb.rawQuery("""
            SELECT g.*, gm.role as user_role
            FROM ${AppDatabase.TABLE_STUDY_GROUPS} g
            INNER JOIN ${AppDatabase.TABLE_GROUP_MEMBERS} gm
            ON g.${AppDatabase.COLUMN_ID} = gm.group_id
            WHERE gm.user_id = ?
        """, arrayOf(userId))

        cursor.use { c ->
            while (c.moveToNext()) {
                val groupId = c.getString(c.getColumnIndexOrThrow(AppDatabase.COLUMN_ID))
                val name = c.getString(c.getColumnIndexOrThrow(AppDatabase.COLUMN_NAME))
                val description = c.getString(c.getColumnIndexOrThrow("description"))
                val createdBy = c.getString(c.getColumnIndexOrThrow("created_by"))
                val createdAt = c.getLong(c.getColumnIndexOrThrow(AppDatabase.COLUMN_CREATED_AT))
                val code = c.getString(c.getColumnIndexOrThrow("code"))
                val userRole = c.getString(c.getColumnIndexOrThrow("user_role"))

                // Get member count
                val memberCountCursor = sqliteDb.rawQuery(
                    "SELECT COUNT(*) FROM ${AppDatabase.TABLE_GROUP_MEMBERS} WHERE group_id = ?",
                    arrayOf(groupId)
                )
                val memberCount = if (memberCountCursor.moveToFirst()) {
                    memberCountCursor.getInt(0)
                } else {
                    0
                }
                memberCountCursor.close()

                val group = StudyGroup(
                    id = groupId,
                    name = name,
                    description = description,
                    createdBy = createdBy,
                    createdAt = createdAt,
                    code = code,
                    memberCount = memberCount,
                    userRole = userRole
                )
                groups.add(group)
            }
        }

        return groups
    }

    private suspend fun syncStudyGroupsFromFirebase(userId: String) {
        Log.d(TAG, "Syncing study groups from Firebase for user: $userId")
        try {
            // Call Firebase sync manager to sync study groups
            firebaseSyncManager.syncStudyGroups(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing study groups from Firebase", e)
            throw e
        }
    }

    suspend fun createStudyGroup(name: String, description: String): String? {
        if (!isInitialized) {
            Log.e(TAG, "Cannot create study group: manager not initialized")
            throw IllegalStateException("OfflineFirstDataManager not initialized")
        }

        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        Log.d(TAG, "Creating study group: $name for user: $userId")
        Log.d("Checkr", "Creating study group: $name for user: $userId")
        return withContext(Dispatchers.IO) {
            try {
                // Use suspendCoroutine to convert callback to suspend function
                val groupId = suspendCoroutine<String?> { continuation ->
                    Log.d("Checkr", "Hi: $name for user: $userId")
                    fbWriteOperations.createStudyGroup(userId, name, description) { id ->
                        continuation.resume(id)
                    }
                }

                if (groupId != null) {
                    // Group created successfully in Firebase, now sync to local
                    firebaseSyncManager.syncStudyGroups(userId)
                    return@withContext groupId
                } else {
                    Log.e(TAG, "Failed to create study group in Firebase")
                    return@withContext null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating study group", e)
                throw e
            }
        }
    }

    suspend fun joinStudyGroup(groupCode: String): Boolean {
        if (!isInitialized) {
            Log.e(TAG, "Cannot join study group: manager not initialized")
            throw IllegalStateException("OfflineFirstDataManager not initialized")
        }

        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        Log.d(TAG, "Joining study group with code: $groupCode for user: $userId")

        return withContext(Dispatchers.IO) {
            try {
                // Use suspendCoroutine to convert callback to suspend function
                val success = suspendCoroutine<Boolean> { continuation ->
                    fbWriteOperations.joinStudyGroup(groupCode) { result ->
                        continuation.resume(result)
                    }
                }

                if (success) {
                    // Group joined successfully in Firebase, now sync to local
                    firebaseSyncManager.syncStudyGroups(userId)
                    return@withContext true
                } else {
                    Log.e(TAG, "Failed to join study group in Firebase")
                    return@withContext false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error joining study group", e)
                throw e
            }
        }
    }
    
    suspend fun getGroupMessages(groupId: String): List<GroupMessage> {
        if (!isInitialized) {
            Log.e(TAG, "Cannot get group messages: manager not initialized")
            throw IllegalStateException("OfflineFirstDataManager not initialized")
        }

        Log.d(TAG, "Getting messages for group: $groupId")
        return withContext(Dispatchers.IO) {
            try {
                // First try to get from SQLite
                val messages = getGroupMessagesFromLocal(groupId)
                Log.d(TAG, "Retrieved ${messages.size} messages from SQLite")

                // If online, sync with Firebase
                if (networkMonitor.isOnline.first()) {
                    Log.d(TAG, "Network available, syncing with Firebase")
                    firebaseSyncManager.syncGroupMessages(groupId, db.writableDatabase)
                    // Get updated messages after sync
                    val updatedMessages = getGroupMessagesFromLocal(groupId)
                    return@withContext updatedMessages
                } else {
                    Log.d(TAG, "Network unavailable, using local data only")
                    return@withContext messages
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting group messages", e)
                throw e
            }
        }
    }

    private fun getGroupMessagesFromLocal(groupId: String): List<GroupMessage> {
        val sqliteDb = db.readableDatabase
        val messages = mutableListOf<GroupMessage>()
        val userId = auth.currentUser?.uid ?: return messages

        try {
            // Query the group_chats table
            val cursor = sqliteDb.query(
                AppDatabase.TABLE_GROUP_CHATS,
                arrayOf("messages"),
                "group_id = ?",
                arrayOf(groupId),
                null, null, null
            )

            if (cursor.moveToFirst()) {
                val messagesJson = cursor.getString(cursor.getColumnIndexOrThrow("messages"))
                
                // Parse JSON array of messages
                val jsonArray = org.json.JSONArray(messagesJson)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    
                    val messageId = jsonObject.getString("id")
                    val senderId = jsonObject.getString("senderId")
                    val senderName = jsonObject.getString("senderName")
                    val content = jsonObject.getString("content")
                    val timestamp = jsonObject.getLong("timestamp")
                    val messageType = MessageType.valueOf(jsonObject.getString("messageType"))
                    val noteId = jsonObject.optString("noteId", "")
                    val noteType = jsonObject.optString("noteType", "")
                    
                    val message = GroupMessage(
                        id = messageId,
                        groupId = groupId,
                        senderId = senderId,
                        senderName = senderName,
                        content = content,
                        timestamp = timestamp,
                        isCurrentUser = senderId == userId,
                        messageType = messageType,
                        noteId = noteId,
                        noteType = noteType
                    )
                    messages.add(message)
                }
                
                // Sort messages by timestamp
                messages.sortBy { it.timestamp }
            }
            cursor.close()
            
            return messages
        } catch (e: Exception) {
            Log.e(TAG, "Error getting group messages from local database", e)
            return emptyList()
        }
    }

    suspend fun getGroupDetails(groupId: String): StudyGroup? {
        if (!isInitialized) {
            Log.e(TAG, "Cannot get group details: manager not initialized")
            throw IllegalStateException("OfflineFirstDataManager not initialized")
        }

        Log.d(TAG, "Getting details for group: $groupId")
        return withContext(Dispatchers.IO) {
            try {
                // First try to get from SQLite
                val group = getGroupDetailsFromLocal(groupId)
                
                // If online and group not found, sync with Firebase
                if (group == null && networkMonitor.isOnline.first()) {
                    Log.d(TAG, "Group not found locally, syncing with Firebase")
                    val userId = auth.currentUser?.uid ?: return@withContext null
                    firebaseSyncManager.syncStudyGroups(userId)
                    return@withContext getGroupDetailsFromLocal(groupId)
                }
                
                return@withContext group
            } catch (e: Exception) {
                Log.e(TAG, "Error getting group details", e)
                throw e
            }
        }
    }

    private fun getGroupDetailsFromLocal(groupId: String): StudyGroup? {
        val sqliteDb = db.readableDatabase
        val userId = auth.currentUser?.uid ?: return null
        
        val cursor = sqliteDb.rawQuery("""
            SELECT g.*, gm.role as user_role
            FROM ${AppDatabase.TABLE_STUDY_GROUPS} g
            INNER JOIN ${AppDatabase.TABLE_GROUP_MEMBERS} gm
            ON g.${AppDatabase.COLUMN_ID} = gm.group_id
            WHERE g.${AppDatabase.COLUMN_ID} = ? AND gm.user_id = ?
        """, arrayOf(groupId, userId))
        
        return cursor.use { c ->
            if (c.moveToFirst()) {
                val name = c.getString(c.getColumnIndexOrThrow(AppDatabase.COLUMN_NAME))
                val description = c.getString(c.getColumnIndexOrThrow("description"))
                val createdBy = c.getString(c.getColumnIndexOrThrow("created_by"))
                val createdAt = c.getLong(c.getColumnIndexOrThrow(AppDatabase.COLUMN_CREATED_AT))
                val code = c.getString(c.getColumnIndexOrThrow("code"))
                val userRole = c.getString(c.getColumnIndexOrThrow("user_role"))

                // Get member count
                val memberCountCursor = sqliteDb.rawQuery(
                    "SELECT COUNT(*) FROM ${AppDatabase.TABLE_GROUP_MEMBERS} WHERE group_id = ?",
                    arrayOf(groupId)
                )
                val memberCount = if (memberCountCursor.moveToFirst()) {
                    memberCountCursor.getInt(0)
                } else {
                    0
                }
                memberCountCursor.close()

                StudyGroup(
                    id = groupId,
                    name = name,
                    description = description,
                    createdBy = createdBy,
                    createdAt = createdAt,
                    code = code,
                    memberCount = memberCount,
                    userRole = userRole
                )
            } else {
                null
            }
        }
    }

    suspend fun sendGroupMessage(groupId: String, message: GroupMessage): Boolean {
        if (!isInitialized) {
            Log.e(TAG, "Cannot send message: manager not initialized")
            throw IllegalStateException("OfflineFirstDataManager not initialized")
        }

        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
        Log.d(TAG, "Sending message to group: $groupId")

        return withContext(Dispatchers.IO) {
            try {
                // Generate a message ID if not present
                val messageId = if (message.id.isEmpty()) {
                    "msg_${System.currentTimeMillis()}_${(0..1000).random()}"
                } else {
                    message.id
                }
                
                // Create a complete message with ID
                val completeMessage = message.copy(
                    id = messageId,
                    groupId = groupId,
                    isCurrentUser = true
                )
                
                // Check if we're online
                val isOnline = try {
                    val online = networkMonitor.isOnline.first()
                    Log.d(TAG, "Network status check: ${if (online) "Online" else "Offline"}")
                    online
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking network status", e)
                    false
                }
                
                if (isOnline) {
                    Log.d(TAG, "Online, sending message directly to Firebase")
                    
                    // Send to Firebase
                    try {
                        val success = suspendCoroutine<Boolean> { continuation ->
                            var isCompleted = false
                            fbWriteOperations.sendGroupMessage(groupId, completeMessage) { result ->
                                if (!isCompleted) {
                                    isCompleted = true
                                    continuation.resume(result)
                                }
                            }
                        }
                        
                        if (success) {
                            Log.d(TAG, "Message sent to Firebase successfully")
                            // Sync from Firebase to ensure consistency
                            firebaseSyncManager.syncGroupMessages(groupId, db.writableDatabase)
                            return@withContext true
                        } else {
                            Log.e(TAG, "Failed to send message to Firebase")
                            // Even if Firebase fails, save locally so it appears in the UI
                            val localSaveSuccess = saveMessageToLocal(groupId, completeMessage)
                            Log.d(TAG, "Local save after Firebase failure: $localSaveSuccess")
                            return@withContext localSaveSuccess
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Exception during Firebase message send", e)
                        // If Firebase throws an exception, save locally
                        val localSaveSuccess = saveMessageToLocal(groupId, completeMessage)
                        Log.d(TAG, "Local save after Firebase exception: $localSaveSuccess")
                        return@withContext localSaveSuccess
                    }
                } else {
                    Log.d(TAG, "Offline, saving message locally")
                    // Save message locally so it appears in the UI
                    val success = saveMessageToLocal(groupId, completeMessage)
                    if (success) {
                        // Register this group for sync when connectivity returns
                        addPendingSyncGroup(groupId)
                        Log.d(TAG, "Message saved locally and group added to pending sync")
                    } else {
                        Log.e(TAG, "Failed to save message locally")
                    }
                    return@withContext success
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending group message", e)
                e.printStackTrace()
                return@withContext false
            }
        }
    }
    
    private fun saveMessageToLocal(groupId: String, message: GroupMessage): Boolean {
        val sqliteDb = db.writableDatabase
        
        try {
            // Ensure the group_chats table exists
            try {
                sqliteDb.execSQL("""
                    CREATE TABLE IF NOT EXISTS ${AppDatabase.TABLE_GROUP_CHATS} (
                        group_id TEXT PRIMARY KEY,
                        messages TEXT,
                        ${AppDatabase.COLUMN_CREATED_AT} INTEGER,
                        ${AppDatabase.COLUMN_UPDATED_AT} INTEGER,
                        ${AppDatabase.COLUMN_PENDING_SYNC} INTEGER
                    )
                """)
                Log.d(TAG, "Ensured group_chats table exists")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating group_chats table", e)
                return false
            }
            
            // Check if chat entry exists
            val cursor = sqliteDb.query(
                AppDatabase.TABLE_GROUP_CHATS,
                arrayOf("messages"),
                "group_id = ?",
                arrayOf(groupId),
                null, null, null
            )
            
            val messagesJson = if (cursor.moveToFirst()) {
                Log.d(TAG, "Found existing chat entry for group: $groupId")
                // Get existing messages
                val existingJson = cursor.getString(cursor.getColumnIndexOrThrow("messages"))
                val jsonArray = if (existingJson.isNullOrEmpty()) {
                    Log.d(TAG, "No existing messages, creating new JSON array")
                    org.json.JSONArray()
                } else {
                    try {
                        org.json.JSONArray(existingJson)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing existing messages JSON, creating new array", e)
                        org.json.JSONArray()
                    }
                }
                
                // Add new message
                val messageJson = org.json.JSONObject().apply {
                    put("id", message.id)
                    put("senderId", message.senderId)
                    put("senderName", message.senderName)
                    put("content", message.content)
                    put("timestamp", message.timestamp)
                    put("messageType", message.messageType.name)
                    put("noteId", message.noteId)
                    put("noteType", message.noteType)
                    put("pendingSync", 1) // Mark as pending sync
                }
                jsonArray.put(messageJson)
                jsonArray.toString()
            } else {
                Log.d(TAG, "No existing chat entry for group: $groupId, creating new one")
                // No existing messages, create new array
                val jsonArray = org.json.JSONArray()
                val messageJson = org.json.JSONObject().apply {
                    put("id", message.id)
                    put("senderId", message.senderId)
                    put("senderName", message.senderName)
                    put("content", message.content)
                    put("timestamp", message.timestamp)
                    put("messageType", message.messageType.name)
                    put("noteId", message.noteId)
                    put("noteType", message.noteType)
                    put("pendingSync", 1) // Mark as pending sync
                }
                jsonArray.put(messageJson)
                jsonArray.toString()
            }
            cursor.close()
            
            // Update or insert chat entry
            val values = ContentValues().apply {
                put("group_id", groupId)
                put("messages", messagesJson)
                put(AppDatabase.COLUMN_CREATED_AT, System.currentTimeMillis())
                put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
                put(AppDatabase.COLUMN_PENDING_SYNC, 1) // Mark as pending sync
            }
            
            val rowsAffected = sqliteDb.insertWithOnConflict(
                AppDatabase.TABLE_GROUP_CHATS,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )
            
            if (rowsAffected != -1L) {
                Log.d(TAG, "Successfully saved message to local database")
                return true
            } else {
                Log.e(TAG, "Failed to save message to local database: no rows affected")
                return false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving message to local database", e)
            e.printStackTrace()
            return false
        }
    }
    
    private fun addPendingSyncGroup(groupId: String) {
        try {
            val sqliteDb = db.writableDatabase
            
            // Check if pending_sync_groups table exists, create if not
            sqliteDb.execSQL("""
                CREATE TABLE IF NOT EXISTS pending_sync_groups (
                    group_id TEXT PRIMARY KEY,
                    last_attempt LONG
                )
            """)
            
            // Add or update group in pending sync table
            val values = ContentValues().apply {
                put("group_id", groupId)
                put("last_attempt", System.currentTimeMillis())
            }
            
            sqliteDb.insertWithOnConflict(
                "pending_sync_groups",
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )
            
            Log.d(TAG, "Added group $groupId to pending sync queue")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding group to pending sync queue", e)
        }
    }
    
    suspend fun getUserDetails(userId: String): UserProfile? {
        if (!isInitialized) {
            Log.e(TAG, "Cannot get user details: manager not initialized")
            throw IllegalStateException("OfflineFirstDataManager not initialized")
        }

        Log.d(TAG, "Getting details for user: $userId")
        return withContext(Dispatchers.IO) {
            try {
                // First try to get from SQLite
                val user = getUserDetailsFromLocal(userId)
                
                // If online and user not found, sync with Firebase
                if (user == null && networkMonitor.isOnline.first()) {
                    Log.d(TAG, "User not found locally, fetching from Firebase")
                    
                    // Use suspendCoroutine to convert callback to suspend function
                    val fbUser = suspendCoroutine<UserProfile?> { continuation ->
                        var isCompleted = false
                        fbReadOps.getUserDetails(userId) { userProfile ->
                            if (!isCompleted) {
                                isCompleted = true
                                continuation.resume(userProfile)
                            }
                        }
                    }
                    
                    // If user found in Firebase, save to local database
                    if (fbUser != null) {
                        saveUserToLocal(fbUser)
                        return@withContext fbUser
                    }
                }
                
                return@withContext user
            } catch (e: Exception) {
                Log.e(TAG, "Error getting user details", e)
                throw e
            }
        }
    }

    private fun getUserDetailsFromLocal(userId: String): UserProfile? {
        val sqliteDb = db.readableDatabase
        
        val cursor = sqliteDb.query(
            AppDatabase.TABLE_USERS,
            null,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(userId),
            null, null, null
        )
        
        return cursor.use { c ->
            if (c.moveToFirst()) {
                val email = c.getString(c.getColumnIndexOrThrow("email"))
                val username = c.getString(c.getColumnIndexOrThrow("username"))
                val createdAt = c.getLong(c.getColumnIndexOrThrow(AppDatabase.COLUMN_CREATED_AT))
                val lastLogin = c.getLong(c.getColumnIndexOrThrow("last_login"))
                
                UserProfile(
                    id = userId,
                    email = email,
                    username = username,
                    createdAt = createdAt,
                    lastLogin = lastLogin
                )
            } else {
                null
            }
        }
    }

    private fun saveUserToLocal(user: UserProfile) {
        val sqliteDb = db.writableDatabase
        
        val values = ContentValues().apply {
            put(AppDatabase.COLUMN_ID, user.id)
            put("email", user.email)
            put("username", user.username)
            put(AppDatabase.COLUMN_CREATED_AT, user.createdAt)
            put("last_login", user.lastLogin)
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
            put(AppDatabase.COLUMN_PENDING_SYNC, 0)
        }
        
        sqliteDb.insertWithOnConflict(
            AppDatabase.TABLE_USERS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
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
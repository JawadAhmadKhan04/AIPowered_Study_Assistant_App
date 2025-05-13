package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.AppDatabase
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.NoteItem

/**
 * Data Access Object for writing user data to SQLite.
 */
class UserWriteDao(private val db: SQLiteDatabase) {

    /**
     * Insert a new user
     */
    fun insert(user: UserProfile): Long {
        val values = ContentValues().apply {
            put(AppDatabase.COLUMN_ID, user.id)
            put("username", user.username)
            put(AppDatabase.COLUMN_EMAIL, user.email)
            put(AppDatabase.COLUMN_FCM_TOKEN, user.fcmToken)
            put(AppDatabase.COLUMN_LAST_LOGIN, System.currentTimeMillis())
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
            put(AppDatabase.COLUMN_CREATED_AT, System.currentTimeMillis())
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
        }
        
        return db.insert(AppDatabase.TABLE_USERS, null, values)
    }

    /**
     * Update an existing user
     */
    fun update(user: UserProfile): Int {
        val values = ContentValues().apply {
            put("username", user.username)
            put(AppDatabase.COLUMN_EMAIL, user.email)
            put(AppDatabase.COLUMN_FCM_TOKEN, user.fcmToken)
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
        }
        
        return db.update(
            AppDatabase.TABLE_USERS,
            values,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(user.id)
        )
    }

    /**
     * Delete a user by their ID
     */
    fun delete(id: String): Int {
        return db.delete(
            AppDatabase.TABLE_USERS,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(id)
        )
    }

    /**
     * Mark a user as synchronized
     */
    fun markSynchronized(id: String) {
        val values = ContentValues().apply {
            put(AppDatabase.COLUMN_PENDING_SYNC, 0)
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
        }
        
        db.update(
            AppDatabase.TABLE_USERS,
            values,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(id)
        )
    }

    /**
     * Update user's FCM token
     */
    fun updateFcmToken(id: String, fcmToken: String) {
        val values = ContentValues().apply {
            put(AppDatabase.COLUMN_FCM_TOKEN, fcmToken)
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
        }
        
        db.update(
            AppDatabase.TABLE_USERS,
            values,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(id)
        )
    }

    /**
     * Update user's last login time
     */
    fun updateLastLogin(id: String) {
        val values = ContentValues().apply {
            put(AppDatabase.COLUMN_LAST_LOGIN, System.currentTimeMillis())
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
        }
        
        db.update(
            AppDatabase.TABLE_USERS,
            values,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(id)
        )
    }

    fun insertCourse(userId: String, course: Course) {
        val values = ContentValues().apply {
            put(AppDatabase.COLUMN_ID, course.courseId)
            put("created_by", userId)
            put("title", course.title)
            put("description", course.description)
            put("color", course.buttonColorResId)
            put("note_count", course.noteCount)
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
            put(AppDatabase.COLUMN_CREATED_AT, System.currentTimeMillis())
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
        }

        val courseId = db.insert(AppDatabase.TABLE_COURSES, null, values)
        if (courseId != -1L) {
            Log.d("UserWriteDao", "Course inserted successfully with ID: ${course.courseId}")
            
            // Insert course member
            val memberValues = ContentValues().apply {
                put("course_id", course.courseId)
                put("user_id", userId)
                put("last_modified", System.currentTimeMillis())
                put(AppDatabase.COLUMN_PENDING_SYNC, 1)
            }
            db.insert(AppDatabase.TABLE_COURSE_MEMBERS, null, memberValues)
        } else {
            Log.e("UserWriteDao", "Failed to insert course")
        }
    }

    fun updateCourse(course: Course): Int {
        val values = ContentValues().apply {
            put("title", course.title)
            put("description", course.description)
            put("color", course.buttonColorResId)
            put("note_count", course.noteCount)
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
        }
        
        return db.update(
            AppDatabase.TABLE_COURSES,
            values,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(course.courseId)
        )
    }

    fun markCourseSynchronized(courseId: String) {
        val values = ContentValues().apply {
            put(AppDatabase.COLUMN_PENDING_SYNC, 0)
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
        }
        
        db.update(
            AppDatabase.TABLE_COURSES,
            values,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(courseId)
        )
    }

    fun markCourseForSync(courseId: String) {
        val values = ContentValues().apply {
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
        }
        
        db.update(
            AppDatabase.TABLE_COURSES,
            values,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(courseId)
        )
    }

    fun updateCourse(courseId: String, title: String, description: String, color: Int) {
        val values = ContentValues().apply {
            put("title", title)
            put("description", description)
            put("color", color)
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
        }
        
        db.update(
            AppDatabase.TABLE_COURSES,
            values,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(courseId)
        )
    }

    fun deleteCourse(courseId: String) {
        db.delete(
            AppDatabase.TABLE_COURSES,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(courseId)
        )
    }

    fun toggleBookmark(userId: String, courseId: String, isBookmarked: Boolean) {
        if (isBookmarked) {
            // Add bookmark
            val values = ContentValues().apply {
                put(AppDatabase.COLUMN_USER_ID, userId)
                put("course_id", courseId)
                put(AppDatabase.COLUMN_PENDING_SYNC, 1)
                put(AppDatabase.COLUMN_CREATED_AT, System.currentTimeMillis())
            }
            db.insert(AppDatabase.TABLE_BOOKMARKS, null, values)
        } else {
            // Remove bookmark
            db.delete(
                AppDatabase.TABLE_BOOKMARKS,
                "${AppDatabase.COLUMN_USER_ID} = ? AND course_id = ?",
                arrayOf(userId, courseId)
            )
        }
    }

    fun clearAllData() {
        db.delete(AppDatabase.TABLE_USERS, null, null)
        db.delete(AppDatabase.TABLE_COURSES, null, null)
        db.delete(AppDatabase.TABLE_COURSE_MEMBERS, null, null)
        db.delete(AppDatabase.TABLE_BOOKMARKS, null, null)
        db.delete(AppDatabase.TABLE_STUDY_GROUPS, null, null)
        db.delete(AppDatabase.TABLE_GROUP_MEMBERS, null, null)
        db.delete(AppDatabase.TABLE_GROUP_CHATS, null, null)
    }

    /**
     * Mark a bookmark as synchronized
     */
    fun markBookmarkSynchronized(userId: String, courseId: String) {
        val values = ContentValues().apply {
            put(AppDatabase.COLUMN_PENDING_SYNC, 0)
        }
        
        db.update(
            AppDatabase.TABLE_BOOKMARKS,
            values,
            "${AppDatabase.COLUMN_USER_ID} = ? AND course_id = ?",
            arrayOf(userId, courseId)
        )
    }

    fun insertNote(courseId: String, note: NoteItem, content: String, tag: Int): String {
        val noteId = java.util.UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: throw IllegalStateException("User not authenticated")

        val values = ContentValues().apply {
            put(AppDatabase.COLUMN_ID, noteId)
            put("course_id", courseId)
            put("title", note.title)
            put("content", content)
            put("audio", "")
            put("type", note.type)
            put("created_by", userId)
            put(AppDatabase.COLUMN_CREATED_AT, timestamp)
            put(AppDatabase.COLUMN_UPDATED_AT, timestamp)
            put("summary", "")
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
        }

        db.insert(AppDatabase.TABLE_NOTES, null, values)

        // Insert tag
        val tagValues = ContentValues().apply {
            put("note_id", noteId)
            put("tag", tag)
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
        }
        db.insert(AppDatabase.TABLE_NOTE_TAGS, null, tagValues)

        // Insert note member
        val memberValues = ContentValues().apply {
            put("note_id", noteId)
            put("user_id", userId)
            put("last_modified", timestamp)
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
        }
        db.insert(AppDatabase.TABLE_NOTE_MEMBERS, null, memberValues)

        return noteId
    }

    fun markNoteForSync(noteId: String) {
        val values = ContentValues().apply {
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
        }
        db.update(
            AppDatabase.TABLE_NOTES,
            values,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(noteId)
        )
    }

    fun markNoteSynchronized(noteId: String) {
        val values = ContentValues().apply {
            put(AppDatabase.COLUMN_PENDING_SYNC, 0)
        }
        db.update(
            AppDatabase.TABLE_NOTES,
            values,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(noteId)
        )

        // Also mark related tables as synchronized
        db.update(
            AppDatabase.TABLE_NOTE_TAGS,
            values,
            "note_id = ?",
            arrayOf(noteId)
        )

        db.update(
            AppDatabase.TABLE_NOTE_MEMBERS,
            values,
            "note_id = ?",
            arrayOf(noteId)
        )
    }
} 
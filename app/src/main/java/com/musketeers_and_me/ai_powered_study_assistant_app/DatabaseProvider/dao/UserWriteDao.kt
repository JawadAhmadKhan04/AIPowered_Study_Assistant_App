package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.AppDatabase
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.UserProfile

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
            put("email", user.email)
            put("username", user.username)
            put(AppDatabase.COLUMN_CREATED_AT, user.createdAt)
            put("last_login", user.lastLogin)
            put("fcm_token", user.fcmToken)
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
            put(AppDatabase.COLUMN_PENDING_SYNC, if (user.pendingSync) 1 else 0)
        }
        
        return db.insert(AppDatabase.TABLE_USERS, null, values)
    }

    /**
     * Update an existing user
     */
    fun update(user: UserProfile): Int {
        val values = ContentValues().apply {
            put("email", user.email)
            put("username", user.username)
            put("last_login", user.lastLogin)
            put("fcm_token", user.fcmToken)
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
            put(AppDatabase.COLUMN_PENDING_SYNC, if (user.pendingSync) 1 else 0)
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
            put("fcm_token", fcmToken)
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
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
            put("last_login", System.currentTimeMillis())
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
        }
        
        db.update(
            AppDatabase.TABLE_USERS,
            values,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(id)
        )
    }

    fun insertCourse(userId: String, course: Course): Long {
        val values = ContentValues().apply {
            put(AppDatabase.COLUMN_ID, course.courseId)
            put("title", course.title)
            put("description", course.description)
            put("note_count", course.noteCount)
            put("color", course.buttonColorResId)
            put("is_bookmarked", if (course.bookmarked) 1 else 0)
            put("created_by", userId)
            put(AppDatabase.COLUMN_CREATED_AT, System.currentTimeMillis())
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
        }
        
        val courseId = db.insert(AppDatabase.TABLE_COURSES, null, values)
        
        // Check if user is already a member of the course
        val cursor = db.query(
            AppDatabase.TABLE_COURSE_MEMBERS,
            null,
            "course_id = ? AND user_id = ?",
            arrayOf(course.courseId, userId),
            null,
            null,
            null
        )
        
        val isMember = cursor.use { it.moveToFirst() }
        
        if (!isMember) {
            // Add user as a member of the course only if not already a member
            val memberValues = ContentValues().apply {
                put("course_id", course.courseId)
                put("user_id", userId)
                put("last_modified", System.currentTimeMillis())
                put(AppDatabase.COLUMN_PENDING_SYNC, 1)
            }
            db.insert(AppDatabase.TABLE_COURSE_MEMBERS, null, memberValues)
        }
        
        return courseId
    }

    fun updateCourse(course: Course): Int {
        val values = ContentValues().apply {
            put("title", course.title)
            put("description", course.description)
            put("note_count", course.noteCount)
            put("color", course.buttonColorResId)
            put("is_bookmarked", if (course.bookmarked) 1 else 0)
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
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

    fun clearAllData() {
        db.delete(AppDatabase.TABLE_USERS, null, null)
        db.delete(AppDatabase.TABLE_COURSES, null, null)
    }
} 
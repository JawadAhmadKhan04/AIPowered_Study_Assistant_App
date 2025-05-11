package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
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
            put(AppDatabase.COLUMN_CREATED_AT, System.currentTimeMillis())
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
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
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
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
        val values = ContentValues().apply {
            put("is_deleted", 1)
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
        }
        return db.update(
            AppDatabase.TABLE_USERS,
            values,
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

    fun insertCourse(userId: String, course: Course) {
        val values = ContentValues().apply {
            put(AppDatabase.COLUMN_ID, course.courseId)
            put("title", course.title)
            put("description", course.description)
            put("created_by", userId)
            put("color", course.buttonColorResId)
            put("note_count", course.noteCount)
            put("is_bookmarked", if (course.bookmarked) 1 else 0)
            put(AppDatabase.COLUMN_CREATED_AT, System.currentTimeMillis())
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
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

    fun updateCourse(courseId: String, title: String, description: String, color: Int) {
        val values = ContentValues().apply {
            put("title", title)
            put("description", description)
            put("color", color)
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
        }
        
        db.update(
            AppDatabase.TABLE_COURSES,
            values,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(courseId)
        )
    }

    fun deleteCourse(courseId: String) {
        val values = ContentValues().apply {
            put("is_deleted", 1)
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
        }
        
        db.update(
            AppDatabase.TABLE_COURSES,
            values,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(courseId)
        )
    }

    fun toggleBookmark(userId: String, courseId: String, isBookmarked: Boolean) {
        if (isBookmarked) {
            val values = ContentValues().apply {
                put("user_id", userId)
                put("course_id", courseId)
                put(AppDatabase.COLUMN_CREATED_AT, System.currentTimeMillis())
                put(AppDatabase.COLUMN_PENDING_SYNC, 1)
            }
            db.insert(AppDatabase.TABLE_BOOKMARKS, null, values)
        } else {
            val values = ContentValues().apply {
                put("is_deleted", 1)
                put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
                put(AppDatabase.COLUMN_PENDING_SYNC, 1)
            }
            db.update(
                AppDatabase.TABLE_BOOKMARKS,
                values,
                "user_id = ? AND course_id = ?",
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
} 
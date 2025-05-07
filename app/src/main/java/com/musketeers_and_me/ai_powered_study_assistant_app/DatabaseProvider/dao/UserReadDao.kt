package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.dao

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.UserProfile
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.AppDatabase

/**
 * Data Access Object for reading user data from SQLite.
 */
class UserReadDao(private val db: SQLiteDatabase) {

    /**
     * Get a user by their ID
     */
    fun getUserById(id: String): UserProfile? {
        val cursor = db.query(
            AppDatabase.TABLE_USERS,
            null,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(id),
            null,
            null,
            null
        )
        return cursor.use {
            if (it.moveToFirst()) cursorToUser(it) else null
        }
    }

    fun getCoursesByUserId(userId: String): List<Course> {
        val courses = mutableListOf<Course>()
        val cursor = db.query(
            AppDatabase.TABLE_COURSES,
            null,
            "created_by = ?",
            arrayOf(userId),
            null,
            null,
            "${AppDatabase.COLUMN_CREATED_AT} DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                cursorToCourse(it)?.let { course -> courses.add(course) }
            }
        }
        return courses
    }

    /**
     * Get all users with pending sync flag
     */
    fun getPendingSyncItems(): List<UserProfile> {
        val cursor = db.query(
            AppDatabase.TABLE_USERS,
            null,
            "${AppDatabase.COLUMN_PENDING_SYNC} = ?",
            arrayOf("1"),
            null,
            null,
            null
        )

        return cursor.use {
            val users = mutableListOf<UserProfile>()
            while (it.moveToNext()) {
                cursorToUser(it)?.let { user -> users.add(user) }
            }
            users
        }
    }

    fun getCourseById(courseId: String): Course? {
        val cursor = db.query(
            AppDatabase.TABLE_COURSES,
            null,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(courseId),
            null,
            null,
            null
        )
        return cursor.use {
            if (it.moveToFirst()) cursorToCourse(it) else null
        }
    }

    fun getPendingSyncCourses(): List<Course> {
        val cursor = db.query(
            AppDatabase.TABLE_COURSES,
            null,
            "${AppDatabase.COLUMN_PENDING_SYNC} = ?",
            arrayOf("1"),
            null,
            null,
            null
        )

        return cursor.use {
            val courses = mutableListOf<Course>()
            while (it.moveToNext()) {
                cursorToCourse(it)?.let { course -> courses.add(course) }
            }
            courses
        }
    }

    private fun cursorToUser(cursor: Cursor): UserProfile? {
        return try {
            UserProfile(
                id = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabase.COLUMN_ID)),
                email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                username = cursor.getString(cursor.getColumnIndexOrThrow("username")),
                createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(AppDatabase.COLUMN_CREATED_AT)),
                lastLogin = cursor.getLong(cursor.getColumnIndexOrThrow("last_login")),
                fcmToken = cursor.getString(cursor.getColumnIndexOrThrow("fcm_token")),
                pendingSync = cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabase.COLUMN_PENDING_SYNC)) == 1
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun cursorToCourse(cursor: Cursor): Course? {
        return try {
            Course(
                title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                noteCount = cursor.getInt(cursor.getColumnIndexOrThrow("note_count")),
                daysAgo = calculateDaysAgo(cursor.getLong(cursor.getColumnIndexOrThrow(AppDatabase.COLUMN_CREATED_AT))),
                buttonColorResId = cursor.getInt(cursor.getColumnIndexOrThrow("color")),
                bookmarked = cursor.getInt(cursor.getColumnIndexOrThrow("is_bookmarked")) == 1,
                courseId = cursor.getString(cursor.getColumnIndexOrThrow(AppDatabase.COLUMN_ID)),
                description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateDaysAgo(timestamp: Long): Int {
        val currentTime = System.currentTimeMillis()
        val diffInMillis = currentTime - timestamp
        return (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
    }
} 
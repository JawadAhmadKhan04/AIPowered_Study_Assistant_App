package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.dao

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.AppDatabase
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.UserProfile

/**
 * Data Access Object that handles read operations for the current user's data.
 */
class UserReadDao(private val db: SQLiteDatabase) {
    private val TAG = "UserReadDao"

    // User Profile Operations
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
            if (it.moveToFirst()) {
                UserProfile(
                    id = it.getString(it.getColumnIndexOrThrow(AppDatabase.COLUMN_ID)),
                    email = it.getString(it.getColumnIndexOrThrow(AppDatabase.COLUMN_EMAIL)),
                    username = it.getString(it.getColumnIndexOrThrow("username")),
                    createdAt = it.getLong(it.getColumnIndexOrThrow(AppDatabase.COLUMN_CREATED_AT)),
                    lastLogin = it.getLong(it.getColumnIndexOrThrow(AppDatabase.COLUMN_LAST_LOGIN)),
                    fcmToken = it.getString(it.getColumnIndexOrThrow(AppDatabase.COLUMN_FCM_TOKEN))
                )
            } else {
                null
            }
        }
    }

    fun getPendingSyncItems(): List<UserProfile> {
        val users = mutableListOf<UserProfile>()
        val cursor = db.query(
            AppDatabase.TABLE_USERS,
            null,
            "${AppDatabase.COLUMN_PENDING_SYNC} = ?",
            arrayOf("1"),
            null,
            null,
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                val user = UserProfile(
                    id = it.getString(it.getColumnIndexOrThrow(AppDatabase.COLUMN_ID)),
                    email = it.getString(it.getColumnIndexOrThrow(AppDatabase.COLUMN_EMAIL)),
                    username = it.getString(it.getColumnIndexOrThrow(AppDatabase.COLUMN_NAME)),
                    createdAt = it.getLong(it.getColumnIndexOrThrow(AppDatabase.COLUMN_CREATED_AT)),
                    lastLogin = it.getLong(it.getColumnIndexOrThrow(AppDatabase.COLUMN_LAST_LOGIN)),
                    fcmToken = it.getString(it.getColumnIndexOrThrow(AppDatabase.COLUMN_FCM_TOKEN))
                )
                users.add(user)
            }
        }
        return users
    }

    // Course Operations
    fun getCoursesByUserId(userId: String): List<Course> {
        val courses = mutableListOf<Course>()
        val query = """
            SELECT c.*, 
                   CASE WHEN b.course_id IS NOT NULL THEN 1 ELSE 0 END as is_bookmarked
            FROM ${AppDatabase.TABLE_COURSES} c
            LEFT JOIN ${AppDatabase.TABLE_BOOKMARKS} b ON c.${AppDatabase.COLUMN_ID} = b.course_id 
                AND b.user_id = ?
            LEFT JOIN ${AppDatabase.TABLE_COURSE_MEMBERS} cm ON c.${AppDatabase.COLUMN_ID} = cm.course_id
            WHERE c.created_by = ? OR cm.user_id = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId, userId, userId))
        cursor.use {
            while (it.moveToNext()) {
                val course = Course(
                    courseId = it.getString(it.getColumnIndexOrThrow(AppDatabase.COLUMN_ID)),
                    title = it.getString(it.getColumnIndexOrThrow("title")),
                    noteCount = it.getInt(it.getColumnIndexOrThrow("note_count")),
                    daysAgo = 0,
                    buttonColorResId = it.getInt(it.getColumnIndexOrThrow("color")),
                    bookmarked = it.getInt(it.getColumnIndexOrThrow("is_bookmarked")) == 1,
                    description = it.getString(it.getColumnIndexOrThrow("description"))
                )
                courses.add(course)
            }
        }
        return courses
    }

    fun getCourseById(courseId: String): Course? {
        val query = """
            SELECT c.*, 
                   CASE WHEN b.course_id IS NOT NULL THEN 1 ELSE 0 END as is_bookmarked
            FROM ${AppDatabase.TABLE_COURSES} c
            LEFT JOIN ${AppDatabase.TABLE_BOOKMARKS} b ON c.${AppDatabase.COLUMN_ID} = b.course_id
            WHERE c.${AppDatabase.COLUMN_ID} = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(courseId))
        return cursor.use {
            if (it.moveToFirst()) {
                Course(
                    courseId = it.getString(it.getColumnIndexOrThrow(AppDatabase.COLUMN_ID)),
                    title = it.getString(it.getColumnIndexOrThrow("title")),
                    noteCount = it.getInt(it.getColumnIndexOrThrow("note_count")),
                    daysAgo = 0,
                    buttonColorResId = it.getInt(it.getColumnIndexOrThrow("color")),
                    bookmarked = it.getInt(it.getColumnIndexOrThrow("is_bookmarked")) == 1,
                    description = it.getString(it.getColumnIndexOrThrow("description"))
                )
            } else {
                null
            }
        }
    }

    fun getPendingSyncCourses(): List<Course> {
        val courses = mutableListOf<Course>()
        val query = """
            SELECT c.*, 
                   CASE WHEN b.course_id IS NOT NULL THEN 1 ELSE 0 END as is_bookmarked
            FROM ${AppDatabase.TABLE_COURSES} c
            LEFT JOIN ${AppDatabase.TABLE_BOOKMARKS} b ON c.${AppDatabase.COLUMN_ID} = b.course_id
            WHERE c.${AppDatabase.COLUMN_PENDING_SYNC} = 1
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        cursor.use {
            while (it.moveToNext()) {
                val course = Course(
                    courseId = it.getString(it.getColumnIndexOrThrow(AppDatabase.COLUMN_ID)),
                    title = it.getString(it.getColumnIndexOrThrow("title")),
                    noteCount = it.getInt(it.getColumnIndexOrThrow("note_count")),
                    daysAgo = 0,
                    buttonColorResId = it.getInt(it.getColumnIndexOrThrow("color")),
                    bookmarked = it.getInt(it.getColumnIndexOrThrow("is_bookmarked")) == 1,
                    description = it.getString(it.getColumnIndexOrThrow("description"))
                )
                courses.add(course)
            }
        }
        return courses
    }

    // Bookmark Operations
    fun isCourseBookmarked(userId: String, courseId: String): Boolean {
        val cursor = db.query(
            AppDatabase.TABLE_BOOKMARKS,
            arrayOf("1"),
            "user_id = ? AND course_id = ?",
            arrayOf(userId, courseId),
            null,
            null,
            null
        )
        
        return cursor.use {
            it.count > 0
        }
    }

    fun getPendingSyncBookmarks(): List<Pair<String, String>> {
        val bookmarks = mutableListOf<Pair<String, String>>()
        val cursor = db.query(
            AppDatabase.TABLE_BOOKMARKS,
            arrayOf("user_id", "course_id"),
            "${AppDatabase.COLUMN_PENDING_SYNC} = ?",
            arrayOf("1"),
            null,
            null,
            null
        )
        
        cursor.use {
            while (it.moveToNext()) {
                val userId = it.getString(it.getColumnIndexOrThrow("user_id"))
                val courseId = it.getString(it.getColumnIndexOrThrow("course_id"))
                bookmarks.add(Pair(userId, courseId))
            }
        }
        return bookmarks
    }

    fun getBookmarksByUserId(userId: String): List<Course> {
        val courses = mutableListOf<Course>()
        val query = """
            SELECT c.*
            FROM ${AppDatabase.TABLE_COURSES} c
            INNER JOIN ${AppDatabase.TABLE_BOOKMARKS} b ON c.${AppDatabase.COLUMN_ID} = b.course_id
            WHERE b.user_id = ? AND b.${AppDatabase.COLUMN_PENDING_SYNC} = 1
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId))
        cursor.use {
            while (it.moveToNext()) {
                val course = Course(
                    courseId = it.getString(it.getColumnIndexOrThrow(AppDatabase.COLUMN_ID)),
                    title = it.getString(it.getColumnIndexOrThrow("title")),
                    noteCount = it.getInt(it.getColumnIndexOrThrow("note_count")),
                    daysAgo = 0,
                    buttonColorResId = it.getInt(it.getColumnIndexOrThrow("color")),
                    bookmarked = true,
                    description = it.getString(it.getColumnIndexOrThrow("description"))
                )
                courses.add(course)
            }
        }
        return courses
    }

    fun isCoursePendingSync(courseId: String): Boolean {
        var isPendingSync = false
        val cursor = db.query(
            AppDatabase.TABLE_COURSES,
            arrayOf(AppDatabase.COLUMN_PENDING_SYNC),
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(courseId),
            null,
            null,
            null
        )
        
        try {
            if (cursor.moveToFirst()) {
                isPendingSync = cursor.getInt(cursor.getColumnIndexOrThrow(AppDatabase.COLUMN_PENDING_SYNC)) == 1
            }
        } finally {
            cursor.close()
        }
        
        return isPendingSync
    }
} 
package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.dao

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.AppDatabase
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.UserProfile

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
            if (it.moveToFirst()) {
                UserProfile(
                    id = it.getString(it.getColumnIndexOrThrow(AppDatabase.COLUMN_ID)),
                    email = it.getString(it.getColumnIndexOrThrow("email")),
                    username = it.getString(it.getColumnIndexOrThrow("username"))
                )
            } else null
        }
    }

    /**
     * Get all users with pending sync flag
     */
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
                users.add(
                    UserProfile(
                        id = it.getString(it.getColumnIndexOrThrow(AppDatabase.COLUMN_ID)),
                        email = it.getString(it.getColumnIndexOrThrow("email")),
                        username = it.getString(it.getColumnIndexOrThrow("username"))
                    )
                )
            }
        }
        return users
    }

    fun getCoursesByUserId(userId: String): List<Course> {
        val courses = mutableListOf<Course>()
        
        val query = """
            SELECT c.*, b.course_id as is_bookmarked
            FROM ${AppDatabase.TABLE_COURSES} c
            LEFT JOIN ${AppDatabase.TABLE_BOOKMARKS} b ON c.${AppDatabase.COLUMN_ID} = b.course_id AND b.user_id = ?
            WHERE c.created_by = ? OR EXISTS (
                SELECT 1 FROM ${AppDatabase.TABLE_COURSE_MEMBERS} m 
                WHERE m.course_id = c.${AppDatabase.COLUMN_ID} AND m.user_id = ?
            )
            ORDER BY c.${AppDatabase.COLUMN_CREATED_AT} DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId, userId, userId))
        
        cursor.use {
            while (it.moveToNext()) {
                val course = Course(
                    courseId = it.getString(it.getColumnIndexOrThrow(AppDatabase.COLUMN_ID)),
                    title = it.getString(it.getColumnIndexOrThrow("title")),
                    noteCount = it.getInt(it.getColumnIndexOrThrow("note_count")),
                    daysAgo = 0, // Calculate this if needed
                    buttonColorResId = it.getInt(it.getColumnIndexOrThrow("color")),
                    bookmarked = !it.isNull(it.getColumnIndexOrThrow("is_bookmarked")),
                    description = it.getString(it.getColumnIndexOrThrow("description"))
                )
                courses.add(course)
            }
        }
        
        return courses
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
            } else null
        }
    }

    fun getPendingSyncCourses(): List<Course> {
        val courses = mutableListOf<Course>()
        val cursor = db.query(
            AppDatabase.TABLE_COURSES,
            null,
            "${AppDatabase.COLUMN_PENDING_SYNC} = ?",
            arrayOf("1"),
            null,
            null,
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                courses.add(
                    Course(
                        courseId = it.getString(it.getColumnIndexOrThrow(AppDatabase.COLUMN_ID)),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        noteCount = it.getInt(it.getColumnIndexOrThrow("note_count")),
                        daysAgo = 0,
                        buttonColorResId = it.getInt(it.getColumnIndexOrThrow("color")),
                        bookmarked = it.getInt(it.getColumnIndexOrThrow("is_bookmarked")) == 1,
                        description = it.getString(it.getColumnIndexOrThrow("description"))
                    )
                )
            }
        }
        return courses
    }
} 
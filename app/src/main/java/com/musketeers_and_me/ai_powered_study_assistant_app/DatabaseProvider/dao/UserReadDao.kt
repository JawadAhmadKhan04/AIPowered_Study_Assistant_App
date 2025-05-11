package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.dao

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.AppDatabase
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.UserProfile
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.NoteItem
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
        val cursor = db.query(
            AppDatabase.TABLE_COURSES,
            arrayOf(AppDatabase.COLUMN_PENDING_SYNC),
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(courseId),
            null,
            null,
            null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                it.getInt(it.getColumnIndexOrThrow(AppDatabase.COLUMN_PENDING_SYNC)) == 1
            } else {
                false
            }
        }
    }

    fun getNotesByCourseId(courseId: String): List<NoteItem> {
        val notes = mutableListOf<NoteItem>()
        val query = """
            SELECT n.*, 
                   CASE WHEN nm.last_modified IS NOT NULL THEN nm.last_modified ELSE n.created_at END as last_modified
            FROM ${AppDatabase.TABLE_NOTES} n
            LEFT JOIN ${AppDatabase.TABLE_NOTE_MEMBERS} nm ON n.${AppDatabase.COLUMN_ID} = nm.note_id
            WHERE n.course_id = ?
            ORDER BY last_modified DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(courseId))
        cursor.use {
            while (it.moveToNext()) {
                val note = NoteItem(
                    title = it.getString(it.getColumnIndexOrThrow("title")),
                    createdAt = it.getLong(it.getColumnIndexOrThrow(AppDatabase.COLUMN_CREATED_AT)),
                    age = calculateAge(it.getLong(it.getColumnIndexOrThrow("last_modified"))),
                    type = it.getString(it.getColumnIndexOrThrow("type")),
                    note_id = it.getString(it.getColumnIndexOrThrow(AppDatabase.COLUMN_ID))
                )
                notes.add(note)
            }
        }
        return notes
    }

    fun getPendingSyncNotes(): List<NoteItem> {
        val notes = mutableListOf<NoteItem>()
        val query = """
            SELECT n.*, 
                   CASE WHEN nm.last_modified IS NOT NULL THEN nm.last_modified ELSE n.created_at END as last_modified
            FROM ${AppDatabase.TABLE_NOTES} n
            LEFT JOIN ${AppDatabase.TABLE_NOTE_MEMBERS} nm ON n.${AppDatabase.COLUMN_ID} = nm.note_id
            WHERE n.${AppDatabase.COLUMN_PENDING_SYNC} = 1
            ORDER BY last_modified DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        cursor.use {
            while (it.moveToNext()) {
                val note = NoteItem(
                    title = it.getString(it.getColumnIndexOrThrow("title")),
                    createdAt = it.getLong(it.getColumnIndexOrThrow(AppDatabase.COLUMN_CREATED_AT)),
                    age = calculateAge(it.getLong(it.getColumnIndexOrThrow("last_modified"))),
                    type = it.getString(it.getColumnIndexOrThrow("type")),
                    note_id = it.getString(it.getColumnIndexOrThrow(AppDatabase.COLUMN_ID))
                )
                notes.add(note)
            }
        }
        return notes
    }

    fun isNotePendingSync(noteId: String): Boolean {
        val cursor = db.query(
            AppDatabase.TABLE_NOTES,
            arrayOf(AppDatabase.COLUMN_PENDING_SYNC),
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(noteId),
            null,
            null,
            null
        )
        return cursor.use {
            if (it.moveToFirst()) {
                it.getInt(it.getColumnIndexOrThrow(AppDatabase.COLUMN_PENDING_SYNC)) == 1
            } else {
                false
            }
        }
    }

    private fun calculateAge(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        return when {
            days > 0 -> "$days days ago"
            hours > 0 -> "$hours hours ago"
            minutes > 0 -> "$minutes minutes ago"
            else -> "Just now"
        }
    }
} 
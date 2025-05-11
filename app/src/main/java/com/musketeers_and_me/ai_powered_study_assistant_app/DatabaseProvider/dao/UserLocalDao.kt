package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.AppDatabase
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.UserProfile

/**
 * Main Data Access Object that handles essential operations for the current user's data.
 */
class UserLocalDao(private val db: SQLiteDatabase) {
    private val readDao = UserReadDao(db)
    private val writeDao = UserWriteDao(db)

    // User Profile Operations
    fun getUserById(id: String): UserProfile? = readDao.getUserById(id)
    fun insert(user: UserProfile): Long = writeDao.insert(user)
    fun update(user: UserProfile): Int = writeDao.update(user)
    fun delete(id: String): Int = writeDao.delete(id)
    fun markSynchronized(id: String) = writeDao.markSynchronized(id)
    fun updateFcmToken(id: String, fcmToken: String) = writeDao.updateFcmToken(id, fcmToken)
    fun updateLastLogin(id: String) = writeDao.updateLastLogin(id)
    fun getPendingSyncItems(): List<UserProfile> = readDao.getPendingSyncItems()

    // Course Operations
    fun getCoursesByUserId(userId: String): List<Course> = readDao.getCoursesByUserId(userId)
    fun insertCourse(userId: String, course: Course) = writeDao.insertCourse(userId, course)
    fun updateCourse(course: Course): Int = writeDao.updateCourse(course)
    fun getCourseById(courseId: String): Course? = readDao.getCourseById(courseId)
    fun getPendingSyncCourses(): List<Course> = readDao.getPendingSyncCourses()
    fun markCourseSynchronized(courseId: String) = writeDao.markCourseSynchronized(courseId)
    fun markCourseForSync(courseId: String) = writeDao.markCourseForSync(courseId)
    fun updateCourse(courseId: String, title: String, description: String, color: Int) = 
        writeDao.updateCourse(courseId, title, description, color)
    fun deleteCourse(courseId: String) = writeDao.deleteCourse(courseId)
    fun toggleBookmark(userId: String, courseId: String, isBookmarked: Boolean) = 
        writeDao.toggleBookmark(userId, courseId, isBookmarked)

    // Bookmark Operations
    fun getBookmarksByUserId(userId: String): List<Course> = readDao.getBookmarksByUserId(userId)
    fun markBookmarkSynchronized(userId: String, courseId: String) = writeDao.markBookmarkSynchronized(userId, courseId)
    fun getPendingSyncBookmarks(): List<Pair<String, String>> = readDao.getPendingSyncBookmarks()
    fun isCourseBookmarked(userId: String, courseId: String): Boolean = readDao.isCourseBookmarked(userId, courseId)

    // Data Management
    fun clearAllData() = writeDao.clearAllData()

    fun isCoursePendingSync(courseId: String): Boolean = readDao.isCoursePendingSync(courseId)
} 
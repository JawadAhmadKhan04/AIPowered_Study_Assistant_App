package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.AppDatabase
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.UserProfile

/**
 * Data Access Object for local user profile operations in SQLite.
 */
class UserLocalDao(private val db: SQLiteDatabase) : LocalDataAccessObject<UserProfile> {

    override fun insert(item: UserProfile): Long {
        val values = ContentValues().apply {
            put(AppDatabase.COLUMN_ID, item.id)
            put("name", item.name)
            put("email", item.email)
            put("profile_image", item.profileImage)
            put("fcm_token", item.fcmToken)
            put("status", item.status)
            put(AppDatabase.COLUMN_CREATED_AT, System.currentTimeMillis())
            put(AppDatabase.COLUMN_UPDATED_AT, System.currentTimeMillis())
            put(AppDatabase.COLUMN_PENDING_SYNC, 1)
        }
        
        return db.insert(AppDatabase.TABLE_USER_PROFILE, null, values)
    }

    override fun update(item: UserProfile): Int {
        val values = ContentValues().apply {
            put("name", item.name)
            put("email", item.email)
            put("profile_image", item.profileImage)
            put("fcm_token", item.fcmToken)
            put("status", item.status)
        }
        
        setCommonFields(values)
        
        return db.update(
            AppDatabase.TABLE_USER_PROFILE,
            values,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(item.id)
        )
    }

    override fun delete(id: String): Int {
        return db.delete(
            AppDatabase.TABLE_USER_PROFILE,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(id)
        )
    }

    override fun getById(id: String): UserProfile? {
        val cursor = db.query(
            AppDatabase.TABLE_USER_PROFILE,
            null,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(id),
            null,
            null,
            null
        )
        
        return if (cursor.moveToFirst()) {
            val user = cursorToUser(cursor)
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    override fun getAll(): List<UserProfile> {
        val users = mutableListOf<UserProfile>()
        val cursor = db.query(
            AppDatabase.TABLE_USER_PROFILE,
            null,
            null,
            null,
            null,
            null,
            null
        )
        
        while (cursor.moveToNext()) {
            users.add(cursorToUser(cursor))
        }
        
        cursor.close()
        return users
    }

    override fun getPendingSyncItems(): List<UserProfile> {
        val users = mutableListOf<UserProfile>()
        val cursor = db.query(
            AppDatabase.TABLE_USER_PROFILE,
            null,
            "${AppDatabase.COLUMN_PENDING_SYNC} = 1",
            null,
            null,
            null,
            null
        )
        
        while (cursor.moveToNext()) {
            users.add(cursorToUser(cursor))
        }
        
        cursor.close()
        return users
    }

    override fun markSynchronized(id: String) {
        val values = ContentValues().apply {
            put(AppDatabase.COLUMN_PENDING_SYNC, 0)
        }
        
        db.update(
            AppDatabase.TABLE_USER_PROFILE,
            values,
            "${AppDatabase.COLUMN_ID} = ?",
            arrayOf(id)
        )
    }

    private fun cursorToUser(cursor: Cursor): UserProfile {
        val idIndex = cursor.getColumnIndex(AppDatabase.COLUMN_ID)
        val nameIndex = cursor.getColumnIndex("name")
        val emailIndex = cursor.getColumnIndex("email")
        val profileImageIndex = cursor.getColumnIndex("profile_image")
        val fcmTokenIndex = cursor.getColumnIndex("fcm_token")
        val statusIndex = cursor.getColumnIndex("status")
        val pendingSyncIndex = cursor.getColumnIndex(AppDatabase.COLUMN_PENDING_SYNC)
        
        return UserProfile(
            id = cursor.getString(idIndex),
            name = cursor.getString(nameIndex),
            email = cursor.getString(emailIndex),
            profileImage = cursor.getString(profileImageIndex),
            fcmToken = cursor.getString(fcmTokenIndex),
            status = cursor.getString(statusIndex),
            pendingSync = cursor.getInt(pendingSyncIndex) == 1
        )
    }
} 
package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * The core database class that manages SQLite connection and schema creation.
 */
class AppDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "study_assistant.db"
        private const val DATABASE_VERSION = 1

        // Table Names
        const val TABLE_USER_PROFILE = "user_profile"
        const val TABLE_COURSES = "courses"
        const val TABLE_LECTURES = "lectures"
        const val TABLE_NOTES = "notes"
        const val TABLE_QUIZZES = "quizzes"
        const val TABLE_QUIZ_RESULTS = "quiz_results"
        const val TABLE_GROUPS = "groups"
        
        // Common Columns
        const val COLUMN_ID = "id"
        const val COLUMN_PENDING_SYNC = "pending_sync"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_UPDATED_AT = "updated_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create User Profile Table
        db.execSQL("""
            CREATE TABLE $TABLE_USER_PROFILE (
                $COLUMN_ID TEXT PRIMARY KEY,
                name TEXT,
                email TEXT,
                profile_image TEXT,
                fcm_token TEXT,
                status TEXT,
                $COLUMN_PENDING_SYNC INTEGER DEFAULT 0,
                $COLUMN_CREATED_AT INTEGER,
                $COLUMN_UPDATED_AT INTEGER
            )
        """)
        
        // Create Courses Table
        db.execSQL("""
            CREATE TABLE $TABLE_COURSES (
                $COLUMN_ID TEXT PRIMARY KEY,
                user_id TEXT,
                title TEXT,
                description TEXT,
                color TEXT,
                $COLUMN_PENDING_SYNC INTEGER DEFAULT 0,
                $COLUMN_CREATED_AT INTEGER,
                $COLUMN_UPDATED_AT INTEGER
            )
        """)
        
        // Other tables would be created here
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades here
    }
} 
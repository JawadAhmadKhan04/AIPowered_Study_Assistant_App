package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * The core database class that manages SQLite connection and schema creation.
 */
class AppDatabase private constructor(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        private const val DATABASE_NAME = "study_mate.db"
        private const val DATABASE_VERSION = 4

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = AppDatabase(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }

        // Common column names
        const val COLUMN_ID = "id"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_UPDATED_AT = "updated_at"
        const val COLUMN_PENDING_SYNC = "pending_sync"
        const val COLUMN_NAME = "name"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PHOTO_URL = "photo_url"
        const val COLUMN_FCM_TOKEN = "fcm_token"
        const val COLUMN_LAST_LOGIN = "last_login"
        const val COLUMN_USER_ID = "user_id"

        // Table names
        const val TABLE_USERS = "users"
        const val TABLE_USER_SETTINGS = "user_settings"
        const val TABLE_USER_PROFILE = "user_profile"
        const val TABLE_NOTIFICATIONS = "notifications"
        const val TABLE_BOOKMARKS = "bookmarks"
        const val TABLE_COURSES = "courses"
        const val TABLE_COURSE_MEMBERS = "course_members"
        const val TABLE_NOTES = "notes"
        const val TABLE_NOTE_TAGS = "note_tags"
        const val TABLE_NOTE_KEY_POINTS = "note_key_points"
        const val TABLE_NOTE_CONCEPTS = "note_concepts"
        const val TABLE_NOTE_MEMBERS = "note_members"
        const val TABLE_STUDY_GROUPS = "study_groups"
        const val TABLE_GROUP_MEMBERS = "group_members"
        const val TABLE_GROUP_CHATS = "group_chats"
        const val TABLE_QUIZZES = "quizzes"
        const val TABLE_QUIZ_QUESTIONS = "quiz_questions"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create tables
        // Users table
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID TEXT PRIMARY KEY,
                email TEXT NOT NULL,
                username TEXT NOT NULL,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                last_login INTEGER NOT NULL,
                fcm_token TEXT,
                $COLUMN_UPDATED_AT INTEGER NOT NULL,
                $COLUMN_PENDING_SYNC INTEGER DEFAULT 1
            )
        """)

        // User Settings table
        db.execSQL("""
            CREATE TABLE $TABLE_USER_SETTINGS (
                user_id TEXT PRIMARY KEY,
                quiz_notifications INTEGER DEFAULT 1,
                study_reminders INTEGER DEFAULT 1,
                add_in_groups INTEGER DEFAULT 1,
                auto_login INTEGER DEFAULT 0,
                auto_sync INTEGER DEFAULT 1,
                $COLUMN_UPDATED_AT INTEGER NOT NULL,
                $COLUMN_PENDING_SYNC INTEGER DEFAULT 1,
                FOREIGN KEY (user_id) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE
            )
        """)

        // User Profile table
        db.execSQL("""
            CREATE TABLE $TABLE_USER_PROFILE (
                user_id TEXT PRIMARY KEY,
                courses INTEGER DEFAULT 0,
                lectures INTEGER DEFAULT 0,
                smart_digests INTEGER DEFAULT 0,
                quizzes INTEGER DEFAULT 0,
                group_count INTEGER DEFAULT 0,
                time_spent TEXT,
                $COLUMN_UPDATED_AT INTEGER NOT NULL,
                $COLUMN_PENDING_SYNC INTEGER DEFAULT 1,
                FOREIGN KEY (user_id) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE
            )
        """)

        // Notifications table
        db.execSQL("""
            CREATE TABLE $TABLE_NOTIFICATIONS (
                $COLUMN_ID TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                heading TEXT NOT NULL,
                description TEXT,
                type TEXT NOT NULL,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                read INTEGER DEFAULT 0,
                data TEXT,
                $COLUMN_UPDATED_AT INTEGER NOT NULL,
                $COLUMN_PENDING_SYNC INTEGER DEFAULT 1,
                FOREIGN KEY (user_id) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE
            )
        """)

        // Bookmarks table
        db.execSQL("""
            CREATE TABLE $TABLE_BOOKMARKS (
                user_id TEXT NOT NULL,
                course_id TEXT NOT NULL,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_PENDING_SYNC INTEGER DEFAULT 1,
                PRIMARY KEY (user_id, course_id),
                FOREIGN KEY (user_id) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE
            )
        """)

        // Courses table
        db.execSQL("""
            CREATE TABLE $TABLE_COURSES (
                $COLUMN_ID TEXT PRIMARY KEY,
                title TEXT NOT NULL,
                description TEXT,
                created_by TEXT NOT NULL,
                color INTEGER,
                note_count INTEGER DEFAULT 0,
                is_bookmarked INTEGER DEFAULT 0,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_UPDATED_AT INTEGER NOT NULL,
                $COLUMN_PENDING_SYNC INTEGER DEFAULT 1,
                FOREIGN KEY (created_by) REFERENCES $TABLE_USERS($COLUMN_ID)
            )
        """)

        // Course Members table
        db.execSQL("""
            CREATE TABLE $TABLE_COURSE_MEMBERS (
                course_id TEXT NOT NULL,
                user_id TEXT NOT NULL,
                last_modified INTEGER NOT NULL,
                $COLUMN_PENDING_SYNC INTEGER DEFAULT 1,
                PRIMARY KEY (course_id, user_id),
                FOREIGN KEY (course_id) REFERENCES $TABLE_COURSES($COLUMN_ID) ON DELETE CASCADE,
                FOREIGN KEY (user_id) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE
            )
        """)

        // Notes table
        db.execSQL("""
            CREATE TABLE $TABLE_NOTES (
                $COLUMN_ID TEXT PRIMARY KEY,
                course_id TEXT NOT NULL,
                title TEXT NOT NULL,
                content TEXT,
                audio TEXT,
                type TEXT NOT NULL,
                created_by TEXT NOT NULL,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_UPDATED_AT INTEGER NOT NULL,
                summary TEXT,
                $COLUMN_PENDING_SYNC INTEGER DEFAULT 1,
                FOREIGN KEY (course_id) REFERENCES $TABLE_COURSES($COLUMN_ID) ON DELETE CASCADE,
                FOREIGN KEY (created_by) REFERENCES $TABLE_USERS($COLUMN_ID)
            )
        """)

        // Note Tags table
        db.execSQL("""
            CREATE TABLE $TABLE_NOTE_TAGS (
                note_id TEXT NOT NULL,
                tag INTEGER NOT NULL,
                $COLUMN_PENDING_SYNC INTEGER DEFAULT 1,
                PRIMARY KEY (note_id, tag),
                FOREIGN KEY (note_id) REFERENCES $TABLE_NOTES($COLUMN_ID) ON DELETE CASCADE
            )
        """)

        // Note Key Points table
        db.execSQL("""
            CREATE TABLE $TABLE_NOTE_KEY_POINTS (
                note_id TEXT NOT NULL,
                key_point TEXT NOT NULL,
                $COLUMN_PENDING_SYNC INTEGER DEFAULT 1,
                PRIMARY KEY (note_id, key_point),
                FOREIGN KEY (note_id) REFERENCES $TABLE_NOTES($COLUMN_ID) ON DELETE CASCADE
            )
        """)

        // Note Concepts table
        db.execSQL("""
            CREATE TABLE $TABLE_NOTE_CONCEPTS (
                note_id TEXT NOT NULL,
                concept TEXT NOT NULL,
                $COLUMN_PENDING_SYNC INTEGER DEFAULT 1,
                PRIMARY KEY (note_id, concept),
                FOREIGN KEY (note_id) REFERENCES $TABLE_NOTES($COLUMN_ID) ON DELETE CASCADE
            )
        """)

        // Note Members table
        db.execSQL("""
            CREATE TABLE $TABLE_NOTE_MEMBERS (
                note_id TEXT NOT NULL,
                user_id TEXT NOT NULL,
                last_modified INTEGER NOT NULL,
                $COLUMN_PENDING_SYNC INTEGER DEFAULT 1,
                PRIMARY KEY (note_id, user_id),
                FOREIGN KEY (note_id) REFERENCES $TABLE_NOTES($COLUMN_ID) ON DELETE CASCADE,
                FOREIGN KEY (user_id) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE
            )
        """)

        // Study Groups table
        db.execSQL("""
            CREATE TABLE $TABLE_STUDY_GROUPS (
                $COLUMN_ID TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                description TEXT,
                created_by TEXT NOT NULL,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                code TEXT NOT NULL,
                $COLUMN_UPDATED_AT INTEGER NOT NULL,
                $COLUMN_PENDING_SYNC INTEGER DEFAULT 1,
                FOREIGN KEY (created_by) REFERENCES $TABLE_USERS($COLUMN_ID)
            )
        """)

        // Group Members table
        db.execSQL("""
            CREATE TABLE $TABLE_GROUP_MEMBERS (
                group_id TEXT NOT NULL,
                user_id TEXT NOT NULL,
                role TEXT NOT NULL,
                joined_at INTEGER NOT NULL,
                $COLUMN_PENDING_SYNC INTEGER DEFAULT 1,
                PRIMARY KEY (group_id, user_id),
                FOREIGN KEY (group_id) REFERENCES $TABLE_STUDY_GROUPS($COLUMN_ID) ON DELETE CASCADE,
                FOREIGN KEY (user_id) REFERENCES $TABLE_USERS($COLUMN_ID) ON DELETE CASCADE
            )
        """)

        // Group Chats table - Modified to include messages
        db.execSQL("""
            CREATE TABLE $TABLE_GROUP_CHATS (
                group_id TEXT PRIMARY KEY,
                messages TEXT NOT NULL,  -- JSON string of messages array
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_UPDATED_AT INTEGER NOT NULL,
                $COLUMN_PENDING_SYNC INTEGER DEFAULT 1,
                FOREIGN KEY (group_id) REFERENCES $TABLE_STUDY_GROUPS($COLUMN_ID) ON DELETE CASCADE
            )
        """)

        // Quizzes table
        db.execSQL("""
            CREATE TABLE $TABLE_QUIZZES (
                $COLUMN_ID TEXT PRIMARY KEY,
                course_id TEXT NOT NULL,
                title TEXT NOT NULL,
                created_by TEXT NOT NULL,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                feedback TEXT,
                $COLUMN_UPDATED_AT INTEGER NOT NULL,
                $COLUMN_PENDING_SYNC INTEGER DEFAULT 1,
                FOREIGN KEY (course_id) REFERENCES $TABLE_COURSES($COLUMN_ID) ON DELETE CASCADE,
                FOREIGN KEY (created_by) REFERENCES $TABLE_USERS($COLUMN_ID)
            )
        """)

        // Quiz Questions table
        db.execSQL("""
            CREATE TABLE $TABLE_QUIZ_QUESTIONS (
                $COLUMN_ID TEXT PRIMARY KEY,
                quiz_id TEXT NOT NULL,
                question TEXT NOT NULL,
                options TEXT NOT NULL,  -- JSON string of options {"A": "string", "B": "string", ...}
                correct_answer TEXT NOT NULL,
                explanation TEXT,
                is_attempted INTEGER DEFAULT 0,
                is_correct INTEGER DEFAULT 0,
                selected_answer TEXT,
                $COLUMN_PENDING_SYNC INTEGER DEFAULT 1,
                FOREIGN KEY (quiz_id) REFERENCES $TABLE_QUIZZES($COLUMN_ID) ON DELETE CASCADE
            )
        """)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        // Enable foreign key constraints
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.w(AppDatabase::class.java.name, "Upgrading database from version $oldVersion to $newVersion")
        // Handle database upgrades here
        if (oldVersion < 3) {
            // Drop and recreate the group_chats table with new structure
            db.execSQL("DROP TABLE IF EXISTS $TABLE_GROUP_CHATS")
            db.execSQL("""
                CREATE TABLE $TABLE_GROUP_CHATS (
                    group_id TEXT PRIMARY KEY,
                    messages TEXT NOT NULL,  -- JSON string of messages array
                    $COLUMN_CREATED_AT INTEGER NOT NULL,
                    $COLUMN_UPDATED_AT INTEGER NOT NULL,
                    $COLUMN_PENDING_SYNC INTEGER DEFAULT 1,
                    FOREIGN KEY (group_id) REFERENCES $TABLE_STUDY_GROUPS($COLUMN_ID) ON DELETE CASCADE
                )
            """)
        }

        if (oldVersion < 4) {
            // Add pending_sync column to tables that were missing it
            db.execSQL("ALTER TABLE $TABLE_NOTE_TAGS ADD COLUMN $COLUMN_PENDING_SYNC INTEGER DEFAULT 1")
            db.execSQL("ALTER TABLE $TABLE_NOTE_KEY_POINTS ADD COLUMN $COLUMN_PENDING_SYNC INTEGER DEFAULT 1")
            db.execSQL("ALTER TABLE $TABLE_NOTE_CONCEPTS ADD COLUMN $COLUMN_PENDING_SYNC INTEGER DEFAULT 1")
            db.execSQL("ALTER TABLE $TABLE_NOTE_MEMBERS ADD COLUMN $COLUMN_PENDING_SYNC INTEGER DEFAULT 1")
        }
    }

    override fun getWritableDatabase(): SQLiteDatabase {
        return super.getWritableDatabase().apply {
            // Enable WAL mode using the proper method
            enableWriteAheadLogging()
        }
    }

    override fun getReadableDatabase(): SQLiteDatabase {
        return super.getReadableDatabase().apply {
            // Enable foreign keys
            execSQL("PRAGMA foreign_keys = ON")
        }
    }
} 
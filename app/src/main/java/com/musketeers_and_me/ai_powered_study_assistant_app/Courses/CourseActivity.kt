package com.musketeers_and_me.ai_powered_study_assistant_app.Courses

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils

class CourseActivity : AppCompatActivity(), OnBookmarkClickListener {

    private var bookmarked = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)
        enableEdgeToEdge()

        ToolbarUtils.setupToolbar(this, "Courses", R.drawable.home_logo_top_bar, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Courses"

        // Default fragment
        switchToCourses()

        findViewById<FrameLayout>(R.id.home_button_container).setOnClickListener {
            // Create intent for MainActivity
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
        onBackPressedDispatcher.addCallback(this) {
            // Simply return to MainActivity
            val intent = Intent(this@CourseActivity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
            finish()
        }

//        val switcher = findViewById<FrameLayout>(R.id.fragment_container)
//        switcher.setOnClickListener {
//            bookmarked = !bookmarked
//            if (bookmarked) {
//                switchToCourses()
//            } else {
//                switchToBookmarks()
//            }
//        }
    }

    fun switchToBookmarks() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, BookmarksFragment())
            .addToBackStack(null)
            .commit()
    }

    fun switchToCourses() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CoursesFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onBookmarkClicked() {
        // Replace CoursesFragment with BookmarksFragment
        bookmarked = !bookmarked
        if (bookmarked) {
            supportActionBar?.title = "Courses"
            switchToCourses()
        } else {
            supportActionBar?.title = "Bookmarks"
            switchToBookmarks()
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

}

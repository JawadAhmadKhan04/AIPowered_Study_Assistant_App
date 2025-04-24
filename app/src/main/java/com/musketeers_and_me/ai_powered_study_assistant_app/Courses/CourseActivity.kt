package com.musketeers_and_me.ai_powered_study_assistant_app.Courses

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils

class CourseActivity : AppCompatActivity(), OnBookmarkClickListener {

    private var bookmarked = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course)

        ToolbarUtils.setupToolbar(this, "Courses", R.drawable.home_logo_top_bar, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Courses"

        // Default fragment
        switchToCourses()

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
}

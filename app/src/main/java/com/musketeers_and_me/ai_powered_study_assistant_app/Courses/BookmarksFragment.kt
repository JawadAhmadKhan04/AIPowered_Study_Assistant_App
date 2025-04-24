package com.musketeers_and_me.ai_powered_study_assistant_app.Courses

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import com.musketeers_and_me.ai_powered_study_assistant_app.R


class BookmarksFragment : Fragment() {

    private var bookmarkClickListener: OnBookmarkClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnBookmarkClickListener) {
            bookmarkClickListener = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_courses, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_courses)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val courseList = listOf(
            Course("Introduction to Computer Science", 12, 2, R.color.brightred, true),
            Course("Introduction to Computer Science", 12, 2, R.color.darkgreen, true)
        )

        val bookmark = view.findViewById<ImageView>(R.id.icon_bookmark)
        bookmark.setImageResource(R.drawable.bookmark_filled)
        bookmark.setOnClickListener {
            bookmarkClickListener?.onBookmarkClicked()
        }

        val add_course = view.findViewById<ImageView>(R.id.icon_add)
        add_course.setOnClickListener {
            val intent = Intent(requireContext(), CreateCourseActivity::class.java)
            startActivity(intent)
        }

        val courses_bar = view.findViewById<EditText>(R.id.search_courses)
        courses_bar.setHint("Search for bookmarks...")

        recyclerView.adapter = CourseAdapter(courseList)
        return view
    }

    override fun onDetach() {
        super.onDetach()
        bookmarkClickListener = null
    }
}

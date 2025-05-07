package com.musketeers_and_me.ai_powered_study_assistant_app.Courses

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.AppDatabase
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBReadOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.dao.UserLocalDao
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.GlobalData
import kotlinx.coroutines.launch

class CoursesFragment : Fragment() {
    private lateinit var userLocalDao: UserLocalDao
    private var bookmarkClickListener: OnBookmarkClickListener? = null
    private lateinit var adapter: CourseAdapter
    private var allCourses: MutableList<Course> = mutableListOf()

    private val databaseService = FBDataBaseService()
    private val ReadOperations = FBReadOperations(databaseService)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnBookmarkClickListener) {
            bookmarkClickListener = context
        }
        val db = AppDatabase(context).writableDatabase
        userLocalDao = UserLocalDao(db)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_courses, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_courses)
        recyclerView.layoutManager = LinearLayoutManager(context)

        Log.d("CoursesFragment", "User ID: ${GlobalData.user_id}")

        // Load courses from local database
//        lifecycleScope.launch {
//            GlobalData.user_id?.let { userId ->
//                allCourses = userLocalDao.getCoursesByUserId(userId).toMutableList()
//                adapter = CourseAdapter(allCourses, false)
//                recyclerView.adapter = adapter
//            }
//        }

        GlobalData.user_id?.let {
            ReadOperations.getCourses(it, false) { courseList ->
                Log.d("CoursesFragment", "User ID: ${GlobalData.user_id}")
                allCourses = courseList
                Log.d("CoursesFragment", "Courses: $allCourses")
                adapter = CourseAdapter(courseList, false)
                                recyclerView.adapter = adapter

            }
        }

        val bookmark = view.findViewById<ImageView>(R.id.icon_bookmark)
        bookmark.setImageResource(R.drawable.bookmark)

        val add_course = view.findViewById<ImageView>(R.id.icon_add)
        add_course.setOnClickListener {
            val intent = Intent(requireContext(), CreateCourseActivity::class.java)
            startActivity(intent)
        }

        bookmark.setOnClickListener {
            bookmarkClickListener?.onBookmarkClicked()
        }

        val courses_bar = view.findViewById<EditText>(R.id.search_courses)
        courses_bar.setHint("Search for courses...")

        courses_bar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                val filteredList = allCourses.filter {
                    it.title.lowercase().contains(query) || it.description.lowercase().contains(query)
                }
                adapter.updateCourses(filteredList.toMutableList())
            }
        })

        return view
    }

    override fun onDetach() {
        super.onDetach()
        bookmarkClickListener = null
    }
}

interface OnBookmarkClickListener {
    fun onBookmarkClicked()
}

package com.musketeers_and_me.ai_powered_study_assistant_app.Courses

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.LectureAndNotes.AddLectureActivity
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.OfflineFirstDataManager
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.GlobalData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CourseAdapter(private var courses: MutableList<Course>, private val bookmarked: Boolean = false) :
    RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    private var dataManager: OfflineFirstDataManager? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun setDataManager(manager: OfflineFirstDataManager) {
        dataManager = manager
    }

    inner class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.course_title)
        val notes: TextView = view.findViewById(R.id.notes_count)
        val days: TextView = view.findViewById(R.id.last_accessed)
        val button: MaterialButton = view.findViewById(R.id.joinButton)
        val bookmark: ImageView = view.findViewById(R.id.bookmark_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.title.text = course.title
        holder.notes.text = "${course.noteCount} notes"
        holder.days.text = "${course.daysAgo} days ago"

        holder.bookmark.setImageResource(
            if (course.bookmarked) R.drawable.bookmark_filled else R.drawable.bookmark
        )

        holder.bookmark.setOnClickListener {
            Log.d("CourseAdapter", "Bookmark clicked for course: ${course.title}")

            // Toggle the bookmark state
            course.bookmarked = !course.bookmarked

            // Update the bookmark status using OfflineFirstDataManager
            GlobalData.user_id?.let { userId ->
                scope.launch {
                    try {
                        dataManager?.toggleBookmark(userId, course.courseId, course.bookmarked)

                        // If the course is unbookmarked and we're showing only bookmarked courses,
                        // remove it from the list
            if (!course.bookmarked && bookmarked) {
                            courses.removeAt(position)
                            notifyItemRemoved(position)
            } else {
                            notifyItemChanged(position)
                        }
                    } catch (e: Exception) {
                        Log.e("CourseAdapter", "Error toggling bookmark", e)
                        // Revert the bookmark state if there was an error
                        course.bookmarked = !course.bookmarked
                notifyItemChanged(position)
            }
        }
            }
        }

        holder.button.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, AddLectureActivity::class.java)
            intent.putExtra("course_title", holder.title.text)
            Log.d("CourseAdapter", "Course ID: ${course.courseId}")
            intent.putExtra("course_id", course.courseId)
            intent.putExtra("course_description", course.description)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = courses.size

    fun updateCourses(newCourses: MutableList<Course>) {
        courses = newCourses
        notifyDataSetChanged()
    }
}

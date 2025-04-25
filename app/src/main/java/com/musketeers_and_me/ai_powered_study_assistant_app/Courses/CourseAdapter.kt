package com.musketeers_and_me.ai_powered_study_assistant_app.Courses

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.content.Intent
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.LectureAndNotes.AddLectureActivity

class CourseAdapter(private val courses: List<Course>) :
    RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    inner class CourseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.course_title)
        val notes: TextView = view.findViewById(R.id.notes_count)
        val days: TextView = view.findViewById(R.id.last_accessed)
        val button: Button = view.findViewById(R.id.open_course_button)
        val bookmark: ImageView = view.findViewById(R.id.bookmark_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.title.text = course.title
        holder.notes.text = "${course.noteCount} notes"
        holder.days.text = "${course.daysAgo} days ago"
        holder.button.setBackgroundResource(course.buttonColorResId)
        holder.bookmark.setImageResource(
            if (course.bookmarked) R.drawable.bookmark_filled else R.drawable.bookmark
        )

        holder.button.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, AddLectureActivity::class.java)
            context.startActivity(intent)
        }


    }

    override fun getItemCount(): Int = courses.size
}
